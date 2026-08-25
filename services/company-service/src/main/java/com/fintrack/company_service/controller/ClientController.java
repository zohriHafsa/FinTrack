package com.fintrack.company_service.controller;

import com.fintrack.company_service.dto.request.CreateClientRequest;
import com.fintrack.company_service.dto.request.UpdateClientRequest;
import com.fintrack.company_service.dto.response.ClientResponse;
import com.fintrack.company_service.service.ClientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/companies/{companyId}/clients")
@RequiredArgsConstructor
@Slf4j
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateClientRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clientService.createClient(companyId, request));
    }

    @GetMapping
    public ResponseEntity<List<ClientResponse>> getAllClients(
            @PathVariable UUID companyId) {
        return ResponseEntity.ok(clientService.getAllClients(companyId));
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> getClientById(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId) {
        return ResponseEntity.ok(clientService.getClientById(companyId, clientId));
    }

    @PutMapping("/{clientId}")
    public ResponseEntity<ClientResponse> updateClient(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId,
            @Valid @RequestBody UpdateClientRequest request) {
        return ResponseEntity.ok(clientService.updateClient(companyId, clientId, request));
    }

    @DeleteMapping("/{clientId}")
    public ResponseEntity<Void> deleteClient(
            @PathVariable UUID companyId,
            @PathVariable UUID clientId) {
        clientService.deleteClient(companyId, clientId);
        return ResponseEntity.noContent().build();
    }
}