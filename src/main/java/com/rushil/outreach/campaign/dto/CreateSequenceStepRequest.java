package com.rushil.outreach.campaign.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateSequenceStepRequest(

        @Min(1)
        int stepOrder,

        @Min(0)
        int delayDays,

        @NotBlank
        String subjectTemplate,

        @NotBlank
        String bodyTemplate) {
}