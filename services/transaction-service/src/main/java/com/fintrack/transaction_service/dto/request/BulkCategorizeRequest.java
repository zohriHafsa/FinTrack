package com.fintrack.transaction_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class BulkCategorizeRequest {

    @NotNull
    private List<UUID> transactionIds;

    @NotNull
    private UUID categoryId;
}