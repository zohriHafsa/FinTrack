package com.fintrack.repository;

import com.fintrack.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, Integer> {
    List<Client> findByCompanyId(UUID companyId);
}
