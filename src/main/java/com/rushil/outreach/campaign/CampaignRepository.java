package com.rushil.outreach.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findAllByOwnerId(UUID ownerId);

    Optional<Campaign> findByIdAndOwnerId(UUID id, UUID ownerId);
}