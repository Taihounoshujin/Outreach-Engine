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
     */
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
}