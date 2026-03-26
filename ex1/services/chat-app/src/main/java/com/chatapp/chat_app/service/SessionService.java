package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.model.WaitingClient;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final ServerRepository serverRepository;

    private final ServerManager serverManager;      // needed to signal the latch
    private final RestTemplate restTemplate;

    // here we split finish session to two methods, so we dont open db connection during http call to chat server
    @Transactional
    public String finishSessionDB(String sessionId, int rating) {
        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.FINISHED || session.getStatus() == SessionStatus.LOST) {
            throw new RuntimeException("Session " + sessionId + " is already terminated");
        }

        session.setEndTime(LocalDateTime.now());
        session.setRating(rating);
        session.setStatus(SessionStatus.FINISHED);
        sessionRepository.save(session);

        ServerEntity server = session.getServerEntity();
        if (server != null) {
            completeSession(server.getId(), server.getServerName(), rating);
        } else {
            logger.warn("Session {} had no assigned server", sessionId);
        }

        // return host so we can notify after transaction commits
        return server != null ? server.getHost() : null;
    }

    // called after transaction — no connection held
    public void notifyAndSignal(String serverHost, String sessionId) {
        if (serverHost != null) {
            notifyChatServer(serverHost, sessionId);
        }
        serverManager.signalNextClient();
    }


    private void completeSession(String serverId, String serverName, int rating) {

        // handle retry when optimisticlocking  exception is thrown by jpa @version
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                ServerEntity server = serverRepository.findById(serverId)
                        .orElseThrow(() -> new RuntimeException("Server not found: " + serverId));

                server.setNumClientsDay(server.getNumClientsDay() + 1);
                server.setNumClientsMonth(server.getNumClientsMonth() + 1);

                if (rating > 0) {
                    server.setRatingTotal(server.getRatingTotal() + rating);
                    server.setRatingCount(server.getRatingCount() + 1);
                }

                serverRepository.save(server);
                logger.info("[{}] Session complete. rating={}, day={}, month={}",
                        serverName, rating, server.getNumClientsDay(), server.getNumClientsMonth());
                return;

            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                logger.warn("Optimistic lock conflict on attempt {}/{} for server {}", attempt, maxRetries, serverId);
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed to update server stats after " + maxRetries + " retries");
                }
            }
        }
    }

    private void notifyChatServer(String serverHost, String sessionId) {
        try {
            restTemplate.postForObject(
                    serverHost + "/server/sessions/finish",
                    Map.of("sessionId", sessionId),
                    Void.class
            );
            logger.info("Notified chat server {} to release sessionId={}", serverHost, sessionId);
            serverManager.signalNextClient();

        } catch (Exception e) {
            // Non-fatal — session is already marked finished in DB
            logger.error("Failed to notify chat server {} of finish: {}", serverHost, e.getMessage());
            throw new RuntimeException("Failed to release server slot , please try again later") ;
        }
    }
}
