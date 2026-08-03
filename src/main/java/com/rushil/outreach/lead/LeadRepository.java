package com.rushil.outreach.lead;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Page<Lead> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Optional<Lead> findByIdAndOwnerId(UUID id, UUID ownerId);

    Optional<Lead> findByOwnerIdAndEmail(UUID ownerId, String email);

    boolean existsByOwnerIdAndEmail(UUID ownerId, String email);
}