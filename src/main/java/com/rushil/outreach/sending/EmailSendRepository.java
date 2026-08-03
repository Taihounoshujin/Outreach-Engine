package com.rushil.outreach.sending;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailSendRepository extends JpaRepository<EmailSend, UUID> {

    Optional<EmailSend> findByEnrollmentIdAndStepId(UUID enrollmentId, UUID stepId);

    Optional<EmailSend> findByProviderMessageId(String providerMessageId);

    long countByEnrollmentCampaignIdAndStatus(UUID campaignId, SendStatus status);
}