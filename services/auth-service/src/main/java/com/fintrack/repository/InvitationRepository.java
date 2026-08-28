package com.fintrack.repository;

import com.fintrack.model.Invitation;
import com.fintrack.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {
    Optional<Invitation> findByToken(String token);
    List<Invitation> findByCompanyIdAndStatus(UUID companyId, InvitationStatus status);
    Optional<Invitation> findByEmailAndCompanyIdAndStatus(String email, UUID companyId, InvitationStatus status);
}
