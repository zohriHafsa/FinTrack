package com.fintrack.transaction_service.mapper;

import com.fintrack.transaction_service.dto.request.CreateTransactionRequest;
import com.fintrack.transaction_service.dto.response.TransactionResponse;
import com.fintrack.transaction_service.model.Transaction;

public class TransactionMapper {
    public static Transaction toEntity(CreateTransactionRequest request){
        return Transaction.builder()
                .companyId(request.getCompanyId())
                .categoryId(request.getCategoryId())
                .amount(request.getAmount())
                .type(request.getType())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod())
                .date(request.getDate())
                .createdBy(request.getCreatedBy())
                .build();
    }

    public static TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .companyId(transaction.getCompanyId())
                .categoryId(transaction.getCategoryId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .description(transaction.getDescription())
                .paymentMethod(transaction.getPaymentMethod())
                .date(transaction.getDate())
                .createdBy(transaction.getCreatedBy())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
