package com.fintrack.repository;

import com.fintrack.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, Integer> {
    boolean existsByOwnerId(UUID ownerId);
}
