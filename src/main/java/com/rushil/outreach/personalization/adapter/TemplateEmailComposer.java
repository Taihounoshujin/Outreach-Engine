package com.rushil.outreach.personalization.adapter;

import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.lead.Lead;
import com.rushil.outreach.personalization.TemplateRenderer;
import com.rushil.outreach.personalization.port.ComposedEmail;
import com.rushil.outreach.personalization.port.EmailComposer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConditionalOnProperty(name = "outreach.personalization.provider", havingValue = "template", matchIfMissing = true)
public class TemplateEmailComposer implements EmailComposer {

    private final TemplateRenderer renderer;

    public TemplateEmailComposer(TemplateRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public ComposedEmail compose(SequenceStep step, Lead lead) {
        return new ComposedEmail(
                renderer.renderSubject(step.getSubjectTemplate(), lead, Map.of()),
                renderer.renderBody(step.getBodyTemplate(), lead, Map.of()));
    }
}