package com.fintrack.budget_notification_service.kafka.consumer;

import com.fintrack.budget_notification_service.model.AlertPreferences;
import com.fintrack.budget_notification_service.model.NotificationType;
import com.fintrack.budget_notification_service.service.BudgetService;
import com.fintrack.budget_notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetNotificationConsumer {

    private final BudgetService budgetService;
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.transaction-created}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "transactionKafkaListenerContainerFactory"
    )
    public void handleTransactionCreated(TransactionCreatedEvent event) {
        log.info("Received transaction event for company: {}", event.getCompanyId());
        if ("EXPENSE".equals(event.getType())) {
            budgetService.updateSpendAndCheckBudget(
                    event.getCompanyId(),
                    event.getCategoryId(),
                    event.getAmount()
            );
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.invoice-status-changed}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "invoiceKafkaListenerContainerFactory"
    )
    public void handleInvoiceStatusChanged(InvoiceStatusChangedEvent event) {
        log.info("Received invoice status event: {} → {}",
                event.getInvoiceId(), event.getStatus());

        if ("OVERDUE".equals(event.getStatus())) {
            notificationService.notifyCompany(
                    event.getCompanyId(),
                    NotificationType.INVOICE_OVERDUE,
                    String.format("Invoice of %.2f MAD is overdue since %s",
                            event.getAmount(), event.getDueDate()),
                    AlertPreferences::getInvoiceOverdue
            );
        } else if ("PAID".equals(event.getStatus())) {
            notificationService.notifyCompany(
                    event.getCompanyId(),
                    NotificationType.PAYMENT_RECEIVED,
                    String.format("Payment of %.2f MAD received", event.getAmount()),
                    AlertPreferences::getPaymentReceived
            );
        }
    }
}