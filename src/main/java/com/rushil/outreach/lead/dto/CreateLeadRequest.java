package com.rushil.outreach.lead.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLeadRequest(

        @NotBlank @Email @Size(max = 320)
        String email,

        @Size(max = 160) String contactName,
        @Size(max = 200) String companyName,
        @Size(max = 500) String website,
        @Size(max = 120) String industry,
        @Size(max = 120) String city,
        @Size(max = 120) String region) {
}