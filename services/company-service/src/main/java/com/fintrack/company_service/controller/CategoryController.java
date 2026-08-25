package com.fintrack.company_service.controller;

import com.fintrack.company_service.dto.request.CreateCategoryRequest;
import com.fintrack.company_service.dto.request.UpdateCategoryRequest;
import com.fintrack.company_service.dto.response.CategoryResponse;
import com.fintrack.company_service.model.CategoryType;
import com.fintrack.company_service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies/{companyId}/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(companyId, request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @PathVariable UUID companyId,
            @RequestParam(required = false) CategoryType type) {
        if (type != null) {
            return ResponseEntity.ok(categoryService.getCategoriesByType(companyId, type));
        }
        return ResponseEntity.ok(categoryService.getAllCategories(companyId));
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable UUID companyId,
            @PathVariable UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(companyId, categoryId));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable UUID companyId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(companyId, categoryId, request));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable UUID companyId,
            @PathVariable UUID categoryId) {
        categoryService.deleteCategory(companyId, categoryId);
        return ResponseEntity.noContent().build();
    }
}