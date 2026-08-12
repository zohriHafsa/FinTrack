package com.fintrack.invoice_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventProducer {

    private final KafkaTemplate<String, InvoiceStatusChangedEvent> kafkaTemplate;

    @Value("${kafka.topics.invoice-status-changed}")
    private String invoiceStatusChangedTopic;

    public void publishInvoiceStatusChanged(InvoiceStatusChangedEvent event) {
        log.info("Publishing invoice status changed event: {} → {}",
                event.getInvoiceId(), event.getStatus());
        kafkaTemplate.send(invoiceStatusChangedTopic,
                event.getCompanyId().toString(), event);
    }
}