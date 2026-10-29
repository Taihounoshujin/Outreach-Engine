package com.rushil.outreach.enrollment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    /**
     * Claims a batch of enrollments whose next step is due.
     *
     * <p>{@code FOR UPDATE SKIP LOCKED} is the important part. Without it, two
     * scheduler instances running the same query both read the same rows and both
     * send — the lead gets the same email twice, which is the fastest route to a
     * spam complaint. With it, the second instance skips rows the first has locked
     * and picks up different work, so the schedulers scale horizontally instead of
     * colliding.
     *
     * <p>The locks are held for the life of the surrounding transaction, so callers
     * must be {@code @Transactional} and should keep the batch small enough that the
     * transaction stays short.
     *
     * <p>The join to campaign is what makes Pause work: only RUNNING campaigns send.
     * Enrollments in a paused (or draft, or archived) campaign stay ACTIVE with their
     * schedule intact and simply aren't picked up; resuming makes them eligible again.
     *
     * <p>{@code FOR UPDATE OF e} matters as much as the join. A plain FOR UPDATE on
     * a join locks rows in every joined table — here, the campaign row. A second
     * scheduler would then find that campaign row locked and SKIP LOCKED would skip
     * every enrollment in the campaign, serialising all its sends onto one worker.
     * Locking only the enrollment rows keeps workers sharing a campaign's queue.
     */
    @Query(value = """
            select e.* from enrollment e
            join campaign c on c.id = e.campaign_id
            where e.status = 'ACTIVE'
              and c.status = 'RUNNING'
              and e.next_action_at is not null
              and e.next_action_at <= now()
            order by e.next_action_at
            limit :batchSize
            for update of e skip locked
            """, nativeQuery = true)
    List<Enrollment> claimDueEnrollments(@Param("batchSize") int batchSize);

    List<Enrollment> findAllByCampaignId(UUID campaignId);

    boolean existsByCampaignIdAndLeadId(UUID campaignId, UUID leadId);

    List<Enrollment> findAllByLeadIdAndStatus(UUID leadId, EnrollmentStatus status);
}