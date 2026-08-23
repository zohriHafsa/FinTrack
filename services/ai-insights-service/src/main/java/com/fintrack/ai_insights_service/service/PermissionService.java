package com.fintrack.ai_insights_service.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class PermissionService {

    public boolean hasAccessToCompany(UUID companyId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("User not authenticated");
                return false;
            }

            if (authentication.getPrincipal() instanceof Jwt jwt) {
                Object companyIdClaim = jwt.getClaim("company_id");

                if (companyIdClaim != null) {
                    Long userCompanyId = ((Number) companyIdClaim).longValue();
                    boolean hasAccess = userCompanyId.equals(companyId);

                    if (!hasAccess) {
                        log.warn("User company {} does not match requested company {}", userCompanyId, companyId);
                    }

                    return hasAccess;
                }
            }

            log.warn("No company_id found in token");
            return false;

        } catch (Exception e) {
            log.error("Error checking permission: {}", e.getMessage(), e);
            return false;
        }
    }
}