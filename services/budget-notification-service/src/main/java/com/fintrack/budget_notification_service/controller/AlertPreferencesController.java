package com.fintrack.budget_notification_service.controller;

import com.fintrack.budget_notification_service.dto.request.UpdateAlertPreferencesRequest;
import com.fintrack.budget_notification_service.dto.response.AlertPreferencesResponse;
import com.fintrack.budget_notification_service.service.AlertPreferencesService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/alert-preferences")
@RequiredArgsConstructor
@Slf4j
public class AlertPreferencesController {

    private final AlertPreferencesService alertPreferencesService;

    @GetMapping
    public ResponseEntity<AlertPreferencesResponse> getPreferences(
            @RequestParam UUID userId) {
        return ResponseEntity.ok(
                alertPreferencesService.getPreferences(userId));
    }

    @PutMapping
    public ResponseEntity<AlertPreferencesResponse> updatePreferences(
            @RequestParam UUID userId,
            @RequestBody UpdateAlertPreferencesRequest request) {
        return ResponseEntity.ok(
                alertPreferencesService.updatePreferences(userId, request));
    }
}