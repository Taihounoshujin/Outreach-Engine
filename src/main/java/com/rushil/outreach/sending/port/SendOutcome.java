package com.rushil.outreach.sending.port;

public record SendOutcome(boolean accepted, String providerMessageId, String failureReason) {

    public static SendOutcome accepted(String providerMessageId) {
        return new SendOutcome(true, providerMessageId, null);
    }

    public static SendOutcome rejected(String failureReason) {
        return new SendOutcome(false, null, failureReason);
    }
}