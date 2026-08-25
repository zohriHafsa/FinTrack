package com.fintrack.company_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishCompanyCreated(CompanyCreatedEvent event) {
        try {
            kafkaTemplate.send("company.created", event.getUserId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish company.created event: {}", ex.getMessage());
                        } else {
                            log.info("Successfully published company.created event to partition: {}",
                                    result.getRecordMetadata().partition());
                        }
                    });
        } catch (Exception e) {
            log.error("Error publishing company.created event: {}", e.getMessage(), e);
        }
    }
}