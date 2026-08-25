package com.fintrack.invoice_service.mapper;

import com.fintrack.invoice_service.dto.request.CreateInvoiceRequest;
import com.fintrack.invoice_service.dto.response.InvoiceResponse;
import com.fintrack.invoice_service.model.Invoice;
import com.fintrack.invoice_service.model.InvoiceStatus;

public class InvoiceMapper {

    public static Invoice toEntity(CreateInvoiceRequest request) {
        return Invoice.builder()
                .companyId(request.getCompanyId())
                .clientId(request.getClientId())
                .amount(request.getAmount())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .notes(request.getNotes())
                .status(InvoiceStatus.UNPAID)
                .build();
    }

    public static InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .id(invoice.getId())
                .companyId(invoice.getCompanyId())
                .clientId(invoice.getClientId())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .build();
    }
}