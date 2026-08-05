package com.fintrack.company_service.service;

import com.fintrack.company_service.dto.request.CreateCategoryRequest;
import com.fintrack.company_service.dto.request.UpdateCategoryRequest;
import com.fintrack.company_service.dto.response.CategoryResponse;
import com.fintrack.company_service.exception.ResourceNotFoundException;
import com.fintrack.company_service.mapper.CategoryMapper;
import com.fintrack.company_service.model.Category;
import com.fintrack.company_service.model.CategoryType;
import com.fintrack.company_service.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(UUID companyId, CreateCategoryRequest request) {
        log.info("Creating category for company: {}", companyId);
        Category category = CategoryMapper.toEntity(request, companyId);
        Category saved = categoryRepository.save(category);
        return CategoryMapper.toResponse(saved);
    }

    public List<CategoryResponse> getAllCategories(UUID companyId) {
        return categoryRepository.findByCompanyId(companyId)
                .stream()
                .map(CategoryMapper::toResponse)
                .toList();
    }

    public List<CategoryResponse> getCategoriesByType(UUID companyId, CategoryType type) {
        return categoryRepository.findByCompanyId(companyId)
                .stream()
                .filter(c -> c.getType().equals(type))
                .map(CategoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse getCategoryById(UUID companyId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID companyId, UUID categoryId,
                                           UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));

        category.setName(request.getName());
        category.setType(request.getType());

        Category updated = categoryRepository.save(category);
        log.info("Updated category: {}", categoryId);
        return CategoryMapper.toResponse(updated);
    }

    @Transactional
    public void deleteCategory(UUID companyId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found with id: " + categoryId
                ));
        categoryRepository.delete(category);
        log.info("Deleted category: {}", categoryId);
    }

    @Transactional
    public void seedDefaultCategories(UUID companyId) {
        log.info("Seeding default categories for company: {}", companyId);
        List<Category> defaults = List.of(
                Category.builder().companyId(companyId).name("Rent").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Utilities").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Salaries").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Raw Materials").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Equipment").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Marketing").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Transport").type(CategoryType.EXPENSE).build(),
                Category.builder().companyId(companyId).name("Sales").type(CategoryType.INCOME).build(),
                Category.builder().companyId(companyId).name("Services").type(CategoryType.INCOME).build(),
                Category.builder().companyId(companyId).name("Other Income").type(CategoryType.INCOME).build()
        );
        categoryRepository.saveAll(defaults);
        log.info("Seeded {} default categories for company: {}", defaults.size(), companyId);
    }
}