package com.fintrack.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.UserProfileResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ProtocolMapperRepresentation;
import org.keycloak.representations.userprofile.config.UPAttribute;
import org.keycloak.representations.userprofile.config.UPAttributePermissions;
import org.keycloak.representations.userprofile.config.UPConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Ensures the "company_id" user attribute is (a) declared in the realm's declarative
 * User Profile schema and (b) mapped onto the JWT (access + ID token) issued by the
 * frontend's Keycloak client.
 * <p>
 * Keycloak 24's User Profile feature validates writes against a schema: any attribute
 * not declared there is silently dropped by the admin REST API on update (no error),
 * which is why {@code company_id} never persisted even though the Kafka consumer's
 * update call succeeded. This repo has no realm-import file, so both pieces are
 * provisioned idempotently on startup via the admin client instead of requiring a
 * manual click-through in the Keycloak admin console.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KeycloakCompanyIdMapperInitializer implements ApplicationRunner {

    private static final String MAPPER_NAME = "company_id";
    private static final String USER_ATTRIBUTE = "company_id";

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id:fintrack360-angular}")
    private String clientId;

    @Override
    public void run(ApplicationArguments args) {
        ensureUserProfileAttribute();
        ensureProtocolMapper();
    }

    private void ensureUserProfileAttribute() {
        try {
            UserProfileResource userProfileResource = keycloak.realm(realm).users().userProfile();
            UPConfig config = userProfileResource.getConfiguration();

            if (config.getAttribute(USER_ATTRIBUTE) != null) {
                log.info("User Profile attribute '{}' already declared in realm '{}'", USER_ATTRIBUTE, realm);
                return;
            }

            UPAttributePermissions permissions = new UPAttributePermissions();
            permissions.setView(Set.of("admin"));
            permissions.setEdit(Set.of("admin"));

            UPAttribute attribute = new UPAttribute(USER_ATTRIBUTE, permissions);
            attribute.setMultivalued(false);

            config.addOrReplaceAttribute(attribute);
            userProfileResource.update(config);

            log.info("Declared '{}' as an admin-managed User Profile attribute in realm '{}'", USER_ATTRIBUTE, realm);
        } catch (Exception e) {
            log.error("Failed to declare company_id in the realm's User Profile schema: {}", e.getMessage(), e);
        }
    }

    private void ensureProtocolMapper() {
        try {
            List<ClientRepresentation> clients = keycloak.realm(realm)
                    .clients()
                    .findByClientId(clientId);

            if (clients.isEmpty()) {
                log.warn("Keycloak client '{}' not found in realm '{}'; skipping company_id mapper setup", clientId, realm);
                return;
            }

            ClientResource clientResource = keycloak.realm(realm)
                    .clients()
                    .get(clients.get(0).getId());

            boolean mapperExists = clientResource.getProtocolMappers().getMappers().stream()
                    .anyMatch(mapper -> MAPPER_NAME.equals(mapper.getName()));

            if (mapperExists) {
                log.info("Keycloak protocol mapper '{}' already present on client '{}'", MAPPER_NAME, clientId);
                return;
            }

            ProtocolMapperRepresentation mapper = new ProtocolMapperRepresentation();
            mapper.setName(MAPPER_NAME);
            mapper.setProtocol("openid-connect");
            mapper.setProtocolMapper("oidc-usermodel-attribute-mapper");

            Map<String, String> config = new HashMap<>();
            config.put("user.attribute", USER_ATTRIBUTE);
            config.put("claim.name", "company_id");
            config.put("jsonType.label", "String");
            config.put("id.token.claim", "true");
            config.put("access.token.claim", "true");
            config.put("userinfo.token.claim", "true");
            config.put("multivalued", "false");
            mapper.setConfig(config);

            clientResource.getProtocolMappers().createMapper(mapper);
            log.info("Created Keycloak protocol mapper '{}' -> claim 'company_id' on client '{}'", MAPPER_NAME, clientId);

        } catch (Exception e) {
            log.error("Failed to provision company_id protocol mapper in Keycloak: {}", e.getMessage(), e);
        }
    }
}
