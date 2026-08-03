package com.rushil.outreach.suppression;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SuppressionRepository extends JpaRepository<SuppressionEntry, UUID> {


    Page<SuppressionEntry> findAllByOwnerId(UUID ownerId, Pageable pageable);
}