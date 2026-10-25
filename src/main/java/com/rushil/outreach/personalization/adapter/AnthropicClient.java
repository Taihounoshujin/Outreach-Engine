package com.rushil.outreach.personalization.adapter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "outreach.personalization.provider", havingValue = "anthropic")
public class AnthropicClient {

    private static final String API_VERSION = "2023-06-01";

    private final RestClient restClient;
    private final AnthropicProperties properties;

    public AnthropicClient(AnthropicProperties properties) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            // Fail at startup, not on the first send at 3am.
            throw new IllegalStateException(
                    "outreach.personalization.provider=anthropic but ANTHROPIC_API_KEY is not set");
        }
        this.properties = properties;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) properties.connectTimeout().toMillis());
        factory.setReadTimeout((int) properties.readTimeout().toMillis());

        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultHeader("x-api-key", properties.apiKey())
                .defaultHeader("anthropic-version", API_VERSION)
                .build();
    }

    /** Sends one system prompt + one user message and returns the text reply. */
    public String complete(String systemPrompt, String userMessage) {
        MessagesRequest request = new MessagesRequest(
                properties.model(),
                properties.maxTokens(),
                systemPrompt,
                List.of(new Message("user", userMessage)));

        MessagesResponse response = restClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(MessagesResponse.class);

        if (response == null || response.content() == null) {
            throw new IllegalStateException("Empty response from Anthropic API");
        }
        return response.content().stream()
                .filter(block -> "text".equals(block.type()))
                .map(ContentBlock::text)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No text block in Anthropic response"));
    }

    record MessagesRequest(
            String model,
            @JsonProperty("max_tokens") int maxTokens,
            String system,
            List<Message> messages) {
    }

    record Message(String role, String content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record MessagesResponse(List<ContentBlock> content, @JsonProperty("stop_reason") String stopReason) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ContentBlock(String type, String text) {
    }
}