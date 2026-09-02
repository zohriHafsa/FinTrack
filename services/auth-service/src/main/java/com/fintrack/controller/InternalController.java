package com.fintrack.controller;

import com.fintrack.model.User;
import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Service-to-service endpoints called directly by other backend services
 * (bypassing the gateway), the same trust model already used elsewhere in
 * this codebase (e.g. ai-insights-service calling transaction-service).
 */
@RestController
@RequestMapping("/api/auth/internal")
@RequiredArgsConstructor
public class InternalController {

    private final UserRepository userRepository;

    @GetMapping("/companies/{companyId}/users")
    public ResponseEntity<List<UUID>> getCompanyUserIds(@PathVariable UUID companyId) {
        List<UUID> userIds = userRepository.findByCompanyId(companyId)
                .stream()
                .map(User::getId)
                .toList();
        return ResponseEntity.ok(userIds);
    }
}
