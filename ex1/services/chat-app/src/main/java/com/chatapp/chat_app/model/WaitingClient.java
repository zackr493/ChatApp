package com.chatapp.chat_app.model;

import lombok.Data;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;

@Data
public class WaitingClient {

    private final String clientId;
    private final String sessionId;
    private final CountDownLatch readyLatch = new CountDownLatch(1);

    public WaitingClient(String clientId, String sessionId) {
        this.clientId  = clientId;
        this.sessionId = sessionId;
    }

}