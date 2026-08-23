package com.fintrack.ai_insights_service.dto.response;

import com.fintrack.ai_insights_service.model.InsightType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsightResponse {

    private UUID id;

    @JsonProperty("type")
    private InsightType type;

    private String title;

    private String content;

    @JsonProperty("generated_at")
    private LocalDateTime generatedAt;

    @JsonProperty("valid_until")
    private LocalDateTime validUntil;
}