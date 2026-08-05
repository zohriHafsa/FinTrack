package com.fintrack.company_service.dto.response;

import com.fintrack.company_service.model.CategoryType;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private UUID companyId;
    private String name;
    private CategoryType type;
}