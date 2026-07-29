package com.fintrack.repository;

import com.fintrack.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    List<Category> findByCompanyId(UUID companyId);
}
