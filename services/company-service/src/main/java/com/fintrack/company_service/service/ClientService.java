package com.fintrack.company_service.service;

import com.fintrack.company_service.dto.request.CreateClientRequest;
import com.fintrack.company_service.dto.request.UpdateClientRequest;
import com.fintrack.company_service.dto.response.ClientResponse;
import com.fintrack.company_service.exception.ResourceNotFoundException;
import com.fintrack.company_service.mapper.ClientMapper;
import com.fintrack.company_service.model.Client;
import com.fintrack.company_service.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional
    public ClientResponse createClient(UUID companyId, CreateClientRequest request) {
        log.info("Creating client for company: {}", companyId);
        Client client = ClientMapper.toEntity(request, companyId);
        Client saved = clientRepository.save(client);
        return ClientMapper.toResponse(saved);
    }

    public List<ClientResponse> getAllClients(UUID companyId) {
        return clientRepository.findByCompanyId(companyId)
                .stream()
                .map(ClientMapper::toResponse)
                .toList();
    }

    public ClientResponse getClientById(UUID companyId, UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + clientId
                ));
        return ClientMapper.toResponse(client);
    }

    @Transactional
    public ClientResponse updateClient(UUID companyId, UUID clientId, UpdateClientRequest request) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + clientId
                ));

        client.setName(request.getName());
        client.setEmail(request.getEmail());
        client.setPhone(request.getPhone());

        Client updated = clientRepository.save(client);
        log.info("Updated client: {}", clientId);
        return ClientMapper.toResponse(updated);
    }

    @Transactional
    public void deleteClient(UUID companyId, UUID clientId) {
        Client client = clientRepository.findById(clientId)
                .filter(c -> c.getCompanyId().equals(companyId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + clientId
                ));
        clientRepository.delete(client);
        log.info("Deleted client: {}", clientId);
    }
}