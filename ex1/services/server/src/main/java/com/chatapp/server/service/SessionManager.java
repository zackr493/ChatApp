package com.chatapp.server.service;

// Config
import com.chatapp.server.config.ServerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;


@Service
public class SessionManager {

    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

    private final ServerConfig serverConfig;
    private final Set<String> activeSessions;

    public SessionManager(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;

        // map active clients that are allowed to interact with this server
        this.activeSessions = ConcurrentHashMap.newKeySet();
        logger.info("SessionManager initialized with maxSessions={}", serverConfig.getMaxSessions());
    }

    public synchronized boolean assignSession(String sessionId) {
        if (activeSessions.contains(sessionId)) {
            logger.warn("Session already assigned: {}", sessionId);
            return false;
        }

        if (activeSessions.size() >= serverConfig.getMaxSessions()) {
            logger.warn("Server at capacity ({}/{})",
                    activeSessions.size(), serverConfig.getMaxSessions());
            return false;
        }

        activeSessions.add(sessionId);
        logger.info("Session assigned: {}. Active: {}/{}",
                sessionId, activeSessions.size(), serverConfig.getMaxSessions());
        return true;
    }

    public void releaseSession(String sessionId) {
        activeSessions.remove(sessionId);
        logger.info("Session released: {}. Active: {}/{}",
                sessionId, activeSessions.size(), serverConfig.getMaxSessions());
    }

    public Set<String> getActiveSessions() {
        return Collections.unmodifiableSet(activeSessions);
    }

    public int getActiveSessionCount() {
        return activeSessions.size();
    }

    public int getMaxCapacity() {
        return serverConfig.getMaxSessions();
    }
}