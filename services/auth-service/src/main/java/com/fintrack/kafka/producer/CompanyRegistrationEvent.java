package com.fintrack.kafka.producer;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompanyRegistrationEvent {
    private UUID userId;
    private String email;
    private String companyName;
    private String sector;
}
