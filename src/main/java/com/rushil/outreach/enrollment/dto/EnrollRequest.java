package com.rushil.outreach.enrollment.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record EnrollRequest(

        @NotEmpty
        List<UUID> leadIds) {
}