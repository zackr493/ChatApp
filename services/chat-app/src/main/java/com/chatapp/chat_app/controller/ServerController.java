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


    @PostMapping("/join")
    public ApiResponse<String> joinServer(@RequestBody JoinRequest request) {


        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return new ApiResponse<>(400, "Client name cannot be empty", null);
        }


//        ClientEntity clientEntity = serverManager.getClient(request.getClientId());
//        if (clientEntity == null) {
//            return new ApiResponse<>(404, "Client not found. Please create the client first.", null);
//        }

        // find associated client
        ClientEntity client = clientRepository.findById(request.getClientId()).orElseThrow(() -> new RuntimeException("Client does not exist"));

        // creates a session
        SessionEntity session = SessionEntity.builder()
                .clientEntity(client)
                .startTime(LocalDateTime.now())
                .build();

        session = sessionRepository.save(session);

        int timeout = (request.getTimeoutMs() != null) ? request.getTimeoutMs() : 300000;
        final String sessionId = session.getId();

        // limit threads
        executorService.submit(() -> serverManager.handleClientJoining(request.getClientId(), sessionId, timeout));

        return new ApiResponse<>(200,
                "Client " + client.getId() + " is trying to join a server",
                client.getId());

    }


    @PostMapping("/finish")
    public String finishSession(@RequestBody FinishRequest request) {
        String sessionId = request.getSessionId();
        int rating = request.getRating();

        try {
            sessionService.finishSession(sessionId, rating); // your ServerManager handles server updates safely
            return "Session finished successfully: " + sessionId;
        } catch (Exception e) {
            return "Failed to finish session: " + e.getMessage();
        }
    }

    // GET all servers
    @GetMapping
    public List<ServerEntity> getAllServers() {
        return serverManager.getAllServers();
    }

    // GET server by id
    @GetMapping("/{serverId}")
    public ApiResponse<ServerEntity> getServerById(@PathVariable String serverId) {
        try {
            Optional<ServerEntity> serverOpt = serverManager.getServerById(serverId);

            return serverOpt
                    .map(server -> new ApiResponse<>(200, "Server found", server))
                    .orElseGet(() -> new ApiResponse<>(404, "Server not found: " + serverId, null));

        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(400, "Invalid server ID format: " + serverId, null);
        }
    }

    @PostMapping
    public ApiResponse<ServerEntity> createServer() {


        // since we dont need params for servers
        ServerEntity server = serverManager.createServer();

        return new ApiResponse<>(
                200,
                "Server created successfully",
                server
        );
    }




}