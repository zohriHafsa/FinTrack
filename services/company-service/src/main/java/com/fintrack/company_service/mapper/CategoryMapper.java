package com.fintrack.company_service.mapper;

import com.fintrack.company_service.dto.request.CreateCategoryRequest;
import com.fintrack.company_service.dto.response.CategoryResponse;
import com.fintrack.company_service.model.Category;
import java.util.UUID;

public class CategoryMapper {

    public static Category toEntity(CreateCategoryRequest request, UUID companyId) {
        return Category.builder()
                .companyId(companyId)
                .name(request.getName())
                .type(request.getType())
                .build();
    }

    public static CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .companyId(category.getCompanyId())
                .name(category.getName())
                .type(category.getType())
                .build();
    }
}