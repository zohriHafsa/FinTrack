package com.fintrack.kafka.producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {
    private final KafkaTemplate<String, CompanyRegistrationEvent> kafkaTemplate;

    @Value("${kafka.topics.company-registration}")
    private String companyRegistrationTopic;

    public void publishCompanyRegistration(CompanyRegistrationEvent event) {
        log.info("Publishing company registration event for user: {}", event.getEmail());
        kafkaTemplate.send(companyRegistrationTopic, event.getUserId().toString(), event);
    }
}
