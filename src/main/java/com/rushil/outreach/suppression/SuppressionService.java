package com.rushil.outreach.suppression;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SuppressionService {

    private final SuppressionRepository repository;

    public SuppressionService(SuppressionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isSuppressed(UUID ownerId, String email) {
        return repository.existsByOwnerIdAndEmail(ownerId, normalize(email));
    }

    @Transactional
    public void suppress(UUID ownerId, String email, SuppressionReason reason) {
        String normalized = normalize(email);
        if (repository.existsByOwnerIdAndEmail(ownerId, normalized)) {
            return;
        }
        repository.save(new SuppressionEntry(ownerId, normalized, reason));
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}