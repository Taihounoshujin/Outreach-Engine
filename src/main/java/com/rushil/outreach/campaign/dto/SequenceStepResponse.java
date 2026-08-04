package com.rushil.outreach.campaign.dto;

import java.util.UUID;

public record SequenceStepResponse(
        UUID id,
        int stepOrder,
        int delayDays,
        String subjectTemplate,
        String bodyTemplate) {
}