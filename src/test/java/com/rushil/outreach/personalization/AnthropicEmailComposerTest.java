package com.rushil.outreach.personalization;

import com.rushil.outreach.campaign.SequenceStep;
import com.rushil.outreach.lead.Lead;
import com.rushil.outreach.personalization.adapter.AnthropicClient;
import com.rushil.outreach.personalization.adapter.AnthropicEmailComposer;
import com.rushil.outreach.personalization.port.ComposedEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests with a mocked client — no network, no API key. They pin down the
 * behaviour that matters: good output is inserted, and every failure mode
 * degrades to the plain template instead of breaking the send.
 */
class AnthropicEmailComposerTest {

    private AnthropicClient client;
    private AnthropicEmailComposer composer;
    private SequenceStep step;
    private Lead lead;

    @BeforeEach
    void setUp() {
        client = mock(AnthropicClient.class);
        composer = new AnthropicEmailComposer(client, new TemplateRenderer(), new OpenerSanitizer());

        step = new SequenceStep(null, 1, 0,
                "Quick question about {{company_name}}",
                "<p>Hi {{contact_name}},</p><p>{{personalized_opener}}</p><p>Pitch.</p>");

        lead = new Lead(null, "dana@beacon.example");
        lead.setContactName("Dana");
        lead.setCompanyName("Beacon Heating & Air");
        lead.setIndustry("HVAC");
        lead.setCity("Columbia");
    }

    @Test
    void insertsGeneratedOpener() {
        when(client.complete(anyString(), anyString()))
                .thenReturn("Saw that Beacon Heating & Air serves homeowners around Columbia.");

        ComposedEmail email = composer.compose(step, lead);

        assertThat(email.subject()).isEqualTo("Quick question about Beacon Heating & Air");
        // Body values are HTML-escaped, so & becomes &amp;
        assertThat(email.htmlBody()).contains("Saw that Beacon Heating &amp; Air serves homeowners");
    }

    @Test
    void fallsBackWhenModelSkips() {
        when(client.complete(anyString(), anyString())).thenReturn("SKIP");

        ComposedEmail email = composer.compose(step, lead);

        assertThat(email.htmlBody()).isEqualTo("<p>Hi Dana,</p><p></p><p>Pitch.</p>");
    }

    @Test
    void fallsBackWhenApiFails() {
        when(client.complete(anyString(), anyString())).thenThrow(new RuntimeException("timeout"));

        ComposedEmail email = composer.compose(step, lead);

        assertThat(email.htmlBody()).contains("Hi Dana").doesNotContain("{{");
    }

    @Test
    void escapesHostileLeadData() {
        lead.setCompanyName("<a href=\"https://phish.example\">Click</a>");
        when(client.complete(anyString(), anyString())).thenReturn("SKIP");

        step = new SequenceStep(null, 1, 0, "Hi", "<p>{{company_name}}</p>");
        ComposedEmail email = composer.compose(step, lead);

        assertThat(email.htmlBody()).doesNotContain("<a href").contains("&lt;a href");
    }
}