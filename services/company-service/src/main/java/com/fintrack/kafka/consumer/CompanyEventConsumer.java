package com.fintrack.Kafka.consumer;

import com.fintrack.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CompanyEventConsumer {

    private final CompanyService companyService;

    @KafkaListener(
            topics = "${kafka.topics.company-registration}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleCompanyRegistration(CompanyRegistrationEvent event) {
        log.info("Received company registration event for: {}", event.getCompanyName());
        companyService.createCompanyFromEvent(event);
    }
}