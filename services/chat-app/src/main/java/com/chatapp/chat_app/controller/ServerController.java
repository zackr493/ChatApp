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

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final ClientRepository  clientRepository;
    private final SessionRepository sessionRepository;
    private final ServerManager     serverManager;
    private final SessionService    sessionService;


    // allows clients to be added to queue , and handled by free server
    @PostMapping("/join")
    public ApiResponse<String> joinServer(@RequestBody JoinRequest request) {
        logger.info("Received join request: clientId={}", request.getClientId());

        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return new ApiResponse<>(400, "Client ID cannot be empty", null);
        }

        try {

            // get client from db
            ClientEntity client = clientRepository.findById(request.getClientId())
                    .orElseThrow(() -> new RuntimeException("Client does not exist: " + request.getClientId()));


            // create session
            SessionEntity session = SessionEntity.builder()
                    .id(java.util.UUID.randomUUID().toString())
                    .clientEntity(client)
                    .startTime(LocalDateTime.now())
                    .status(SessionStatus.WAITING)
                    .build();
            session = sessionRepository.save(session);

            int timeoutMs = (request.getTimeoutMs() != null) ? request.getTimeoutMs() : 300_000;

            // add client , session into queue
            serverManager.enqueueClient(client.getId(), session.getId(), timeoutMs);

            logger.info("Client {} enqueued with sessionId={}", client.getId(), session.getId());
            return new ApiResponse<>(200,
                    "Joined queue. Poll GET /session/" + session.getId() + " to check status.",
                    session.getId());   // return sessionId so client can poll for status

        } catch (RuntimeException e) {
            logger.warn("Join failed: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Unexpected error on join", e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }



    // triggered by finish button, should only be called when client successfully joins
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



    @GetMapping
    public List<ServerEntity> getAllServers() {
        return serverManager.getAllServers();
    }

    @GetMapping("/{serverId}")
    public ApiResponse<ServerEntity> getServerById(@PathVariable String serverId) {
        Optional<ServerEntity> serverOpt = serverManager.getServerById(serverId);
        return serverOpt
                .map(s  -> new ApiResponse<>(200, "Server found", s))
                .orElse(new ApiResponse<>(404, "Server not found: " + serverId, null));
    }

    @PostMapping
    public ApiResponse<ServerEntity> createServer() {
        try {
            ServerEntity server = serverManager.createServer();
            return new ApiResponse<>(200, "Server created", server);
        } catch (IllegalStateException e) {
            return new ApiResponse<>(400, e.getMessage(), null);
        } catch (Exception e) {
            logger.error("Error creating server", e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }
}