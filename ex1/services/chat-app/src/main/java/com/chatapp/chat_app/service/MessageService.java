package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.MessageRole;
import com.chatapp.chat_app.dto.SendMessageResponse;
import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.*;
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.MessageRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

    private final ClientRepository  clientRepository;
    private final SessionRepository sessionRepository;
    private final MessageRepository messageRepository;
    private final ServerManager     serverManager;
    private final RestTemplate      restTemplate;

    @Value("${chat.nginx-url}")
    private String nginxUrl;

    private static final int MAX_RETRIES   = 3;
    private static final int RETRY_WAIT_MS = 1000;


    public SendMessageResponse sendMessage(String clientId, String sessionId, String content)
            throws InterruptedException {
        if (sessionId == null || sessionId.isBlank()) {
            return handleFirstMessage(clientId, content);
        }
        return handleSubsequentMessage(clientId, sessionId, content);
    }

    private SendMessageResponse handleFirstMessage(String clientId, String content)
            // first message we save session to db

            throws InterruptedException {
        logger.info("First message from clientId={}", clientId);

        ClientEntity client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        SessionEntity session = SessionEntity.builder()
                .id(UUID.randomUUID().toString())
                .clientEntity(client)
                .startTime(LocalDateTime.now())
                .status(SessionStatus.WAITING)
                .build();
        sessionRepository.save(session);

        // enqueue and block message , until signal
        WaitingClient wc = serverManager.enqueueClient(clientId, session.getId());

        // after 5min it becomes lost and removed, if latch not released
        boolean ready = wc.getReadyLatch().await(300, TimeUnit.SECONDS);

        if (!ready) {
            serverManager.markLost(wc);
            throw new RuntimeException("Request timed out after 300s");
        }

        session.setStatus(SessionStatus.ASSIGNED);
        sessionRepository.save(session);

        return forwardAndSave(session, clientId, content);
    }

    // TODO: combine subsequent , handlefirst
    private SendMessageResponse handleSubsequentMessage(String clientId, String sessionId, String content)
            throws InterruptedException {
        logger.info("Subsequent message: clientId={}, sessionId={}", clientId, sessionId);


        // since subsequent message, session already created
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));


        // check if session active
        if (session.getStatus() != SessionStatus.ASSIGNED) {
            throw new RuntimeException("Session is not active: " + session.getStatus());
        }

        // check whether session belongs to client
        if (!session.getClientEntity().getId().equals(clientId)) {
            throw new RuntimeException("Session does not belong to client: " + clientId);
        }

        // enqueue and block message , until signal
        WaitingClient wc = serverManager.enqueueClient(clientId, sessionId);
        boolean ready = wc.getReadyLatch().await(300, TimeUnit.SECONDS);

        if (!ready) {
            serverManager.markLost(wc);
            throw new RuntimeException("No server available — request timed out");
        }

        return forwardAndSave(session, clientId, content);
    }

    private SendMessageResponse forwardAndSave(SessionEntity session, String clientId, String content) {
        // this function
        // 1. saves to message repository
        // 2. forwards message to nginx for server routing

        // Persist client message
        messageRepository.save(MessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .sessionId(session.getId())
                .clientId(clientId)
                .role(MessageRole.USER)
                .content(content)
                .sentAt(LocalDateTime.now())
                .build());

        // Forward to NGINX with recursive retry on 502 , (TO_CHANGE)
        String reply = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {

                // this throws 502 when server is overloaded
                Map<String, Object> response = restTemplate.postForObject(
                        nginxUrl + "/server/message",
                        Map.of(
                                "sessionId", session.getId(),
                                "clientId",  clientId,
                                "content",   content
                        ),
                        Map.class
                );
                if (response == null) {
                    reply = "No response";
                }
                else {
                    response.get("data") ;
                }

                // when success, signal next item
                serverManager.signalNextClient();

                break;

            } catch (HttpServerErrorException e) {
                logger.warn("NGINX error {} on attempt {}/{}", e.getStatusCode(), attempt, MAX_RETRIES);
                if (attempt == MAX_RETRIES) {
                    throw new RuntimeException("All chat servers busy after " + MAX_RETRIES + " retries");
                }

                try {
                    // wait awhile before retry
                    Thread.sleep((long) RETRY_WAIT_MS * attempt);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted during retry");
                }
            }
        }

        // persist mock response from server
        messageRepository.save(MessageEntity.builder()
                .id(UUID.randomUUID().toString())
                .sessionId(session.getId())
                .clientId(clientId)
                .role(MessageRole.ASSISTANT)
                .content(reply)
                .sentAt(LocalDateTime.now())
                .build());

        logger.info("Message forwarded and saved for sessionId={}", session.getId());
        return new SendMessageResponse(session.getId(), reply);
    }
}