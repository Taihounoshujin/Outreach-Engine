package com.rushil.outreach.enrollment;

import com.rushil.outreach.enrollment.dto.EnrollRequest;
import com.rushil.outreach.enrollment.dto.EnrollmentResponse;
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
@RequestMapping("/api/campaigns/{campaignId}/enrollments")
@Tag(name = "Enrollments", description = "Put leads into a campaign and watch them progress")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @Operation(summary = "Enroll leads. Suppressed and already-enrolled leads are skipped.")
    public ResponseEntity<List<EnrollmentResponse>> enroll(
            @PathVariable UUID campaignId,
            @Valid @RequestBody EnrollRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollmentService.enroll(campaignId, request.leadIds()));
    }

    @GetMapping
    @Operation(summary = "List enrollments and their current status")
    public List<EnrollmentResponse> list(@PathVariable UUID campaignId) {
        return enrollmentService.listForCampaign(campaignId);
    }

    @PostMapping("/{enrollmentId}/replied")
    @Operation(summary = "Manually stop a sequence because the lead replied")
    public EnrollmentResponse markReplied(
            @PathVariable UUID campaignId,
            @PathVariable UUID enrollmentId) {
        return enrollmentService.markReplied(campaignId, enrollmentId);
    }
}