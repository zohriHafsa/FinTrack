package com.fintrack.Kafka.consumer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyRegistrationEvent {
    private UUID userId;
    private String email;
    private String companyName;
    private String sector;
}
