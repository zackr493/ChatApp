package com.chatapp.server.service;


import com.chatapp.server.config.ServerConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final ServerConfig serverConfig;
    private final ConcurrentHashMap<String, Future<?>> activeSessions;
    private final ExecutorService threadPool;

    public SessionManager(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.activeSessions = new ConcurrentHashMap<>();
        this.threadPool = Executors.newFixedThreadPool(serverConfig.getMaxSessions());
        logger.info("SessionManager initialized with maxSessions={}", serverConfig.getMaxSessions());
    }

    // Reserve a thread slot for this session
    // Returns true if slot was available, false if server is full
    public boolean assignSession(String sessionId) {
        if (activeSessions.containsKey(sessionId)) {
            logger.warn("Session already assigned: {}", sessionId);
            return false;
        }

        if (activeSessions.size() >= serverConfig.getMaxSessions()) {
            logger.warn("Server at capacity, cannot assign session: {}", sessionId);
            return false;
        }

        // Reserve the slot with a placeholder future
        Future<?> future = threadPool.submit(() -> {
            logger.info("Thread reserved for sessionId={}", sessionId);
        });

        activeSessions.put(sessionId, future);
        logger.info("Session assigned: {}. Active sessions: {}", sessionId, activeSessions.size());
        return true;
    }

    // Check if session is active on this server
    public boolean hasSession(String sessionId) {
        return activeSessions.containsKey(sessionId);
    }

    // Get current active session count
    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    // Get max capacity
    public int getMaxCapacity() {
        return serverConfig.getMaxSessions();
    }

    // Release the session slot
    public void releaseSession(String sessionId) {
        activeSessions.remove(sessionId);
        logger.info("Session released: {}. Active sessions: {}", sessionId, activeSessions.size());
    }
}