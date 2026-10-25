package com.rushil.outreach.personalization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenerSanitizerTest {

    private final OpenerSanitizer sanitizer = new OpenerSanitizer();

    @Test
    void acceptsAndTidiesNormalSentence() {
        assertThat(sanitizer.clean("  \"Saw Beacon Heating has served   Columbia homeowners.\"  "))
                .contains("Saw Beacon Heating has served Columbia homeowners.");
    }

    @Test
    void rejectsSkip() {
        assertThat(sanitizer.clean("SKIP")).isEmpty();
    }

    @Test
    void rejectsLinks() {
        assertThat(sanitizer.clean("Check out https://evil.example now")).isEmpty();
        assertThat(sanitizer.clean("Visit www.evil.example")).isEmpty();
    }

    @Test
    void rejectsMarkupAndTemplateSyntax() {
        assertThat(sanitizer.clean("Hi <b>there</b>")).isEmpty();
        assertThat(sanitizer.clean("Hello {{contact_name}}")).isEmpty();
    }

    @Test
    void rejectsMultipleParagraphsAndOverlongText() {
        assertThat(sanitizer.clean("One.\n\nTwo.")).isEmpty();
        assertThat(sanitizer.clean("a".repeat(OpenerSanitizer.MAX_LENGTH + 1))).isEmpty();
    }

    @Test
    void rejectsEmptyAndNull() {
        assertThat(sanitizer.clean("   ")).isEmpty();
        assertThat(sanitizer.clean(null)).isEmpty();
    }
}