package com.fintrack.company_service.repository;

import com.fintrack.company_service.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    List<Client> findByCompanyId(UUID companyId);
}
