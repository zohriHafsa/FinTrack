package com.fintrack.transaction_service.service;

import com.fintrack.transaction_service.dto.request.BulkCategorizeRequest;
import com.fintrack.transaction_service.dto.request.CreateTransactionRequest;
import com.fintrack.transaction_service.dto.request.UpdateTransactionRequest;
import com.fintrack.transaction_service.dto.response.TransactionResponse;
import com.fintrack.transaction_service.exception.ResourceNotFoundException;
import com.fintrack.transaction_service.kafka.producer.TransactionCreatedEvent;
import com.fintrack.transaction_service.kafka.producer.TransactionEventProducer;
import com.fintrack.transaction_service.mapper.TransactionMapper;
import com.fintrack.transaction_service.model.Transaction;
import com.fintrack.transaction_service.model.TransactionType;
import com.fintrack.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionEventProducer eventProducer;

    @Transactional
    public TransactionResponse createTransaction(CreateTransactionRequest request) {
        log.info("Creating transaction for company: {}", request.getCompanyId());
        Transaction transaction = TransactionMapper.toEntity(request);
        Transaction saved = transactionRepository.save(transaction);

        // Publish Kafka event
        TransactionCreatedEvent event = TransactionCreatedEvent.builder()
                .transactionId(saved.getId())
                .companyId(saved.getCompanyId())
                .categoryId(saved.getCategoryId())
                .amount(saved.getAmount())
                .type(saved.getType())
                .date(saved.getDate())
                .build();
        eventProducer.publishTransactionCreated(event);

        return TransactionMapper.toResponse(saved);
    }

    public Page<TransactionResponse> getTransactions(
            UUID companyId,
            TransactionType type,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        return transactionRepository.findWithFilters(
                        companyId, type, categoryId, startDate, endDate, pageable)
                .map(TransactionMapper::toResponse);
    }

    public TransactionResponse getTransactionById(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id
                ));
        return TransactionMapper.toResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateTransaction(UUID id,
                                                 UpdateTransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id
                ));

        transaction.setCategoryId(request.getCategoryId());
        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod());
        transaction.setDate(request.getDate());

        Transaction updated = transactionRepository.save(transaction);
        log.info("Updated transaction: {}", id);
        return TransactionMapper.toResponse(updated);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction not found with id: " + id
                ));
        transactionRepository.delete(transaction);
        log.info("Deleted transaction: {}", id);
    }

    @Transactional
    public void bulkCategorize(BulkCategorizeRequest request) {
        List<Transaction> transactions = transactionRepository
                .findAllById(request.getTransactionIds());
        transactions.forEach(t -> t.setCategoryId(request.getCategoryId()));
        transactionRepository.saveAll(transactions);
        log.info("Bulk categorized {} transactions", transactions.size());
    }

    @Transactional
    public List<TransactionResponse> importFromCsv(
            UUID companyId, UUID createdBy, MultipartFile file) {
        List<TransactionResponse> imported = new ArrayList<>();

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(file.getInputStream()))) {

            List<String[]> rows = reader.readAll();
            // Skip header row
            rows.remove(0);

            for (String[] row : rows) {
                try {
                    Transaction transaction = Transaction.builder()
                            .companyId(companyId)
                            .categoryId(UUID.fromString(row[0]))
                            .amount(new BigDecimal(row[1]))
                            .type(TransactionType.valueOf(row[2].toUpperCase()))
                            .description(row[3])
                            .paymentMethod(com.fintrack.transaction_service
                                    .model.PaymentMethod.valueOf(row[4].toUpperCase()))
                            .date(LocalDate.parse(row[5]))
                            .createdBy(createdBy)
                            .build();

                    Transaction saved = transactionRepository.save(transaction);
                    imported.add(TransactionMapper.toResponse(saved));
                } catch (Exception e) {
                    log.warn("Skipping invalid CSV row: {}", e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage());
        }

        log.info("Imported {} transactions from CSV", imported.size());
        return imported;
    }
}