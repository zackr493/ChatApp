package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.*;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import com.chatapp.chat_app.service.ServerManager;
import com.chatapp.chat_app.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;


// inject final fields
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;

    private final ServerManager serverManager;
    private final ExecutorService executorService;
    private final SessionService sessionService;

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);


    @PostMapping("/join")
    public ApiResponse<String> joinServer(@RequestBody JoinRequest request) {
        logger.info("Received join server request for clientId={}", request.getClientId());

        if (request.getClientId() == null || request.getClientId().isBlank()) {
            logger.warn("Join server failed: Client ID is empty");
            return new ApiResponse<>(400, "Client name cannot be empty", null);
        }

        try {
            // find associated client
            ClientEntity client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> {
                        logger.warn("Client does not exist: {}", request.getClientId());
                        return new RuntimeException("Client does not exist");
                    });

            // creates a session
            SessionEntity session = SessionEntity.builder()
                    .clientEntity(client)
                    .startTime(LocalDateTime.now())
                    .status(SessionStatus.CREATED)
                    .build();

            session = sessionRepository.save(session);
            logger.info("Session created: sessionId={}, clientId={}", session.getId(), client.getId());

            int timeout = (request.getTimeoutMs() != null) ? request.getTimeoutMs() : 300000;
            final String sessionId = session.getId();

            // limit threads
            executorService.submit(() -> {
                logger.debug("Submitting async task for client joining: clientId={}, sessionId={}, timeout={}",
                        client.getId(), sessionId, timeout);
                serverManager.handleClientJoining(request.getClientId(), sessionId, timeout);
            });

            logger.info("Client {} is attempting to join a server with session {}", client.getId(), sessionId);
            return new ApiResponse<>(200,
                    "Client " + client.getId() + " is trying to join a server",
                    client.getId());

        } catch (Exception e) {
            logger.error("Error while client {} was trying to join server", request.getClientId(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }


    @PostMapping("/finish")
    public ApiResponse<String> finishSession(@RequestBody FinishRequest request) {
        String sessionId = request.getSessionId();
        int rating = request.getRating();

        logger.info("Received request to finish session: sessionId={}, rating={}", sessionId, rating);

        try {
            sessionService.finishSession(sessionId, rating); // your ServerManager handles server updates safely
            logger.info("Session {} ended successfully", sessionId);
            return new ApiResponse<>(
                    200,
                    "Session " + sessionId + " ended",
                    ""
            );
        } catch (Exception e) {
            logger.error("Error finishing session: {}", sessionId, e);
            return new ApiResponse<>(
                    500,
                    "Internal Server Error",
                    null
            );
        }
    }

    // GET all servers
    @GetMapping
    public List<ServerEntity> getAllServers() {
        logger.info("Received request to fetch all servers");

        try {
            List<ServerEntity> servers = serverManager.getAllServers();
            logger.info("Fetched {} servers successfully", servers.size());
            return servers;
        } catch (Exception e) {
            logger.error("Error occurred while fetching all servers", e);
            throw e; // keep behavior unchanged
        }
    }

    // GET server by id
    @GetMapping("/{serverId}")
    public ApiResponse<ServerEntity> getServerById(@PathVariable String serverId) {
        logger.info("Received request to fetch server by ID: {}", serverId);

        try {
            Optional<ServerEntity> serverOpt = serverManager.getServerById(serverId);

            if (serverOpt.isPresent()) {
                logger.info("Server found: id={}", serverId);
                return new ApiResponse<>(200, "Server found", serverOpt.get());
            } else {
                logger.warn("Server not found: {}", serverId);
                return new ApiResponse<>(404, "Server not found: " + serverId, null);
            }
        } catch (IllegalArgumentException e) {
            logger.error("Invalid server ID format: {}", serverId, e);
            return new ApiResponse<>(400, "Invalid server ID format: " + serverId, null);
        } catch (Exception e) {
            logger.error("Unexpected error while fetching server: {}", serverId, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    @PostMapping
    public ApiResponse<ServerEntity> createServer() {
        logger.info("Received request to create a new server");

        try {
            // since we don't need params for servers
            ServerEntity server = serverManager.createServer();
            logger.info("Server created successfully: id={}", server.getId());

            return new ApiResponse<>(
                    200,
                    "Server created successfully",
                    server
            );
        } catch (Exception e) {
            logger.error("Error occurred while creating server", e);
            return new ApiResponse<>(
                    500,
                    "Internal server error",
                    null
            );
        }
    }




}