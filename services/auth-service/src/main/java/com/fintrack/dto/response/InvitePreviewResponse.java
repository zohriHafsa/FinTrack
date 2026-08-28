package com.fintrack.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InvitePreviewResponse {
    private boolean valid;
    private String email;
    private String message;
}
