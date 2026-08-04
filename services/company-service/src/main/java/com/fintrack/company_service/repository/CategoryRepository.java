package com.fintrack.company_service.repository;

import com.fintrack.company_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByCompanyId(UUID companyId);
}
