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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final ServerRepository serverRepository;

    private final ServerManager     serverManager;      // needed to signal the latch

    public void finishSession(String sessionId, int rating) {
        logger.info("Finishing session: sessionId={}, rating={}", sessionId, rating);

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.FINISHED || session.getStatus() == SessionStatus.LOST) {
            logger.warn("Session {} is already in terminal: {}", sessionId, session.getStatus());

            throw new RuntimeException("Session " + sessionId + " is already terminated");
        }

        session.setEndTime(LocalDateTime.now());
        session.setRating(rating);
        session.setStatus(SessionStatus.FINISHED);
        sessionRepository.save(session);

        // Update server stats now that the session is done
        ServerEntity server = session.getServerEntity();
        if (server != null) {
            completeSession(server.getId(), server.getServerName(), rating);
        } else {
            logger.warn("Session {} had no assigned server — skipping completeSession", sessionId);
        }

        logger.info("Session {} finished successfully", sessionId);
    }

    private void completeSession(String serverId, String serverName, int rating) {
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
    }
}
