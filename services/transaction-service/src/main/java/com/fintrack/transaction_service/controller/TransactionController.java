package com.fintrack.transaction_service.controller;

import com.fintrack.transaction_service.dto.request.BulkCategorizeRequest;
import com.fintrack.transaction_service.dto.request.CreateTransactionRequest;
import com.fintrack.transaction_service.dto.request.UpdateTransactionRequest;
import com.fintrack.transaction_service.dto.response.TransactionResponse;
import com.fintrack.transaction_service.model.TransactionType;
import com.fintrack.transaction_service.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.createTransaction(request));
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam UUID companyId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getTransactions(
                companyId, type, categoryId, startDate, endDate, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable UUID id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/bulk-categorize")
    public ResponseEntity<Void> bulkCategorize(
            @Valid @RequestBody BulkCategorizeRequest request) {
        transactionService.bulkCategorize(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/import")
    public ResponseEntity<List<TransactionResponse>> importCsv(
            @RequestParam UUID companyId,
            @RequestParam UUID createdBy,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.importFromCsv(companyId, createdBy, file));
    }
}