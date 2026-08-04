package com.rushil.outreach.campaign;

import com.rushil.outreach.campaign.dto.CampaignResponse;
import com.rushil.outreach.campaign.dto.CreateCampaignRequest;
import com.rushil.outreach.campaign.dto.CreateSequenceStepRequest;
import com.rushil.outreach.campaign.dto.SequenceStepResponse;
import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.common.security.CurrentUserService;
import com.rushil.outreach.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CampaignService {

    private final CampaignRepository campaigns;
    private final SequenceStepRepository steps;
    private final CampaignMapper mapper;
    private final CurrentUserService currentUser;

    public CampaignService(CampaignRepository campaigns, SequenceStepRepository steps,
                           CampaignMapper mapper, CurrentUserService currentUser) {
        this.campaigns = campaigns;
        this.steps = steps;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public CampaignResponse create(CreateCampaignRequest request) {
        User owner = currentUser.require();
        Campaign campaign = new Campaign(owner, request.name(), request.fromName(), request.fromEmail());
        if (request.dailyCap() != null) {
            campaign.setDailyCap(request.dailyCap());
        }
        if (request.timeZone() != null) {
            campaign.setTimeZone(request.timeZone());
        }
        return mapper.toResponse(campaigns.save(campaign));
    }

    @Transactional(readOnly = true)
    public List<CampaignResponse> listMine() {
        return campaigns.findAllByOwnerId(currentUser.requireId()).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CampaignResponse get(UUID id) {
        return mapper.toResponse(requireOwned(id));
    }

    @Transactional
    public CampaignResponse start(UUID id) {
        Campaign campaign = requireOwned(id);
        if (steps.findAllByCampaignIdOrderByStepOrderAsc(id).isEmpty()) {
            throw new IllegalStateException("Cannot start a campaign with no sequence steps");
        }
        campaign.start();
        return mapper.toResponse(campaign);
    }

    @Transactional
    public CampaignResponse pause(UUID id) {
        Campaign campaign = requireOwned(id);
        campaign.pause();
        return mapper.toResponse(campaign);
    }

    @Transactional
    public SequenceStepResponse addStep(UUID campaignId, CreateSequenceStepRequest request) {
        Campaign campaign = requireOwned(campaignId);
        SequenceStep step = new SequenceStep(
                campaign,
                request.stepOrder(),
                request.delayDays(),
                request.subjectTemplate(),
                request.bodyTemplate());
        return mapper.toResponse(steps.save(step));
    }

    @Transactional(readOnly = true)
    public List<SequenceStepResponse> listSteps(UUID campaignId) {
        requireOwned(campaignId);
        return steps.findAllByCampaignIdOrderByStepOrderAsc(campaignId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Loads a campaign scoped to the caller in a single query.
     *
     * <p>Note the shape: the owner is part of the lookup, not a check applied after
     * loading. A row belonging to someone else is never materialised at all, so
     * there is no window in which the wrong object exists in memory waiting for a
     * guard clause that a future refactor might drop.
     */
    private Campaign requireOwned(UUID campaignId) {
        return campaigns.findByIdAndOwnerId(campaignId, currentUser.requireId())
                .orElseThrow(() -> new ResourceNotFoundException("Campaign not found: " + campaignId));
    }
}