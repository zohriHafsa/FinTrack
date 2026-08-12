package com.fintrack.budget_notification_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetEventProducer {

    private final KafkaTemplate<String, BudgetExceededEvent> kafkaTemplate;

    @Value("${kafka.topics.budget-exceeded}")
    private String budgetExceededTopic;

    public void publishBudgetExceeded(BudgetExceededEvent event) {
        log.info("Publishing budget exceeded event for company: {}",
                event.getCompanyId());
        kafkaTemplate.send(budgetExceededTopic,
                event.getCompanyId().toString(), event);
    }
}