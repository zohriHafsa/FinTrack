package com.fintrack.transaction_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionEventProducer {

    private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

    @Value("${kafka.topics.transaction-created}")
    private String transactionCreatedTopic;

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        log.info("Publishing transaction created event for company: {}",
                event.getCompanyId());
        kafkaTemplate.send(transactionCreatedTopic,
                event.getCompanyId().toString(), event);
    }
}