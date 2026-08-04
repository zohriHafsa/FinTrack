package com.fintrack.company_service.repository;

import com.fintrack.company_service.model.Sector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectorRepository extends JpaRepository<Sector, UUID> {
    Optional<Sector> findByName(String name);
    List<Sector> findAllByOrderByNameAsc();

}
