package com.chatapp.chat_app.service;

import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.Server;
import org.apache.catalina.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final ServerRepository serverRepository;

    @Transactional
    public void finishSession(String sessionId, int rating) {

        SessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        ServerEntity server = session.getServerEntity();
        if (server != null) {
            serverRepository.incrementStats(server.getId(), rating);
        }

        // Update session
        session.setEndTime(LocalDateTime.now());
        session.setRating(rating);
        sessionRepository.save(session);
    }
}
