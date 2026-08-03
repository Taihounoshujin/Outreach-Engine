package com.rushil.outreach.common;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

import java.time.Instant;
import java.util.UUID;

/**
 * For tables that record when a row was created but not when it was last touched.
 *
 * <p>Sends, suppression entries, and sequence steps are log-like: they are written
 * once and their history matters more than their latest edit, so the schema gives
 * them {@code created_at} only. Extending {@link BaseEntity} here would map an
 * {@code updated_at} column that does not exist and fail Hibernate's schema
 * validation on startup.
 */
@MappedSuperclass
public abstract class CreatedEntity {

    @Id
    @GeneratedValue
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}