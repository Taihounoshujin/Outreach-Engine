package com.rushil.outreach.enrollment;

import java.util.Set;

public enum EnrollmentStatus {

    PENDING,
    ACTIVE,
    COMPLETED,
    REPLIED,
    BOUNCED,
    UNSUBSCRIBED,
    FAILED;

    private static final Set<EnrollmentStatus> TERMINAL =
            Set.of(COMPLETED, REPLIED, BOUNCED, UNSUBSCRIBED, FAILED);

    public boolean isTerminal() {
        return TERMINAL.contains(this);
    }
}