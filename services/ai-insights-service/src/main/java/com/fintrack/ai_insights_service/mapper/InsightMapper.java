package com.fintrack.ai_insights_service.mapper;

import com.fintrack.ai_insights_service.dto.response.InsightResponse;
import com.fintrack.ai_insights_service.model.Insight;
import org.springframework.stereotype.Component;

@Component
public class InsightMapper {

    public InsightResponse toResponse(Insight insight) {
        if (insight == null) {
            return null;
        }

        return InsightResponse.builder()
                .id(insight.getId())
                .type(insight.getType())
                .title(insight.getTitle())
                .content(insight.getContent())
                .generatedAt(insight.getGeneratedAt())
                .validUntil(insight.getValidUntil())
                .build();
    }
}