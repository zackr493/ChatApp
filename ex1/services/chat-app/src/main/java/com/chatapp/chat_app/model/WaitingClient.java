package com.chatapp.chat_app.model;

import lombok.Data;

import java.time.Instant;

@Data
public class WaitingClient {

    private final String clientId;
    private final String sessionId;
    private final Instant deadline;

    public WaitingClient(String clientId, String sessionId, long timeoutMs) {
        this.clientId  = clientId;
        this.sessionId = sessionId;
        this.deadline  = Instant.now().plusMillis(timeoutMs);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(deadline);
    }
}