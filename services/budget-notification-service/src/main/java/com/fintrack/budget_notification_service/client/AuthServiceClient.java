package com.fintrack.budget_notification_service.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.auth-service.url}")
    private String authServiceUrl;

    public List<UUID> getCompanyUserIds(UUID companyId) {
        try {
            UUID[] ids = restTemplate.getForObject(
                    authServiceUrl + "/api/auth/internal/companies/{companyId}/users",
                    UUID[].class, companyId);
            return ids != null ? List.of(ids) : List.of();
        } catch (Exception e) {
            log.error("Failed to fetch users for company {}: {}", companyId, e.getMessage());
            return List.of();
        }
    }
}
