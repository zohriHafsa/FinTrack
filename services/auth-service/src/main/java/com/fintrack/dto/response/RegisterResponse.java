package com.fintrack.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class RegisterResponse {
    private UUID userId;
    private String email;
    private String role;
    private UUID companyId;
    private String message;
}