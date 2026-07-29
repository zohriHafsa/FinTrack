package com.fintrack.controller;

import com.fintrack.service.CompanyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping("/sectors")
    public ResponseEntity<List<String>> getSectors() {
        return ResponseEntity.ok(companyService.getAllSectors());
    }
}
