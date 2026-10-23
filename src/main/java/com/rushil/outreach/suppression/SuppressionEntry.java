package com.rushil.outreach.suppression;

import com.rushil.outreach.common.CreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "suppression")
public class SuppressionEntry extends CreatedEntity {

    @Column(name = "owner_id", nullable = false, updatable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 320, updatable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, updatable = false)
    private SuppressionReason reason;

    protected SuppressionEntry() {
    }

    public SuppressionEntry(UUID ownerId, String email, SuppressionReason reason) {
        this.ownerId = ownerId;
        this.email = email == null ? null : email.trim().toLowerCase();
        this.reason = reason;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getEmail() {
        return email;
    }

    public SuppressionReason getReason() {
        return reason;
    }
}