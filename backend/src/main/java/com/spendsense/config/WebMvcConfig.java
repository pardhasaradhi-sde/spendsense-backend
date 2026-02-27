package com.spendsense.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * MVC configuration: registers flexible date-time converters so that
 * query params like startDate/endDate accept both:
 * - ISO 8601 with T: 2026-02-01T09:00:00
 * - Space-separated: 2026-02-01 09:00:00
 * - Date only: 2026-02-01 (defaults to start-of-day)
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter FLEXIBLE_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // 2026-02-01T09:00:00
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) // 2026-02-01 09:00:00
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) // 2026-02-01 09:00
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd")) // 2026-02-01 → 00:00
            .toFormatter();

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(String.class, LocalDateTime.class, source -> {
            String trimmed = source.trim();
            if (trimmed.isEmpty())
                return null;

            // Normalise space to T so ISO parser can handle it too
            String normalised = trimmed.replace(' ', 'T');

            try {
                return LocalDateTime.parse(normalised, FLEXIBLE_FORMATTER);
            } catch (Exception e) {
                // Last-chance: date-only input → midnight
                try {
                    return LocalDateTime.parse(trimmed + "T00:00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } catch (Exception ex) {
                    throw new IllegalArgumentException(
                            "Cannot parse date-time value '" + source +
                                    "'. Use format: yyyy-MM-dd'T'HH:mm:ss or yyyy-MM-dd HH:mm:ss",
                            ex);
                }
            }
        });
    }
}
