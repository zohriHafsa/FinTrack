package com.fintrack.ai_insights_service.service;

import com.fintrack.ai_insights_service.dto.response.InsightResponse;
import com.fintrack.ai_insights_service.mapper.InsightMapper;
import com.fintrack.ai_insights_service.model.Insight;
import com.fintrack.ai_insights_service.model.InsightType;
import com.fintrack.ai_insights_service.repository.InsightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsightService {

    private final InsightRepository insightRepository;
    private final InsightMapper insightMapper;

    @Transactional(readOnly = true)
    public Page<InsightResponse> getInsightsByCompanyId(UUID companyId, Pageable pageable) {
        log.info("Fetching insights for company: {} with page: {}", companyId, pageable);

        Page<Insight> insights = insightRepository.findValidInsightsByCompanyId(
                companyId, LocalDateTime.now(), pageable);

        return new PageImpl<>(
                insights.getContent().stream()
                        .map(insightMapper::toResponse)
                        .collect(Collectors.toList()),
                pageable,
                insights.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public Page<InsightResponse> getInsightsByType(UUID companyId, InsightType type, Pageable pageable) {
        log.info("Fetching {} insights for company: {}", type, companyId);

        var insights = insightRepository.findByCompanyIdAndTypeOrderByGeneratedAtDesc(companyId, type);

        var filteredInsights = insights.stream()
                .filter(i -> i.getValidUntil().isAfter(LocalDateTime.now()))
                .map(insightMapper::toResponse)
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredInsights.size());

        return new PageImpl<>(
                filteredInsights.subList(start, Math.max(start, end)),
                pageable,
                filteredInsights.size()
        );
    }

    @Transactional(readOnly = true)
    public InsightResponse getInsightById(UUID id) {
        log.info("Fetching insight with id: {}", id);

        return insightRepository.findById(id)
                .map(insightMapper::toResponse)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public long countInsightsByCompanyId(UUID companyId) {
        return insightRepository.findByCompanyId(companyId).size();
    }

    @Transactional
    public void deleteInsight(UUID id) {
        log.info("Deleting insight with id: {}", id);
        insightRepository.deleteById(id);
    }
}