package com.fintrack.invoice_service.service;

import com.fintrack.invoice_service.dto.request.CreateInvoiceRequest;
import com.fintrack.invoice_service.dto.request.UpdateInvoiceRequest;
import com.fintrack.invoice_service.dto.response.InvoiceResponse;
import com.fintrack.invoice_service.exception.ResourceNotFoundException;
import com.fintrack.invoice_service.kafka.producer.InvoiceEventProducer;
import com.fintrack.invoice_service.kafka.producer.InvoiceStatusChangedEvent;
import com.fintrack.invoice_service.mapper.InvoiceMapper;
import com.fintrack.invoice_service.model.Invoice;
import com.fintrack.invoice_service.model.InvoiceStatus;
import com.fintrack.invoice_service.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceEventProducer eventProducer;

    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        log.info("Creating invoice for company: {}", request.getCompanyId());
        Invoice invoice = InvoiceMapper.toEntity(request);
        Invoice saved = invoiceRepository.save(invoice);
        return InvoiceMapper.toResponse(saved);
    }

    public Page<InvoiceResponse> getInvoices(
            UUID companyId,
            InvoiceStatus status,
            UUID clientId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        return invoiceRepository.findWithFilters(
                        companyId, status, clientId, startDate, endDate, pageable)
                .map(InvoiceMapper::toResponse);
    }

    public InvoiceResponse getInvoiceById(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id
                ));
        return InvoiceMapper.toResponse(invoice);
    }

    @Transactional
    public InvoiceResponse updateInvoice(UUID id, UpdateInvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id
                ));

        // Can only update UNPAID invoices
        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new RuntimeException("Cannot update a paid or overdue invoice");
        }

        invoice.setClientId(request.getClientId());
        invoice.setAmount(request.getAmount());
        invoice.setDueDate(request.getDueDate());
        invoice.setNotes(request.getNotes());

        Invoice updated = invoiceRepository.save(invoice);
        log.info("Updated invoice: {}", id);
        return InvoiceMapper.toResponse(updated);
    }

    @Transactional
    public InvoiceResponse markAsPaid(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id
                ));

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(LocalDate.now());

        Invoice saved = invoiceRepository.save(invoice);
        log.info("Invoice marked as paid: {}", id);

        // Publish Kafka event
        eventProducer.publishInvoiceStatusChanged(
                InvoiceStatusChangedEvent.builder()
                        .invoiceId(saved.getId())
                        .companyId(saved.getCompanyId())
                        .clientId(saved.getClientId())
                        .status(InvoiceStatus.PAID)
                        .amount(saved.getAmount())
                        .dueDate(saved.getDueDate())
                        .build()
        );

        return InvoiceMapper.toResponse(saved);
    }

    @Transactional
    public void deleteInvoice(UUID id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invoice not found with id: " + id
                ));
        invoiceRepository.delete(invoice);
        log.info("Deleted invoice: {}", id);
    }

    public List<InvoiceResponse> getOverdueInvoices(UUID companyId) {
        return invoiceRepository
                .findByCompanyIdAndStatus(companyId, InvoiceStatus.OVERDUE)
                .stream()
                .map(InvoiceMapper::toResponse)
                .toList();
    }

    // Runs every day at midnight
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkOverdueInvoices() {
        log.info("Running overdue invoice check...");

        List<Invoice> overdueInvoices = invoiceRepository
                .findByStatusAndDueDateBefore(InvoiceStatus.UNPAID, LocalDate.now());

        for (Invoice invoice : overdueInvoices) {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepository.save(invoice);

            // Publish Kafka event for each overdue invoice
            eventProducer.publishInvoiceStatusChanged(
                    InvoiceStatusChangedEvent.builder()
                            .invoiceId(invoice.getId())
                            .companyId(invoice.getCompanyId())
                            .clientId(invoice.getClientId())
                            .status(InvoiceStatus.OVERDUE)
                            .amount(invoice.getAmount())
                            .dueDate(invoice.getDueDate())
                            .build()
            );
        }

        log.info("Marked {} invoices as overdue", overdueInvoices.size());
    }
}