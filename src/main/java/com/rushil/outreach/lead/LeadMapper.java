package com.rushil.outreach.lead;

import com.rushil.outreach.lead.dto.LeadResponse;
import org.springframework.stereotype.Component;

@Component
public class LeadMapper {

    public LeadResponse toResponse(Lead lead) {
        return new LeadResponse(
                lead.getId(),
                lead.getEmail(),
                lead.getContactName(),
                lead.getCompanyName(),
                lead.getWebsite(),
                lead.getIndustry(),
                lead.getCity(),
                lead.getRegion(),
                lead.getSource(),
                lead.getCreatedAt());
    }
}