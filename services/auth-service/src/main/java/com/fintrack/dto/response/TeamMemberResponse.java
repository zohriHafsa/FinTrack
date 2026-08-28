package com.fintrack.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class TeamMemberResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String role;
    private String status;
    private UUID companyId;
}
