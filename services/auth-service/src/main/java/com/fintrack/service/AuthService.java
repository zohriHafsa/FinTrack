package com.fintrack.service;

import com.fintrack.dto.request.RegisterRequest;
import com.fintrack.dto.response.RegisterResponse;
import com.fintrack.exception.EmailAlreadyExistsException;
import com.fintrack.kafka.producer.CompanyRegistrationEvent;
import com.fintrack.model.Role;
import com.fintrack.model.User;
import com.fintrack.repository.UserRepository;
import com.fintrack.kafka.producer.AuthEventProducer;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;


import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;




@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;

    @Value("${keycloak.realm}")
    private String realm;

    @Transactional
    public RegisterResponse  register(RegisterRequest request) {

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exist" + request.getEmail()
            );
        }

        //build keycloak user representation
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setUsername(request.getEmail());
        keycloakUser.setEmail(request.getEmail());
        keycloakUser.setFirstName(request.getFirstName());
        keycloakUser.setLastName(request.getLastName());
        keycloakUser.setEnabled(true);
        keycloakUser.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        keycloakUser.setCredentials(Collections.singletonList(credential));

        //create user in keycloak
        Response response = keycloak
                .realm(realm)
                .users()
                .create(keycloakUser);
        if (response.getStatus() != 201) {
            log.error("Failed to create Keycloak user, status: {}", response.getStatus());
            throw new RuntimeException("Failed to create user in Keycloak");
        }

        String locationHeader = response.getHeaderString("Location");
        String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        log.info("Keycloak user created with id: {}", keycloakId);

        //
        RoleRepresentation ownerRole = keycloak
                .realm(realm)
                .roles()
                .get("OWNER")
                .toRepresentation();

        keycloak.realm(realm)
                .users()
                .get(keycloakId)
                .roles()
                .realmLevel()
                .add(List.of(ownerRole));

        //Save user to our database
        User user = User.builder()
                .keycloakId(keycloakId)
                .email(request.getEmail())
                .role(Role.OWNER)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User saved to DB with id: {}", savedUser.getId());

        //publish Kafka event for company-service
        CompanyRegistrationEvent event = CompanyRegistrationEvent.builder()
                .userId(savedUser.getId())
                .email(request.getEmail())
                .companyName(request.getCompanyName())
                .sector(request.getSector())
                .build();

        authEventProducer.publishCompanyRegistration(event);

        return RegisterResponse.builder()
                .userId(savedUser.getId())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .message("Registration successful. Company setup in progress.")
                .build();
    }
}
