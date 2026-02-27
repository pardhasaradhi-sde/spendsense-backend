package com.spendsense.service.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsense.dto.response.SpendingInsightResponse;
import com.spendsense.model.Transaction;
import com.spendsense.model.Budget;
import com.spendsense.model.enums.TransactionType;
import com.spendsense.repository.TransactionRepository;
import com.spendsense.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * AI-powered spending insights service using Gemini
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AiInsightsService {

    private final GeminiClientService geminiClient;
    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final ObjectMapper objectMapper;

    /**
     * Generate personalized spending insights for a user
     * Cached for 24 hours to reduce API calls
     */
    @Cacheable(value = "spendingInsights", key = "#userId")
    public SpendingInsightResponse generateSpendingInsights(UUID userId) {
        log.info("Generating AI spending insights for user: {}", userId);

        try {
            // Fetch last 90 days of transactions
            LocalDateTime startDate = LocalDateTime.now().minusDays(90);
            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateAfterOrderByDateDesc(userId, startDate);

            if (transactions.isEmpty()) {
                return SpendingInsightResponse.builder()
                        .summary("No transaction data available yet. Start tracking your expenses!")
                        .recommendations(List.of("Add your first transaction to get personalized insights"))
                        .build();
            }

            // Compact JSON context — only send what Gemini needs
            Budget budget = budgetRepository.findByUserId(userId).orElse(null);
            String ctx = buildCompactContext(transactions, budget);
            String prompt = buildInsightsPrompt(ctx);

            // Get AI response and parse
            return parseInsightsResponse(geminiClient.generateContent(prompt));

        } catch (Exception e) {
            log.error("Error generating AI insights for user: {}", userId, e);
            return SpendingInsightResponse.builder()
                    .summary("Unable to generate insights at this time. Please try again later.")
                    .build();
        }
    }

    /**
     * Detect spending anomalies using AI
     */
    public List<String> detectAnomalies(UUID userId) {
        log.info("Detecting spending anomalies for user: {}", userId);

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(30);
            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateAfterOrderByDateDesc(userId, startDate);

            if (transactions.size() < 10) {
                return List.of("Need more transaction data to detect anomalies");
            }

            // Compact JSON context — only send what Gemini needs
            String ctx = buildCompactContext(transactions,
                    budgetRepository.findByUserId(userId).orElse(null));

            String prompt = "Financial advisor. Analyze this user data and return JSON ONLY (no markdown):\n" + ctx +
                    "\nReturn: {\"summary\":\"2 sentences\",\"recommendations\":[\"3-5 specific tips\"],\"patterns\":[\"2-3 key patterns\"],\"topCategories\":[\"Cat: $amt\"]}";

            return parseJsonArray(geminiClient.generateContent(prompt));

        } catch (Exception e) {
            log.error("Error detecting anomalies for user: {}", userId, e);
            return List.of("Unable to detect anomalies at this time");
        }
    }

    /**
     * Generate budget recommendations using AI
     */
    public List<String> generateBudgetRecommendations(UUID userId) {
        log.info("Generating budget recommendations for user: {}", userId);

        try {
            LocalDateTime startDate = LocalDateTime.now().minusDays(90);
            List<Transaction> transactions = transactionRepository
                    .findByUserIdAndDateAfterOrderByDateDesc(userId, startDate);

            Budget budget = budgetRepository.findByUserId(userId).orElse(null);

            String ctx = buildCompactContext(transactions, budget);

            String prompt = "Give 3-5 specific budget recommendations. Data:\n" + ctx +
                    "\nReturn JSON array only (no markdown): [\"tip1\",\"tip2\"]";

            return parseJsonArray(geminiClient.generateContent(prompt));

        } catch (Exception e) {
            log.error("Error generating budget recommendations for user: {}", userId, e);
            return List.of("Unable to generate recommendations at this time");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Builds a compact JSON context string to minimize tokens sent to Gemini.
     * Instead of verbose text, we send only the key numbers as JSON.
     */
    private String buildCompactContext(List<Transaction> transactions, Budget budget) {
        // Category totals (EXPENSE only)
        Map<String, BigDecimal> expenseByCategory = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .collect(Collectors.groupingBy(
                        t -> t.getCategory() != null ? t.getCategory() : "Other",
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        BigDecimal totalExpense = calculateTotalByType(transactions, TransactionType.EXPENSE);
        BigDecimal totalIncome = calculateTotalByType(transactions, TransactionType.INCOME);

        StringBuilder sb = new StringBuilder();
        sb.append("{\"income\":").append(totalIncome.setScale(2, RoundingMode.HALF_UP))
                .append(",\"expense\":").append(totalExpense.setScale(2, RoundingMode.HALF_UP))
                .append(",\"txCount\":").append(transactions.size());

        if (budget != null) {
            sb.append(",\"budget\":").append(budget.getAmount().setScale(2, RoundingMode.HALF_UP));
        }

        sb.append(",\"categories\":{");
        boolean first = true;
        for (Map.Entry<String, BigDecimal> e : expenseByCategory.entrySet()) {
            if (!first)
                sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":").append(e.getValue().setScale(2, RoundingMode.HALF_UP));
            first = false;
        }
        sb.append("}}");
        return sb.toString();
    }

    private BigDecimal calculateTotalByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildInsightsPrompt(String ctx) {
        return "Financial advisor. Analyze this user data and return JSON ONLY (no markdown):\n" + ctx +
                "\nReturn: {\"summary\":\"2 sentences\",\"recommendations\":[\"3-5 specific tips\"],\"patterns\":[\"2-3 key patterns\"],\"topCategories\":[\"Cat: $amt\"]}";
    }

    private SpendingInsightResponse parseInsightsResponse(String aiResponse) {
        try {
            // Extract first JSON object from the response (handles markdown, preamble text,
            // etc.)
            String jsonContent = extractJsonObject(aiResponse);

            // Parse JSON
            Map<String, Object> responseMap = objectMapper.readValue(jsonContent, Map.class);

            return SpendingInsightResponse.builder()
                    .summary((String) responseMap.get("summary"))
                    .recommendations((List<String>) responseMap.getOrDefault("recommendations", List.of()))
                    .patterns((List<String>) responseMap.getOrDefault("patterns", List.of()))
                    .topCategories((List<String>) responseMap.getOrDefault("topCategories", List.of()))
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error parsing AI insights response: {}", e.getMessage());
            return SpendingInsightResponse.builder()
                    .summary(aiResponse.length() > 500 ? aiResponse.substring(0, 500) + "..." : aiResponse)
                    .recommendations(List.of("Unable to parse detailed insights"))
                    .build();
        }
    }

    private List<String> parseJsonArray(String aiResponse) {
        try {
            String jsonContent = extractJsonArray(aiResponse);
            return objectMapper.readValue(jsonContent, List.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON array from AI response: {}", e.getMessage());
            return List.of(aiResponse.length() > 200 ? aiResponse.substring(0, 200) : aiResponse);
        }
    }

    /**
     * Extracts the first JSON object {...} from a string that may contain markdown
     * or other text.
     */
    private String extractJsonObject(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\{[\\s\\S]*\\}",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        // Fallback: strip markdown fences and return
        return text.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
    }

    /**
     * Extracts the first JSON array [...] from a string that may contain markdown
     * or other text.
     */
    private String extractJsonArray(String text) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[[\\s\\S]*\\]",
                java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        // Fallback: strip markdown fences and return
        return text.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
    }
}