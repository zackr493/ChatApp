package com.chatapp.chat_app.controller;


import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.dto.FinishRequest;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.SessionRepository;
import com.chatapp.chat_app.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;
    private static final Logger logger = LoggerFactory.getLogger(SessionController.class);

    @PostMapping("/finish")
    public ApiResponse<String> finishSession(@RequestBody FinishRequest request) {
        logger.info("Finish request: sessionId={}, rating={}", request.getSessionId(), request.getRating());
        try {
            sessionService.finishSession(request.getSessionId(), request.getRating());
            return new ApiResponse<>(200, "Session finished", request.getSessionId());
        } catch (Exception e) {
            logger.error("Error finishing session: {}", request.getSessionId(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // GET all sessions
    @GetMapping
    public ApiResponse<List<SessionEntity>> getAllSessions() {
        logger.info("Received request to fetch all sessions");

        try {
            List<SessionEntity> sessions = sessionRepository.findAll();
            logger.info("Fetched {} sessions successfully", sessions.size());
            return new ApiResponse<>(200, "Sessions fetched successfully", sessions);
        } catch (Exception e) {
            logger.error("Error occurred while fetching all sessions", e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // GET session by ID
    @GetMapping("/{id}")
    public ApiResponse<SessionEntity> getSessionById(@PathVariable String id) {
        logger.info("Received request to fetch session by ID: {}", id);

        try {
            return sessionRepository.findById(id)
                    .map(session -> {
                        logger.info("Session found: id={}", id);
                        return new ApiResponse<>(200, "Session found", session);
                    })
                    .orElseGet(() -> {
                        logger.warn("Session not found: {}", id);
                        return new ApiResponse<>(404, "Session not found: " + id, null);
                    });
        } catch (Exception e) {
            logger.error("Error occurred while fetching session by ID: {}", id, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }



}
