package com.chatapp.chat_app.controller;

// Dtos
import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.dto.FinishRequest;

// Entities
import com.chatapp.chat_app.model.SessionEntity;

// Repository
import com.chatapp.chat_app.repository.SessionRepository;

// Services
import com.chatapp.chat_app.service.SessionService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        int maxRetries = 3;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // commit transaction and release connection
                String serverHost = sessionService.finishSessionDB(
                        request.getSessionId(), request.getRating());

                // notify server
                sessionService.notifyAndSignal(serverHost, request.getSessionId());

                return new ApiResponse<>(200, "Session finished", request.getSessionId());
            } catch (ObjectOptimisticLockingFailureException e) {
                logger.warn("Optimistic lock on attempt {}/{}", attempt, maxRetries);
                if (attempt == maxRetries)
                    return new ApiResponse<>(500, "Could not finish session after retries", null);
                try { Thread.sleep(100L * attempt); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (RuntimeException e) {
                logger.warn("Finish failed: {}", e.getMessage());
                return new ApiResponse<>(400, e.getMessage(), null);
            } catch (Exception e) {
                logger.error("Error finishing session: {}", request.getSessionId(), e);
                return new ApiResponse<>(500, "Internal server error", null);
            }
        }
        return new ApiResponse<>(500, "Could not finish session", null);
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
