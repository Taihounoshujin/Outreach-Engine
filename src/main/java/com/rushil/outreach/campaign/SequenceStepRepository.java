package com.rushil.outreach.campaign;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SequenceStepRepository extends JpaRepository<SequenceStep, UUID> {

    Optional<SequenceStep> findByCampaignIdAndStepOrder(UUID campaignId, int stepOrder);

    List<SequenceStep> findAllByCampaignIdOrderByStepOrderAsc(UUID campaignId);
}