package com.rushil.outreach.lead;

import com.rushil.outreach.common.BaseEntity;
import com.rushil.outreach.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "lead")
public class Lead extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @Column(nullable = false, length = 320)
    private String email;

    @Column(name = "contact_name", length = 160)
    private String contactName;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(length = 500)
    private String website;

    @Column(length = 120)
    private String industry;

    @Column(length = 120)
    private String city;

    @Column(length = 120)
    private String region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private LeadSource source = LeadSource.MANUAL;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> enrichment = new HashMap<>();

    protected Lead() {
    }

    public Lead(User owner, String email) {
        this.owner = owner;
        this.email = normalizeEmail(email);
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    private String normalizeEmail(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Lead email is required");
        }
        return value.trim().toLowerCase();
    }

    public User getOwner() {
        return owner;
    }

    public String getEmail() {
        return email;
    }

    public String getContactName() {
        return contactName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getWebsite() {
        return website;
    }

    public String getIndustry() {
        return industry;
    }

    public String getCity() {
        return city;
    }

    public String getRegion() {
        return region;
    }

    public LeadSource getSource() {
        return source;
    }

    public Map<String, Object> getEnrichment() {
        return enrichment;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setSource(LeadSource source) {
        this.source = source;
    }

    public void setEnrichment(Map<String, Object> enrichment) {
        this.enrichment = enrichment == null ? new HashMap<>() : enrichment;
    }
}