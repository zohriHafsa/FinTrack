package com.fintrack.company_service.repository;

import com.fintrack.company_service.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    boolean existsByOwnerId(UUID ownerId);
}
