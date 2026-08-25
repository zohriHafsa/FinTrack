package com.fintrack.ai_insights_service.service;

import com.fintrack.ai_insights_service.client.TransactionDataClient;
import com.fintrack.ai_insights_service.model.Insight;
import com.fintrack.ai_insights_service.model.InsightType;
import com.fintrack.ai_insights_service.repository.InsightRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.ai_insights_service.client.GeminiApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightGenerationService {

    private final GeminiApiService claudeApiService;
    private final TransactionDataClient transactionDataClient;
    private final InsightRepository insightRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public List<Insight> generateInsightsForCompany(UUID companyId) {
        log.info("Starting insight generation for company: {}", companyId);

        Map<String, Object> monthlyData = transactionDataClient.getMonthlyFinancialData(companyId);
        JsonNode recentTransactions = transactionDataClient.getRecentTransactions(companyId);
        Map<String, Object> invoiceData = transactionDataClient.getInvoiceData(companyId);

        String prompt = buildPrompt(companyId, monthlyData, recentTransactions, invoiceData);
        log.debug("Built prompt for Claude API");

        // Let failures here (auth, network, rate limit) propagate instead of being
        // swallowed - a silent empty result looks identical to "no insights found".
        String claudeResponse = claudeApiService.generateInsights(prompt);
        log.debug("Received response from Claude API");

        List<Insight> insights = parseClaudeResponse(claudeResponse, companyId);
        log.info("Parsed {} insights from Claude response", insights.size());

        deleteOldInsights(companyId);
        List<Insight> savedInsights = insightRepository.saveAll(insights);

        log.info("Successfully generated and saved {} insights for company: {}", savedInsights.size(), companyId);
        return savedInsights;
    }

    private String buildPrompt(UUID companyId, Map<String, Object> monthlyData,
                               JsonNode recentTransactions, Map<String, Object> invoiceData) {

        double totalIncome = monthlyData.containsKey("totalIncome")
                ? ((Number) monthlyData.get("totalIncome")).doubleValue()
                : 0.0;
        double totalExpenses = monthlyData.containsKey("totalExpenses")
                ? ((Number) monthlyData.get("totalExpenses")).doubleValue()
                : 0.0;
        double netCashFlow = totalIncome - totalExpenses;

        String topCategories = monthlyData.getOrDefault("categoryBreakdown", "No data").toString();
        int overdueCount = ((List<?>) invoiceData.getOrDefault("overdue", new ArrayList<>())).size();

        return String.format("""
                You are a financial advisor for Moroccan SMEs. Analyze this financial data for the company and generate exactly 3 insights.
                
                Financial data (in MAD):
                - Total income this month: %.2f MAD
                - Total expenses this month: %.2f MAD
                - Net cash flow: %.2f MAD
                - Top expense categories: %s
                - Number of overdue invoices: %d
                
                Respond ONLY with valid JSON in this exact format (no markdown, no code blocks, just raw JSON):
                {
                  "insights": [
                    {"title": "short title", "content": "detailed insight", "type": "TREND"},
                    {"title": "short title", "content": "detailed insight", "type": "SPENDING_ANOMALY"},
                    {"title": "short title", "content": "detailed insight", "type": "FORECAST"}
                  ]
                }
                """, totalIncome, totalExpenses, netCashFlow, topCategories, overdueCount);
    }

    private List<Insight> parseClaudeResponse(String claudeResponse, UUID companyId) {
        List<Insight> insights = new ArrayList<>();

        try {
            String jsonStr = claudeResponse;
            if (claudeResponse.contains("```json")) {
                jsonStr = claudeResponse.substring(
                        claudeResponse.indexOf("```json") + 7,
                        claudeResponse.lastIndexOf("```")
                ).trim();
            } else if (claudeResponse.contains("```")) {
                jsonStr = claudeResponse.substring(
                        claudeResponse.indexOf("```") + 3,
                        claudeResponse.lastIndexOf("```")
                ).trim();
            }

            JsonNode root = objectMapper.readTree(jsonStr);
            JsonNode insightsArray = root.get("insights");

            if (insightsArray != null && insightsArray.isArray()) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime validUntil = now.plusHours(24);

                for (JsonNode insightNode : insightsArray) {
                    try {
                        String title = insightNode.get("title").asText();
                        String content = insightNode.get("content").asText();
                        String typeStr = insightNode.get("type").asText().toUpperCase();
                        InsightType type = InsightType.valueOf(typeStr);

                        Insight insight = Insight.builder()
                                .companyId(companyId)
                                .type(type)
                                .title(title)
                                .content(content)
                                .generatedAt(now)
                                .validUntil(validUntil)
                                .build();

                        insights.add(insight);
                    } catch (Exception e) {
                        log.warn("Error parsing individual insight: {}", e.getMessage());
                    }
                }
            }

            log.info("Successfully parsed {} insights from Claude response", insights.size());

        } catch (Exception e) {
            log.error("Error parsing Claude JSON response: {}", e.getMessage(), e);
        }

        return insights;
    }

    @Transactional
    public void deleteOldInsights(UUID companyId) {
        try {
            List<Insight> expiredInsights = insightRepository.findExpiredInsightsByCompanyId(
                    companyId, LocalDateTime.now());

            if (!expiredInsights.isEmpty()) {
                insightRepository.deleteAll(expiredInsights);
                log.info("Deleted {} expired insights for company: {}", expiredInsights.size(), companyId);
            }
        } catch (Exception e) {
            log.error("Error deleting old insights: {}", e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteAllInsights(UUID companyId) {
        try {
            List<Insight> allInsights = insightRepository.findByCompanyId(companyId);
            insightRepository.deleteAll(allInsights);
            log.info("Deleted all {} insights for company: {}", allInsights.size(), companyId);
        } catch (Exception e) {
            log.error("Error deleting all insights: {}", e.getMessage(), e);
        }
    }
}