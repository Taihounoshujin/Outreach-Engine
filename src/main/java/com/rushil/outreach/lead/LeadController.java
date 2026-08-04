package com.rushil.outreach.lead;

import com.rushil.outreach.lead.dto.CreateLeadRequest;
import com.rushil.outreach.lead.dto.LeadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/leads")
@Tag(name = "Leads", description = "The prospects a campaign sends to")
public class LeadController {

    private final LeadService leadService;

    public LeadController(LeadService leadService) {
        this.leadService = leadService;
    }

    @PostMapping
    @Operation(summary = "Add a lead")
    public ResponseEntity<LeadResponse> create(@Valid @RequestBody CreateLeadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.create(request));
    }

    @PostMapping("/batch")
    @Operation(summary = "Add many leads at once. Addresses already on file are skipped.")
    public ResponseEntity<List<LeadResponse>> createBatch(
            @Valid @RequestBody List<CreateLeadRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED).body(leadService.createBatch(requests));
    }

    @GetMapping
    @Operation(summary = "List your leads")
    public Page<LeadResponse> list(@PageableDefault(size = 25) Pageable pageable) {
        return leadService.listMine(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one lead")
    public LeadResponse get(@PathVariable UUID id) {
        return leadService.get(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a lead. Does not remove them from the suppression list.")
    public void delete(@PathVariable UUID id) {
        leadService.delete(id);
    }
}