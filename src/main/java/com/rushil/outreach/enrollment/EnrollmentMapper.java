package com.rushil.outreach.enrollment;

import com.rushil.outreach.enrollment.dto.EnrollmentResponse;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCampaign().getId(),
                enrollment.getLead().getId(),
                enrollment.getLead().getEmail(),
                enrollment.getStatus(),
                enrollment.getCurrentStepOrder(),
                enrollment.getNextActionAt(),
                enrollment.getEndReason());
    }
}