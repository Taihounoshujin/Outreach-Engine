package com.rushil.outreach.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    @Query(value = """
            select * from enrollment e
            where e.status = 'ACTIVE'
              and e.next_action_at is not null
              and e.next_action_at <= now()
            order by e.next_action_at
            limit :batchSize
            for update skip locked
            """, nativeQuery = true)
    List<Enrollment> claimDueEnrollments(@Param("batchSize") int batchSize);

    List<Enrollment> findAllByCampaignId(UUID campaignId);

    boolean existsByCampaignIdAndLeadId(UUID campaignId, UUID leadId);

    List<Enrollment> findAllByLeadIdAndStatus(UUID leadId, EnrollmentStatus status);
}