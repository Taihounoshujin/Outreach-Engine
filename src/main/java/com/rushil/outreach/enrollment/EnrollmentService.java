package com.rushil.outreach.enrollment;

import com.rushil.outreach.campaign.Campaign;
import com.rushil.outreach.campaign.CampaignRepository;
import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.campaign.SequenceStepRepository;
import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.common.security.CurrentUserService;
import com.rushil.outreach.enrollment.dto.EnrollmentResponse;
import com.rushil.outreach.lead.Lead;
import com.rushil.outreach.lead.LeadRepository;
import com.rushil.outreach.suppression.SuppressionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollments;
    private final CampaignRepository campaigns;
    private final SequenceStepRepository steps;
    private final LeadRepository leads;
    private final SuppressionService suppression;
    private final EnrollmentMapper mapper;
    private final CurrentUserService currentUser;

    public EnrollmentService(EnrollmentRepository enrollments, CampaignRepository campaigns,
                             SequenceStepRepository steps, LeadRepository leads,
                             SuppressionService suppression, EnrollmentMapper mapper,
                             CurrentUserService currentUser) {
        this.enrollments = enrollments;
        this.campaigns = campaigns;
        this.steps = steps;
        this.leads = leads;
        this.suppression = suppression;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public List<EnrollmentResponse> enroll(UUID campaignId, List<UUID> leadIds) {
        UUID ownerId = currentUser.requireId();
        Campaign campaign = campaigns.findByIdAndOwnerId(campaignId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        SequenceStep firstStep = steps.findByCampaignIdAndStepOrder(campaignId, 1)
                .orElseThrow(() -> new IllegalStateException(
                        "Campaign has no step 1 — add a sequence step before enrolling leads"));

        Instant firstActionAt = Instant.now().plus(Duration.ofDays(firstStep.getDelayDays()));
        List<EnrollmentResponse> created = new ArrayList<>();

        for (UUID leadId : leadIds) {
            Lead lead = leads.findByIdAndOwnerId(leadId, ownerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + leadId));

            if (suppression.isSuppressed(ownerId, lead.getEmail())) {
                log.info("Skipping enrollment of suppressed address for lead {}", leadId);
                continue;
            }
            if (enrollments.existsByCampaignIdAndLeadId(campaignId, leadId)) {
                continue;
            }

            Enrollment enrollment = new Enrollment(campaign, lead);
            enrollment.start(firstActionAt);
            created.add(mapper.toResponse(enrollments.save(enrollment)));
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> listForCampaign(UUID campaignId) {
        campaigns.findByIdAndOwnerId(campaignId, currentUser.requireId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
        return enrollments.findAllByCampaignId(campaignId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional
    public EnrollmentResponse markReplied(UUID campaignId, UUID enrollmentId) {
        campaigns.findByIdAndOwnerId(campaignId, currentUser.requireId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));

        Enrollment enrollment = enrollments.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found: " + enrollmentId));

        if (!enrollment.getCampaign().getId().equals(campaignId)) {
            throw new ResourceNotFoundException("Enrollment not found: " + enrollmentId);
        }
        enrollment.markReplied();
        return mapper.toResponse(enrollment);
    }
}