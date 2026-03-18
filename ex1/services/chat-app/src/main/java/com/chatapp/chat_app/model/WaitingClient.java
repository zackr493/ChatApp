package com.chatapp.chat_app.model;

import lombok.Data;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;


@Data
public class WaitingClient {

    private final String clientId;
    private final String sessionId;
    // absolute expiry time
    private final Instant deadline;

    // worker blocks on this
    private final CountDownLatch finishLatch = new CountDownLatch(1);
    private volatile int rating;

    public WaitingClient(String clientId, String sessionId, long timeoutMs) {
        this.clientId  = clientId;
        this.sessionId = sessionId;
        this.deadline  = Instant.now().plusMillis(timeoutMs);
    }

    public boolean isExpired()
    {
        return Instant.now().isAfter(deadline);
    }


}
