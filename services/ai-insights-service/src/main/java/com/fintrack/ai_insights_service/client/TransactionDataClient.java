package com.fintrack.ai_insights_service.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class TransactionDataClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public TransactionDataClient(
            @Value("${services.transaction-service.url}") String transactionServiceUrl,
            WebClient webClient,
            ObjectMapper objectMapper) {
        this.webClient = webClient.mutate()
                .baseUrl(transactionServiceUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getMonthlyFinancialData(UUID companyId) {
        try {
            log.info("Fetching monthly financial data for company: {}", companyId);

            Map<String, Object> response = webClient.get()
                    .uri("/api/transactions/monthly-summary?companyId={companyId}", companyId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Successfully fetched monthly financial data");
            return response != null ? response : new HashMap<>();

        } catch (Exception e) {
            log.error("Error fetching monthly financial data: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public JsonNode getRecentTransactions(UUID companyId) {
        try {
            log.info("Fetching recent transactions for company: {}", companyId);

            LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);

            Map<String, Object> response = webClient.get()
                    .uri("/api/transactions?companyId={companyId}&startDate={startDate}&endDate={endDate}&page=0&size=100",
                            companyId, thirtyDaysAgo, LocalDate.now())
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return objectMapper.valueToTree(response);

        } catch (Exception e) {
            log.error("Error fetching recent transactions: {}", e.getMessage(), e);
            return objectMapper.createObjectNode();
        }
    }

    public Map<String, Object> getInvoiceData(UUID companyId) {
        try {
            log.info("Fetching invoice data for company: {}", companyId);

            Map<String, Object> response = webClient.get()
                    .uri("/api/invoices/overdue?companyId={companyId}", companyId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            log.info("Successfully fetched invoice data");
            return response != null ? response : new HashMap<>();

        } catch (Exception e) {
            log.error("Error fetching invoice data: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public String getTopExpenseCategories(UUID companyId) {
        try {
            Map<String, Object> summary = getMonthlyFinancialData(companyId);

            if (summary.containsKey("categoryBreakdown")) {
                return summary.get("categoryBreakdown").toString();
            }

            return "No category data available";

        } catch (Exception e) {
            log.error("Error calculating top expense categories: {}", e.getMessage());
            return "Unable to fetch category data";
        }
    }
}