package com.rushil.outreach.suppression;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/u")
@Tag(name = "Unsubscribe", description = "Public opt-out endpoint linked from every email")
public class UnsubscribeController {

    private final UnsubscribeService unsubscribeService;

    public UnsubscribeController(UnsubscribeService unsubscribeService) {
        this.unsubscribeService = unsubscribeService;
    }

    @GetMapping("/{token}")
    @Operation(summary = "Opt out via the link in an email")
    public ResponseEntity<String> unsubscribe(@PathVariable UUID token) {
        unsubscribeService.unsubscribeByToken(token);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body("""
                        <!doctype html>
                        <html lang="en">
                        <head><meta charset="utf-8"><title>Unsubscribed</title></head>
                        <body style="font-family:system-ui;max-width:32rem;margin:4rem auto">
                          <h1>You're unsubscribed</h1>
                          <p>You won't receive any further emails from this sender.</p>
                        </body>
                        </html>
                        """);
    }

    @PostMapping("/{token}")
    @Operation(summary = "One-click unsubscribe (RFC 8058), called by mailbox providers")
    public ResponseEntity<Void> oneClickUnsubscribe(@PathVariable UUID token) {
        unsubscribeService.unsubscribeByToken(token);
        return ResponseEntity.ok().build();
    }
}