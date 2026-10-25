package com.rushil.outreach.personalization.port;

import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.lead.Lead;

public interface EmailComposer {

    ComposedEmail compose(SequenceStep step, Lead lead);
}