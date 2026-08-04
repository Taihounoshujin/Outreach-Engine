package com.rushil.outreach.enrollment.dto;

import com.rushil.outreach.enrollment.EnrollmentStatus;

import java.time.Instant;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID campaignId,
        UUID leadId,
        String leadEmail,
        EnrollmentStatus status,
        int currentStepOrder,
        Instant nextActionAt,
        String endReason) {
}