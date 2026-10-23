package com.rushil.outreach.campaign.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCampaignRequest(

        @NotBlank @Size(max = 160)
        String name,

        @NotBlank @Size(max = 120)
        String fromName,

        @NotBlank @Email @Size(max = 320)
        String fromEmail,

        @Min(1) @Max(200)
        Integer dailyCap,

        @Size(max = 64)
        String timeZone) {
}