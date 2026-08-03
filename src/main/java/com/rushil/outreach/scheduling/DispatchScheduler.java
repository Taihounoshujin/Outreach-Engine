package com.rushil.outreach.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Polls for due work on a fixed interval.
 *
 * <p>The tick itself is deliberately thin: it owns no logic beyond "run a batch and
 * don't die." All the interesting behaviour lives in {@link DispatchService}, which
 * makes the batch testable without waiting on a timer.
 *
 * <p>Concurrency is handled inside the claiming query rather than here, so running
 * several instances of this application is safe. That is a better shape than a
 * distributed lock like ShedLock, which would serialize the work — SKIP LOCKED lets
 * instances share the queue and work in parallel.
 */
@Component
@ConditionalOnProperty(name = "outreach.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DispatchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DispatchScheduler.class);

    private final DispatchService dispatchService;

    public DispatchScheduler(DispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @Scheduled(
            fixedDelayString = "${outreach.scheduler.interval-ms:60000}",
            initialDelayString = "${outreach.scheduler.initial-delay-ms:10000}")
    public void tick() {
        try {
            int processed = dispatchService.processDueBatch();
            if (processed > 0) {
                log.info("Dispatch tick processed {} enrollment(s)", processed);
            }
        } catch (Exception e) {
            // Never let an exception escape a scheduled method: Spring's default
            // scheduler silently stops re-running a task that throws, and the failure
            // is easy to miss until nothing has sent for a day.
            log.error("Dispatch tick failed", e);
        }
    }
}