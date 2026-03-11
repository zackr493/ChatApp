package com.chatapp.chat_app.service;

import com.chatapp.chat_app.dto.SessionStatus;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
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
    private final ServerManager     serverManager;      // needed to signal the latch

    public void finishSession(String sessionId, int rating) {
        logger.info("Finishing session: sessionId={}, rating={}", sessionId, rating);

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.FINISHED || session.getStatus() == SessionStatus.LOST) {
            logger.warn("Session {} is already in terminal state: {}", sessionId, session.getStatus());
            return;
        }

        session.setEndTime(LocalDateTime.now());
        session.setRating(rating);
        session.setStatus(SessionStatus.FINISHED);
        sessionRepository.save(session);

        // release worker thread
        serverManager.signalFinish(sessionId, rating);

        logger.info("Session {} finished successfully", sessionId);
    }
}
