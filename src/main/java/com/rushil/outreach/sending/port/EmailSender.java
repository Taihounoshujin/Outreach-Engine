package com.rushil.outreach.sending.port;

public interface EmailSender {

    SendOutcome send(OutboundEmail email);
}