package com.fintrack.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class InviteResponse {
    private UUID id;
    private String email;
    private String status;
    private String message;
}
