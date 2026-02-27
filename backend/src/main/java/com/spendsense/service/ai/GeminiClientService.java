package com.spendsense.service.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.spendsense.config.GeminiConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

/**
 * Low-level client for Google Gemini API
 * Handles direct API communication with retry logic
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiClientService {

    private final WebClient geminiWebClient;
    private final GeminiConfig geminiConfig;

    /**
     * Generate text content from a text prompt
     */
    public String generateContent(String prompt) {
        return generateContent(prompt, geminiConfig.getModel());
    }

    /**
     * Generate text content with specific model
     */
    public String generateContent(String prompt, String model) {
        log.debug("Generating content with Gemini model: {}", model);

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(Part.text(prompt)))
                                .build()))
                .generationConfig(GenerationConfig.builder()
                        .temperature(0.7)
                        .topK(40)
                        .topP(0.95)
                        .maxOutputTokens(4096)
                        .build())
                .build();

        return callGeminiApi(request, model);
    }

    /**
     * Generate content from image and text (Vision API)
     */
    public String generateContentWithImage(String prompt, byte[] imageData, String mimeType) {
        log.debug("Generating content with image using Gemini Vision model");

        String base64Image = Base64.getEncoder().encodeToString(imageData);

        GeminiRequest request = GeminiRequest.builder()
                .contents(List.of(
                        Content.builder()
                                .parts(List.of(
                                        Part.text(prompt),
                                        Part.inlineData(base64Image, mimeType)))
                                .build()))
                .generationConfig(GenerationConfig.builder()
                        .temperature(0.4)
                        .topK(32)
                        .topP(0.95)
                        .maxOutputTokens(4096)
                        .build())
                .build();

        return callGeminiApi(request, geminiConfig.getVisionModel());
    }

    /**
     * Make API call to Gemini with retry logic
     */
    private String callGeminiApi(GeminiRequest request, String model) {
        try {
            String endpoint = String.format("/models/%s:generateContent?key=%s",
                    model, geminiConfig.getKey());

            GeminiResponse response = geminiWebClient.post()
                    .uri(endpoint)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .retryWhen(Retry.backoff(geminiConfig.getMaxRetries(), Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests ||
                                    throwable instanceof WebClientResponseException.ServiceUnavailable)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                log.error("Max retries exceeded for Gemini API");
                                return new RuntimeException("Failed to get response from Gemini API after retries");
                            }))
                    .block();

            if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
                log.error("Empty response from Gemini API");
                throw new RuntimeException("Empty response from Gemini API");
            }

            String generatedText = response.getCandidates().getFirst()
                    .getContent()
                    .getParts()
                    .getFirst()
                    .getText();

            log.debug("Successfully generated content from Gemini");
            return generatedText;

        } catch (WebClientResponseException e) {
            log.error("Gemini API error - Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Gemini API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error calling Gemini API", e);
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    // ==================== Request/Response DTOs ====================

    @Data
    @lombok.Builder
    public static class GeminiRequest {
        private List<Content> contents;
        private GenerationConfig generationConfig;
    }

    @Data
    @lombok.Builder
    public static class Content {
        private List<Part> parts;
    }

    @Data
    public static class Part {
        private String text;
        private InlineData inlineData;

        public static Part text(String text) {
            Part part = new Part();
            part.text = text;
            return part;
        }

        public static Part inlineData(String base64Data, String mimeType) {
            Part part = new Part();
            part.inlineData = new InlineData(mimeType, base64Data);
            return part;
        }
    }

    @Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class InlineData {
        @JsonProperty("mime_type")
        private String mimeType;
        private String data;
    }

    @Data
    @lombok.Builder
    public static class GenerationConfig {
        private Double temperature;
        private Integer topK;
        private Double topP;
        private Integer maxOutputTokens;
    }

    @Data
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    public static class Candidate {
        private Content content;
        private String finishReason;
        private Integer index;
    }
}