package com.rushil.outreach.personalization.adapter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "outreach.personalization.anthropic")
public record AnthropicProperties(
        String apiKey,
        @DefaultValue("https://api.anthropic.com") String baseUrl,
        @DefaultValue("claude-haiku-5-5") String model,
        @DefaultValue("200") int maxTokens,
        @DefaultValue("5s") Duration connectTimeout,
        @DefaultValue("20s") Duration readTimeout) {
}