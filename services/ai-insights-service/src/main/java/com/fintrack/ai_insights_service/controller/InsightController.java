package com.fintrack.ai_insights_service.controller;

import com.fintrack.ai_insights_service.dto.request.ChatRequest;
import com.fintrack.ai_insights_service.dto.response.ChatResponse;
import com.fintrack.ai_insights_service.dto.response.InsightResponse;
import com.fintrack.ai_insights_service.model.InsightType;
import com.fintrack.ai_insights_service.service.InsightGenerationService;
import com.fintrack.ai_insights_service.service.InsightService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;
    private final InsightGenerationService insightGenerationService;

    @GetMapping
    @PreAuthorize("@permissionService.hasAccessToCompany(#companyId)")
    public ResponseEntity<Page<InsightResponse>> getInsights(
            @RequestParam UUID companyId,
            @PageableDefault(size = 10, sort = "generatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/insights - companyId: {}", companyId);
        Page<InsightResponse> insights = insightService.getInsightsByCompanyId(companyId, pageable);
        return ResponseEntity.ok(insights);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InsightResponse> getInsightById(@PathVariable UUID id) {
        log.info("GET /api/insights/{} ", id);
        InsightResponse insight = insightService.getInsightById(id);

        if (insight == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(insight);
    }

    @GetMapping("/type/{type}")
    @PreAuthorize("@permissionService.hasAccessToCompany(#companyId)")
    public ResponseEntity<Page<InsightResponse>> getInsightsByType(
            @PathVariable InsightType type,
            @RequestParam UUID companyId,
            @PageableDefault(size = 10, sort = "generatedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("GET /api/insights/type/{} - companyId: {}", type, companyId);
        Page<InsightResponse> insights = insightService.getInsightsByType(companyId, type, pageable);
        return ResponseEntity.ok(insights);
    }

    @PostMapping("/generate")
    @PreAuthorize("@permissionService.hasAccessToCompany(#companyId)")
    public ResponseEntity<Void> generateInsights(@RequestParam UUID companyId) {
        log.info("POST /api/insights/generate - companyId: {}", companyId);

        insightGenerationService.generateInsightsForCompany(companyId);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/chat")
    @PreAuthorize("@permissionService.hasAccessToCompany(#companyId)")
    public ResponseEntity<ChatResponse> chat(
            @RequestParam UUID companyId,
            @RequestBody ChatRequest request) {
        log.info("POST /api/insights/chat - companyId: {}", companyId);

        if (request.getQuestion() == null || request.getQuestion().isBlank()) {
            throw new IllegalArgumentException("Question is required");
        }

        String answer = insightGenerationService.answerQuestion(companyId, request.getQuestion());
        return ResponseEntity.ok(ChatResponse.builder().answer(answer).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInsight(@PathVariable UUID id) {
        log.info("DELETE /api/insights/{}", id);

        insightService.deleteInsight(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAllInsights(@RequestParam UUID companyId) {
        log.info("DELETE /api/insights - companyId: {}", companyId);

        insightGenerationService.deleteAllInsights(companyId);

        return ResponseEntity.noContent().build();
    }
}