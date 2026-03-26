package com.chatapp.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public String handleMessage(String sessionId, String clientId, String content) {
        logger.info("Handling message: sessionId={}, content={}", sessionId, content);

        // we simulate a chat here
        try {
            TimeUnit.SECONDS.sleep(5);
            return "Echo: " + content;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Thread was interrupted");
            return "Interrupted while processing" ;

        }

    }


}