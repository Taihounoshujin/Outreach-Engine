package com.rushil.outreach.personalization;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

@Component
public class OpenerSanitizer {

    static final int MAX_LENGTH = 300;

    private static final Pattern URL = Pattern.compile("(?i)(https?://|www\\.)");
    private static final Pattern WRAPPING_QUOTES = Pattern.compile("^[\"'“”]+|[\"'“”]+$");

    public Optional<String> clean(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String text = raw.trim();
        if (text.contains("\n\n")) {
            return Optional.empty();
        }
        text = WRAPPING_QUOTES.matcher(text.replaceAll("\\s+", " ")).replaceAll("").trim();

        if (text.isEmpty()
                || text.length() > MAX_LENGTH
                || text.contains("{{")
                || text.contains("<")
                || URL.matcher(text).find()
                || text.equalsIgnoreCase("SKIP")) {
            return Optional.empty();
        }
        return Optional.of(text);
    }
}