package com.fintrack.ai_insights_service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

@SpringBootApplication
@EnableScheduling
public class AiInsightsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiInsightsServiceApplication.class, args);
    }
}

/**
 * Scheduled task to regenerate insights daily for all companies
 */
@Slf4j
@Component
@RequiredArgsConstructor
class InsightScheduledTask {

    private final com.fintrack.ai_insights_service.repository.InsightRepository insightRepository;
    private final com.fintrack.ai_insights_service.service.InsightGenerationService insightGenerationService;

    /**
     * Run daily at 6:00 AM
     * Regenerates insights for all companies with existing insights
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void regenerateInsightsDaily() {
        log.info("Starting scheduled insight regeneration");

        try {
            // Get all unique company IDs that have insights
            var allInsights = insightRepository.findAll();
            var companyIds = allInsights.stream()
                    .map(insight -> insight.getCompanyId())
                    .distinct()
                    .toList();

            log.info("Found {} companies with existing insights", companyIds.size());

            // Regenerate insights for each company
            for (UUID companyId : companyIds) {
                try {
                    log.info("Regenerating insights for company: {}", companyId);
                    insightGenerationService.generateInsightsForCompany(companyId);
                } catch (Exception e) {
                    log.error("Error regenerating insights for company {}: {}", companyId, e.getMessage(), e);
                }
            }

            log.info("Completed scheduled insight regeneration");

        } catch (Exception e) {
            log.error("Error in scheduled insight regeneration: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup expired insights (runs daily at 2:00 AM)
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredInsights() {
        log.info("Starting cleanup of expired insights");

        try {
            var expiredInsights = insightRepository.findExpiredInsights(java.time.LocalDateTime.now());

            if (!expiredInsights.isEmpty()) {
                insightRepository.deleteAll(expiredInsights);
                log.info("Deleted {} expired insights", expiredInsights.size());
            } else {
                log.info("No expired insights to clean up");
            }

        } catch (Exception e) {
            log.error("Error cleaning up expired insights: {}", e.getMessage(), e);
        }
    }
}