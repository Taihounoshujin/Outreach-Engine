package com.rushil.outreach.common;

import com.rushil.outreach.campaign.Campaign;
import com.rushil.outreach.campaign.CampaignRepository;
import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.campaign.SequenceStepRepository;
import com.rushil.outreach.enrollment.Enrollment;
import com.rushil.outreach.enrollment.EnrollmentRepository;
import com.rushil.outreach.lead.Lead;
import com.rushil.outreach.lead.LeadRepository;
import com.rushil.outreach.user.User;
import com.rushil.outreach.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Profile("dev")
public class DevSeedRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSeedRunner.class);

    private static final String SEED_OWNER_EMAIL = "owner@localhost";

    private final UserRepository users;
    private final CampaignRepository campaigns;
    private final SequenceStepRepository steps;
    private final LeadRepository leads;
    private final EnrollmentRepository enrollments;

    public DevSeedRunner(UserRepository users, CampaignRepository campaigns,
                         SequenceStepRepository steps, LeadRepository leads,
                         EnrollmentRepository enrollments) {
        this.users = users;
        this.campaigns = campaigns;
        this.steps = steps;
        this.leads = leads;
        this.enrollments = enrollments;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (users.findByEmail(SEED_OWNER_EMAIL).isPresent()) {
            log.info("Seed data already present — skipping");
            return;
        }

        User owner = new User();
        owner.setEmail(SEED_OWNER_EMAIL);
        owner.setPasswordHash("{noop}seed-only-not-a-real-password");
        owner.setDisplayName("Seed Owner");
        users.save(owner);

        Campaign campaign = new Campaign(
                owner,
                "Local HVAC outreach",
                "Rushil",
                "rushil@localhost");
        campaign.start();
        campaigns.save(campaign);

        steps.save(new SequenceStep(campaign, 1, 0,
                "Quick question about {{company_name}}",
                """
                <p>Hi {{contact_name}},</p>
                <p>I came across {{company_name}} while looking at {{industry}} firms
                around {{city}}. I build tooling that helps small teams keep a steady
                flow of inbound conversations without hiring a sales rep.</p>
                <p>Worth a short call next week?</p>
                <p>— Rushil</p>
                """));

        steps.save(new SequenceStep(campaign, 2, 3,
                "Following up — {{company_name}}",
                """
                <p>Hi {{contact_name}},</p>
                <p>Circling back on this in case it got buried. Happy to close the
                loop either way.</p>
                <p>— Rushil</p>
                """));

        Lead lead = new Lead(owner, "prospect@example.com");
        lead.setContactName("Dana");
        lead.setCompanyName("Beacon Heating & Air");
        lead.setIndustry("HVAC");
        lead.setCity("Columbia");
        lead.setRegion("MO");
        leads.save(lead);

        Enrollment enrollment = new Enrollment(campaign, lead);
        enrollment.start(Instant.now());
        enrollments.save(enrollment);

        log.info("Seeded campaign {} with 1 lead — first send due now", campaign.getId());
    }
}