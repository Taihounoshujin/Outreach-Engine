package com.rushil.outreach.suppression;

import com.rushil.outreach.common.exception.ResourceNotFoundException;
import com.rushil.outreach.enrollment.Enrollment;
import com.rushil.outreach.enrollment.EnrollmentRepository;
import com.rushil.outreach.enrollment.EnrollmentStatus;
import com.rushil.outreach.sending.EmailSend;
import com.rushil.outreach.sending.EmailSendRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UnsubscribeService {

    private static final Logger log = LoggerFactory.getLogger(UnsubscribeService.class);

    private final EmailSendRepository sends;
    private final EnrollmentRepository enrollments;
    private final SuppressionService suppression;

    public UnsubscribeService(EmailSendRepository sends, EnrollmentRepository enrollments,
                              SuppressionService suppression) {
        this.sends = sends;
        this.enrollments = enrollments;
        this.suppression = suppression;
    }

    @Transactional
    public void unsubscribeByToken(UUID sendId) {
        EmailSend send = sends.findById(sendId)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown unsubscribe token"));

        Enrollment source = send.getEnrollment();
        UUID ownerId = source.getCampaign().getOwner().getId();
        String email = send.getToEmail();

        suppression.suppress(ownerId, email, SuppressionReason.UNSUBSCRIBED);

        List<Enrollment> active = enrollments.findAllByLeadIdAndStatus(
                source.getLead().getId(), EnrollmentStatus.ACTIVE);

        for (Enrollment enrollment : active) {
            enrollment.markUnsubscribed();
        }
        log.info("Unsubscribed lead {} and stopped {} active enrollment(s)",
                source.getLead().getId(), active.size());
    }
}