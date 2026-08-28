package com.fintrack.service;

import com.fintrack.dto.request.InviteRequest;
import com.fintrack.dto.request.RegisterRequest;
import com.fintrack.dto.response.InvitePreviewResponse;
import com.fintrack.dto.response.InviteResponse;
import com.fintrack.dto.response.RegisterResponse;
import com.fintrack.dto.response.TeamMemberResponse;
import com.fintrack.exception.EmailAlreadyExistsException;
import com.fintrack.exception.InvalidInvitationException;
import com.fintrack.exception.UserNotFoundException;
import com.fintrack.kafka.producer.AuthEventProducer;
import com.fintrack.kafka.producer.CompanyRegistrationEvent;
import com.fintrack.model.Invitation;
import com.fintrack.model.InvitationStatus;
import com.fintrack.model.Role;
import com.fintrack.model.User;
import com.fintrack.repository.InvitationRepository;
import com.fintrack.repository.UserRepository;
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

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final Keycloak keycloak;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final AuthEventProducer authEventProducer;
    private final EmailService emailService;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "Email already exist" + request.getEmail()
            );
        }

        boolean isInvitedRegistration = request.getInviteToken() != null && !request.getInviteToken().isBlank();

        Invitation invitation = null;
        if (isInvitedRegistration) {
            invitation = invitationRepository.findByToken(request.getInviteToken())
                    .orElseThrow(() -> new InvalidInvitationException("This invitation link is invalid."));

            if (invitation.getStatus() != InvitationStatus.PENDING) {
                throw new InvalidInvitationException("This invitation has already been used or cancelled.");
            }
            if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new InvalidInvitationException("This invitation has expired. Ask the owner to send a new one.");
            }
            if (!invitation.getEmail().equalsIgnoreCase(request.getEmail())) {
                throw new InvalidInvitationException("Please register using the email address the invitation was sent to.");
            }
        } else {
            if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
                throw new InvalidInvitationException("Company name is required");
            }
            if (request.getSector() == null || request.getSector().isBlank()) {
                throw new InvalidInvitationException("Sector is required");
            }
        }

        Role role = isInvitedRegistration ? Role.ACCOUNTANT : Role.OWNER;

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
            String body = response.readEntity(String.class);
            log.error("Failed to create Keycloak user, status: {}, body: {}", response.getStatus(), body);
            if (response.getStatus() == 409) {
                throw new EmailAlreadyExistsException(
                        "This email is already in use by another account: " + request.getEmail());
            }
            throw new RuntimeException("Failed to create user in Keycloak (status " + response.getStatus() + ")");
        }

        String locationHeader = response.getHeaderString("Location");
        String keycloakId = locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
        log.info("Keycloak user created with id: {}", keycloakId);

        assignRealmRole(keycloakId, role.name());

        //Save user to our database
        User.UserBuilder userBuilder = User.builder()
                .keycloakId(keycloakId)
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(role);

        if (isInvitedRegistration) {
            userBuilder.companyId(invitation.getCompanyId());
        }

        User savedUser = userRepository.save(userBuilder.build());
        log.info("User saved to DB with id: {}", savedUser.getId());

        if (isInvitedRegistration) {
            setCompanyIdKeycloakAttribute(keycloakId, invitation.getCompanyId());

            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);

            return RegisterResponse.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole().name())
                    .companyId(invitation.getCompanyId())
                    .message("Registration successful. You've joined the team as an Accountant.")
                    .build();
        }

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

    @Transactional
    public InviteResponse invite(String ownerKeycloakId, InviteRequest request) {
        User owner = userRepository.findByKeycloakId(ownerKeycloakId)
                .orElseThrow(() -> new UserNotFoundException("No account found for this login."));

        if (owner.getCompanyId() == null) {
            throw new InvalidInvitationException("Your company is still being set up. Please try again shortly.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new InvalidInvitationException("This email is already registered.");
        }
        invitationRepository.findByEmailAndCompanyIdAndStatus(
                request.getEmail(), owner.getCompanyId(), InvitationStatus.PENDING
        ).ifPresent(existing -> {
            throw new InvalidInvitationException("An invitation is already pending for this email.");
        });

        Invitation invitation = Invitation.builder()
                .companyId(owner.getCompanyId())
                .invitedBy(owner.getId())
                .name(request.getName())
                .email(request.getEmail())
                .token(generateToken())
                .status(InvitationStatus.PENDING)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        Invitation saved = invitationRepository.save(invitation);

        String acceptUrl = frontendUrl + "/register?inviteToken=" + saved.getToken();
        String inviterName = (owner.getFirstName() != null ? owner.getFirstName() : owner.getEmail());
        emailService.sendInvitationEmail(request.getEmail(), inviterName, acceptUrl);

        return InviteResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .status(saved.getStatus().name())
                .message("Invitation sent to " + request.getEmail())
                .build();
    }

    public InvitePreviewResponse previewInvite(String token) {
        return invitationRepository.findByToken(token)
                .map(invitation -> {
                    if (invitation.getStatus() != InvitationStatus.PENDING) {
                        return InvitePreviewResponse.builder()
                                .valid(false)
                                .message("This invitation has already been used or cancelled.")
                                .build();
                    }
                    if (invitation.getExpiresAt().isBefore(LocalDateTime.now())) {
                        return InvitePreviewResponse.builder()
                                .valid(false)
                                .message("This invitation has expired.")
                                .build();
                    }
                    return InvitePreviewResponse.builder()
                            .valid(true)
                            .email(invitation.getEmail())
                            .message("Valid invitation")
                            .build();
                })
                .orElse(InvitePreviewResponse.builder()
                        .valid(false)
                        .message("This invitation link is invalid.")
                        .build());
    }

    @Transactional
    public List<TeamMemberResponse> getTeamMembers(String ownerKeycloakId, UUID companyId) {
        User owner = userRepository.findByKeycloakId(ownerKeycloakId)
                .orElseThrow(() -> new UserNotFoundException("No account found for this login."));
        if (!companyId.equals(owner.getCompanyId())) {
            throw new InvalidInvitationException("You do not have access to this company's team.");
        }

        List<TeamMemberResponse> members = userRepository.findByCompanyId(companyId).stream()
                .map(user -> TeamMemberResponse.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .role(user.getRole().name())
                        .status("ACTIVE")
                        .companyId(user.getCompanyId())
                        .build())
                .collect(Collectors.toList());

        List<TeamMemberResponse> pendingInvites = invitationRepository
                .findByCompanyIdAndStatus(companyId, InvitationStatus.PENDING).stream()
                .filter(inv -> inv.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(inv -> TeamMemberResponse.builder()
                        .id(inv.getId())
                        .firstName(inv.getName())
                        .lastName("")
                        .email(inv.getEmail())
                        .role(Role.ACCOUNTANT.name())
                        .status("INVITED")
                        .companyId(inv.getCompanyId())
                        .build())
                .collect(Collectors.toList());

        members.addAll(pendingInvites);
        return members;
    }

    @Transactional
    public void removeMember(String ownerKeycloakId, UUID companyId, UUID memberId) {
        User owner = userRepository.findByKeycloakId(ownerKeycloakId)
                .orElseThrow(() -> new UserNotFoundException("No account found for this login."));
        if (!companyId.equals(owner.getCompanyId())) {
            throw new InvalidInvitationException("You do not have access to this company's team.");
        }

        var userOpt = userRepository.findById(memberId);
        if (userOpt.isPresent()) {
            User member = userOpt.get();
            if (!companyId.equals(member.getCompanyId())) {
                throw new InvalidInvitationException("This member does not belong to your company.");
            }
            if (member.getRole() == Role.OWNER) {
                throw new InvalidInvitationException("The company owner cannot be removed.");
            }

            member.setCompanyId(null);
            userRepository.save(member);

            try {
                var userResource = keycloak.realm(realm).users().get(member.getKeycloakId());
                var rep = userResource.toRepresentation();
                rep.setEnabled(false);
                userResource.update(rep);
            } catch (Exception e) {
                log.error("Failed to disable Keycloak account for removed member {}: {}", memberId, e.getMessage(), e);
            }
            return;
        }

        var invitationOpt = invitationRepository.findById(memberId);
        if (invitationOpt.isPresent() && companyId.equals(invitationOpt.get().getCompanyId())) {
            Invitation invitation = invitationOpt.get();
            invitation.setStatus(InvitationStatus.CANCELLED);
            invitationRepository.save(invitation);
            return;
        }

        throw new UserNotFoundException("Team member or invitation not found.");
    }

    private void assignRealmRole(String keycloakId, String roleName) {
        try {
            RoleRepresentation role = keycloak.realm(realm).roles().get(roleName).toRepresentation();
            keycloak.realm(realm).users().get(keycloakId).roles().realmLevel().add(List.of(role));
        } catch (Exception e) {
            log.error("Failed to assign realm role '{}' in Keycloak: {}", roleName, e.getMessage(), e);
            throw new RuntimeException("Role '" + roleName + "' is not configured in Keycloak. " +
                    "Create it under Realm roles before inviting/registering.", e);
        }
    }

    private void setCompanyIdKeycloakAttribute(String keycloakId, UUID companyId) {
        try {
            var userResource = keycloak.realm(realm).users().get(keycloakId);
            var keycloakUser = userResource.toRepresentation();

            Map<String, List<String>> attributes = keycloakUser.getAttributes() != null
                    ? new HashMap<>(keycloakUser.getAttributes())
                    : new HashMap<>();
            attributes.put("company_id", List.of(companyId.toString()));
            keycloakUser.setAttributes(attributes);

            userResource.update(keycloakUser);
            log.info("Set company_id attribute in Keycloak for user: {}", keycloakId);
        } catch (Exception e) {
            log.error("Failed to set Keycloak company_id attribute for user {}: {}", keycloakId, e.getMessage(), e);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
