package com.fintrack.company_service.mapper;

import com.fintrack.company_service.dto.request.CreateClientRequest;
import com.fintrack.company_service.dto.response.ClientResponse;
import com.fintrack.company_service.model.Client;

import java.util.UUID;

public class ClientMapper {
    public static Client toEntity(CreateClientRequest request, UUID companyId) {
        return Client.builder()
                .companyId(companyId)
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .build();
    }

    public static ClientResponse toResponse(Client client) {
        return ClientResponse.builder()
                .id(client.getId())
                .companyId(client.getCompanyId())
                .name(client.getName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .createdAt(client.getCreatedAt())
                .build();
    }
}
