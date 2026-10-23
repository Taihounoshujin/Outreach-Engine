package com.rushil.outreach.scheduling;

import com.rushil.outreach.AbstractIntegrationTest;
import com.rushil.outreach.campaign.Campaign;
import com.rushil.outreach.campaign.CampaignRepository;
import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.campaign.SequenceStepRepository;
import com.rushil.outreach.enrollment.Enrollment;
import com.rushil.outreach.enrollment.EnrollmentRepository;
import com.rushil.outreach.enrollment.EnrollmentStatus;
import com.rushil.outreach.lead.Lead;
import com.rushil.outreach.lead.LeadRepository;
import com.rushil.outreach.sending.EmailSend;
import com.rushil.outreach.sending.EmailSendRepository;
import com.rushil.outreach.sending.SendStatus;
import com.rushil.outreach.sending.port.EmailSender;
import com.rushil.outreach.sending.port.OutboundEmail;
import com.rushil.outreach.sending.port.SendOutcome;
import com.rushil.outreach.suppression.SuppressionReason;
import com.rushil.outreach.suppression.SuppressionService;
import com.rushil.outreach.user.User;
import com.rushil.outreach.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@Import(DispatchConcurrencyTest.TestSenderConfig.class)
class DispatchConcurrencyTest extends AbstractIntegrationTest {

    private static final int ENROLLMENT_COUNT = 20;

    @Autowired DispatchService dispatchService;
    @Autowired UserRepository users;
    @Autowired CampaignRepository campaigns;
    @Autowired SequenceStepRepository steps;
    @Autowired LeadRepository leads;
    @Autowired EnrollmentRepository enrollments;
    @Autowired EmailSendRepository sends;
    @Autowired RecordingEmailSender sender;
    @Autowired SuppressionService suppression;

    @BeforeEach
    void setUp() {
        sends.deleteAll();
        enrollments.deleteAll();
        steps.deleteAll();
        leads.deleteAll();
        campaigns.deleteAll();
        users.deleteAll();
        sender.reset();

        User owner = users.save(User.builder()
                .email("owner@test.local")
                .passwordHash("not-a-real-hash")
                .displayName("Test Owner")
                .build());

        Campaign campaign = new Campaign(owner, "Concurrency campaign", "Tester", "tester@test.local");
        campaign.start();
        campaigns.save(campaign);

        steps.save(new SequenceStep(campaign, 1, 0, "Hello {{company_name}}", "<p>Body</p>"));

        for (int i = 0; i < ENROLLMENT_COUNT; i++) {
            Lead lead = new Lead(owner, "prospect" + i + "@example.com");
            lead.setCompanyName("Company " + i);
            leads.save(lead);

            Enrollment enrollment = new Enrollment(campaign, lead);
            enrollment.start(Instant.now().minusSeconds(60));
            enrollments.save(enrollment);
        }
    }

    @Test
    @DisplayName("two schedulers running at once send each enrollment exactly once")
    void concurrentDispatchersDoNotDoubleSend() throws Exception {
        int workers = 2;
        CountDownLatch startGate = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        List<Future<Integer>> results = new ArrayList<>();

        for (int i = 0; i < workers; i++) {
            results.add(pool.submit(() -> {
                startGate.await();
                return dispatchService.processDueBatch();
            }));
        }

        startGate.countDown();
        pool.shutdown();
        assertThat(pool.awaitTermination(30, TimeUnit.SECONDS)).isTrue();

        int totalProcessed = 0;
        for (Future<Integer> result : results) {
            totalProcessed += result.get();
        }

        assertThat(totalProcessed)
                .as("every enrollment claimed exactly once across both workers")
                .isEqualTo(ENROLLMENT_COUNT);

        List<EmailSend> allSends = sends.findAll();
        assertThat(allSends).hasSize(ENROLLMENT_COUNT);
        assertThat(allSends).allSatisfy(send ->
                assertThat(send.getStatus()).isEqualTo(SendStatus.SENT));

        assertThat(sender.recipients())
                .as("no address received the same step twice")
                .doesNotHaveDuplicates()
                .hasSize(ENROLLMENT_COUNT);
    }

    @Test
    @DisplayName("a suppressed lead is skipped and its enrollment stops")
    void suppressedLeadIsNotSent() {
        Enrollment target = enrollments.findAll().get(0);
        String suppressedEmail = target.getLead().getEmail();
        UUID ownerId = target.getCampaign().getOwner().getId();

        suppression.suppress(ownerId, suppressedEmail, SuppressionReason.MANUAL);

        dispatchService.processDueBatch();

        assertThat(sender.recipients())
                .as("suppression gate blocked the send")
                .doesNotContain(suppressedEmail);

        Enrollment reloaded = enrollments.findById(target.getId()).orElseThrow();
        assertThat(reloaded.getStatus()).isEqualTo(EnrollmentStatus.UNSUBSCRIBED);
    }

    @TestConfiguration
    static class TestSenderConfig {

        @Bean
        RecordingEmailSender recordingEmailSender() {
            return new RecordingEmailSender();
        }
    }

    static class RecordingEmailSender implements EmailSender {

        private final List<String> recipients = new CopyOnWriteArrayList<>();

        @Override
        public SendOutcome send(OutboundEmail email) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            recipients.add(email.toEmail());
            return SendOutcome.accepted(UUID.randomUUID().toString());
        }

        List<String> recipients() {
            return List.copyOf(recipients);
        }

        void reset() {
            recipients.clear();
        }
    }
}