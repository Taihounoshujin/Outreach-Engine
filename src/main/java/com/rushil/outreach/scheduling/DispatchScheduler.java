package com.rushil.outreach.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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