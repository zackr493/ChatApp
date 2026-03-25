package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.*;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.model.ServerEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.model.MessageEntity;



import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import com.chatapp.chat_app.service.MessageService;
import com.chatapp.chat_app.service.ServerManager;
import com.chatapp.chat_app.service.SessionService;

// DTOs
import com.chatapp.chat_app.dto.RegisterServerRequest;
import com.chatapp.chat_app.dto.HeartbeatRequest;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final ClientRepository  clientRepository;
    private final SessionRepository sessionRepository;
    private final ServerManager     serverManager;
    private final SessionService    sessionService;
    private final MessageService messageService;

    // server calls this on startup
    @PostMapping("/register")
    public ApiResponse<RegisterServerResponse> registerServer(@RequestBody RegisterServerRequest request) {
        logger.info("Server registration: name={}, host={}", request.getServerName(), request.getHost());
        try {
            RegisterServerResponse server = serverManager.registerServer(request.getServerName(), request.getHost());
            return new ApiResponse<RegisterServerResponse>(200, "Server registered", server);
        } catch (Exception e) {
            logger.error("Error registering server", e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // server calls this on interval, to record health in db
    @PostMapping("/heartbeat")
    public ApiResponse<String> heartbeat(@RequestBody HeartbeatRequest request) {
        logger.debug("Heartbeat received: serverhost={}", request.getServerHost());
        try {
            serverManager.recordHeartbeat(request.getServerHost());
            return new ApiResponse<>(200, "OK", request.getServerHost());
        } catch (Exception e) {
            logger.warn("Heartbeat failed for serverHost={}: {}", request.getServerHost(), e.getMessage());
            return new ApiResponse<>(404, "Server not found", null);
        }
    }


    //


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
}