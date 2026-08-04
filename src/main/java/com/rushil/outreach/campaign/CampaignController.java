package com.rushil.outreach.campaign;

import com.rushil.outreach.campaign.dto.CampaignResponse;
import com.rushil.outreach.campaign.dto.CreateCampaignRequest;
import com.rushil.outreach.campaign.dto.CreateSequenceStepRequest;
import com.rushil.outreach.campaign.dto.SequenceStepResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campaigns")
@Tag(name = "Campaigns", description = "Create campaigns and define their email sequences")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PostMapping
    @Operation(summary = "Create a campaign")
    public ResponseEntity<CampaignResponse> create(@Valid @RequestBody CreateCampaignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.create(request));
    }

    @GetMapping
    @Operation(summary = "List your campaigns")
    public List<CampaignResponse> list() {
        return campaignService.listMine();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one campaign")
    public CampaignResponse get(@PathVariable UUID id) {
        return campaignService.get(id);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start sending. Requires at least one sequence step.")
    public CampaignResponse start(@PathVariable UUID id) {
        return campaignService.start(id);
    }

    @PostMapping("/{id}/pause")
    @Operation(summary = "Pause sending. In-flight enrollments stop advancing.")
    public CampaignResponse pause(@PathVariable UUID id) {
        return campaignService.pause(id);
    }

    @PostMapping("/{id}/steps")
    @Operation(summary = "Add an email to the sequence. Supports {{contact_name}}, {{company_name}}, {{industry}}, {{city}}.")
    public ResponseEntity<SequenceStepResponse> addStep(
            @PathVariable UUID id,
            @Valid @RequestBody CreateSequenceStepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(campaignService.addStep(id, request));
    }

    @GetMapping("/{id}/steps")
    @Operation(summary = "List the campaign's sequence steps in order")
    public List<SequenceStepResponse> listSteps(@PathVariable UUID id) {
        return campaignService.listSteps(id);
    }
}