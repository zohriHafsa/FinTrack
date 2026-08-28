package com.fintrack.controller;


import com.fintrack.dto.request.InviteRequest;
import com.fintrack.dto.request.RegisterRequest;
import com.fintrack.dto.response.InvitePreviewResponse;
import com.fintrack.dto.response.InviteResponse;
import com.fintrack.dto.response.RegisterResponse;
import com.fintrack.dto.response.TeamMemberResponse;
import com.fintrack.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import com.fintrack.exception.UserNotFoundException;
import com.fintrack.model.User;
import com.fintrack.repository.UserRepository;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<RegisterResponse> getCurrentUser(
            @AuthenticationPrincipal Jwt jwt) {
        String keycloakId = jwt.getSubject();
        User user = userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new UserNotFoundException(
                        "No account found for this login. Please register first."));
        return ResponseEntity.ok(RegisterResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .companyId(user.getCompanyId())
                .message("ok")
                .build());
    }

    @PostMapping("/invite")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<InviteResponse> invite(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteRequest request) {
        InviteResponse response = authService.invite(jwt.getSubject(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/invite/{token}")
    public ResponseEntity<InvitePreviewResponse> previewInvite(@PathVariable String token) {
        return ResponseEntity.ok(authService.previewInvite(token));
    }

    @GetMapping("/team/{companyId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<TeamMemberResponse>> getTeamMembers(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID companyId) {
        return ResponseEntity.ok(authService.getTeamMembers(jwt.getSubject(), companyId));
    }

    @DeleteMapping("/team/{companyId}/members/{userId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID companyId,
            @PathVariable UUID userId) {
        authService.removeMember(jwt.getSubject(), companyId, userId);
        return ResponseEntity.noContent().build();
    }
}
