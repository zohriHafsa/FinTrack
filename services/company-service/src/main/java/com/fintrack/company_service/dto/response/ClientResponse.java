package com.fintrack.company_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ClientResponse {
    private UUID id;
    private UUID companyId;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
}
