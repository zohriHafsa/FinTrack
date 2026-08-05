package com.fintrack.company_service.service;

import com.fintrack.company_service.kafka.consumer.CompanyRegistrationEvent;
import com.fintrack.company_service.model.Company;
import com.fintrack.company_service.model.Sector;
import com.fintrack.company_service.repository.CompanyRepository;
import com.fintrack.company_service.repository.SectorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final SectorRepository sectorRepository;

    @Autowired
    private CategoryService categoryService;

    @Transactional
    public void createCompanyFromEvent(CompanyRegistrationEvent event) {
        String normalizedSector = normalizeSector(event.getSector());

        sectorRepository.findByName(normalizedSector)
                .orElseGet(() -> sectorRepository.save(
                        Sector.builder().name(normalizedSector).build()
                ));

        Company company = Company.builder()
                .name(event.getCompanyName())
                .sector(normalizedSector)
                .ownerId(event.getUserId())
                .build();

        Company saved = companyRepository.save(company);
        log.info("Company created successfully for owner: {}", event.getUserId());

        // Seed default categories for the new company
        categoryService.seedDefaultCategories(saved.getId());
    }

    public List<String> getAllSectors() {
        return sectorRepository.findAllByOrderByNameAsc()
                .stream()
                .map(Sector::getName)
                .toList();
    }

    //"BAKERY" or "bakery" or "bAkErY" → "Bakery"
    private String normalizeSector(String sector) {
        if (sector == null || sector.isBlank()) return "Other";
        String trimmed = sector.trim().toLowerCase();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}
