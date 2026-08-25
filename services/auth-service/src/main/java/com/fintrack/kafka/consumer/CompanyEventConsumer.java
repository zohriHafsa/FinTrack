package com.fintrack.kafka.consumer;

import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompanyEventConsumer {

    private final UserRepository userRepository;
    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @KafkaListener(
            topics = "company.created",
            groupId = "auth-service-group",
            containerFactory = "companyCreatedContainerFactory"
    )
    @Transactional
    public void handleCompanyCreated(Map<String, Object> event) {
        try {
            log.info("Received company.created event: {}", event);

            String userId = (String) event.get("userId");
            String companyId = (String) event.get("companyId");

            if (userId == null || companyId == null) {
                log.error("Invalid event - missing userId or companyId");
                return;
            }

            UUID userUUID = UUID.fromString(userId);
            UUID companyUUID = UUID.fromString(companyId);

            userRepository.findById(userUUID).ifPresent(user -> {
                user.setCompanyId(companyUUID);
                userRepository.save(user);
                log.info("Updated companyId in DB for user: {}", userId);


                try {
                    var userResource = keycloak.realm(realm)
                            .users()
                            .get(user.getKeycloakId());

                    var keycloakUser = userResource.toRepresentation();

                    Map<String, List<String>> attributes = keycloakUser.getAttributes() != null
                            ? new HashMap<>(keycloakUser.getAttributes())
                            : new HashMap<>();
                    attributes.put("company_id", List.of(companyId));
                    keycloakUser.setAttributes(attributes);

                    userResource.update(keycloakUser);

                    log.info("Updated company_id attribute in Keycloak for user: {}", userId);

                } catch (Exception e) {
                    log.error("Failed to update Keycloak attribute for user {}: {}", userId, e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            log.error("Error processing company.created event: {}", e.getMessage(), e);
        }
    }
}