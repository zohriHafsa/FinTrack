package com.fintrack.company_service.controller;

import com.fintrack.company_service.model.Company;
import com.fintrack.company_service.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final CompanyRepository companyRepository;

    @GetMapping("/companies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Company>> getAllCompanies() {
        log.info("Admin: fetching all companies");
        return ResponseEntity.ok(companyRepository.findAll());
    }

    @DeleteMapping("/companies/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
        log.info("Admin: deleting company {}", id);
        companyRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}