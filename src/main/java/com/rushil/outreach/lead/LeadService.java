package com.rushil.outreach.lead;

import com.rushil.outreach.common.exception.EmailAlreadyExistsException;
import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.common.security.CurrentUserService;
import com.rushil.outreach.lead.dto.CreateLeadRequest;
import com.rushil.outreach.lead.dto.LeadResponse;
import com.rushil.outreach.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LeadService {

    private final LeadRepository leads;
    private final LeadMapper mapper;
    private final CurrentUserService currentUser;

    public LeadService(LeadRepository leads, LeadMapper mapper, CurrentUserService currentUser) {
        this.leads = leads;
        this.mapper = mapper;
        this.currentUser = currentUser;
    }

    @Transactional
    public LeadResponse create(CreateLeadRequest request) {
        User owner = currentUser.require();
        String email = request.email().trim().toLowerCase();

        if (leads.existsByOwnerIdAndEmail(owner.getId(), email)) {
            throw new EmailAlreadyExistsException("Lead already exists: " + email);
        }
        return mapper.toResponse(leads.save(apply(new Lead(owner, email), request)));
    }

    /**
     * Bulk create, skipping addresses already on file.
     *
     * <p>Skipping rather than failing the whole batch is the right behaviour for
     * imports: a 500-row CSV with three duplicates should load 497 leads, not
     * reject the file. Duplicates are silently ignored because re-importing a
     * list is a normal thing users do.
     */
    @Transactional
    public List<LeadResponse> createBatch(List<CreateLeadRequest> requests) {
        User owner = currentUser.require();
        return requests.stream()
                .map(request -> request.email().trim().toLowerCase())
                .distinct()
                .filter(email -> !leads.existsByOwnerIdAndEmail(owner.getId(), email))
                .map(email -> {
                    CreateLeadRequest source = requests.stream()
                            .filter(r -> r.email().trim().equalsIgnoreCase(email))
                            .findFirst()
                            .orElseThrow();
                    Lead lead = apply(new Lead(owner, email), source);
                    lead.setSource(LeadSource.CSV_IMPORT);
                    return mapper.toResponse(leads.save(lead));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> listMine(Pageable pageable) {
        return leads.findAllByOwnerId(currentUser.requireId(), pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public LeadResponse get(UUID id) {
        return mapper.toResponse(requireOwned(id));
    }

    @Transactional
    public void delete(UUID id) {
        leads.delete(requireOwned(id));
    }

    Lead requireOwned(UUID leadId) {
        return leads.findByIdAndOwnerId(leadId, currentUser.requireId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found: " + leadId));
    }

    private Lead apply(Lead lead, CreateLeadRequest request) {
        lead.setContactName(request.contactName());
        lead.setCompanyName(request.companyName());
        lead.setWebsite(request.website());
        lead.setIndustry(request.industry());
        lead.setCity(request.city());
        lead.setRegion(request.region());
        return lead;
    }
}