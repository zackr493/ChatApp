package com.chatapp.chat_app.controller;

// Entities
import com.chatapp.chat_app.model.ServerEntity;

// Repository
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.SessionRepository;

// Services
import com.chatapp.chat_app.service.MessageService;
import com.chatapp.chat_app.service.ServerManager;
import com.chatapp.chat_app.service.SessionService;

// DTOs
import com.chatapp.chat_app.dto.RegisterServerRequest;
import com.chatapp.chat_app.dto.HeartbeatRequest;
import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.dto.RegisterServerResponse;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private static final Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final ClientRepository clientRepository;
    private final SessionRepository sessionRepository;
    private final ServerManager serverManager;
    private final SessionService sessionService;
    private final MessageService messageService;

    // server calls this on startup
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterServerResponse>> registerServer(
            @RequestBody RegisterServerRequest request) {
        logger.info("Server registration: name={}, host={}", request.getServerName(), request.getHost());

        try {
            RegisterServerResponse server = serverManager.registerServer(request.getServerName(), request.getHost());
            logger.info("Server registered successfully: name={}, host={}", request.getServerName(), request.getHost());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(200, "Server registered", server));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid server registration request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, "Invalid server name or host", null));
        } catch (DataAccessException e) {
            logger.error("Database error while registering server: name={}, host={}", request.getServerName(),
                    request.getHost(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Database error", null));
        } catch (Exception e) {
            logger.error("Error registering server: name={}, host={}", request.getServerName(), request.getHost(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }



    @GetMapping
    public ResponseEntity<ApiResponse<List<ServerEntity>>> getAllServers() {
        logger.info("Received request to fetch all servers");

        try {
            List<ServerEntity> servers = serverManager.getAllServers();
            logger.info("Fetched {} servers successfully", servers.size());
            return ResponseEntity.ok(new ApiResponse<>(200, "Servers fetched successfully", servers));
        } catch (Exception e) {
            logger.error("Error occurred while fetching servers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }

    @GetMapping("/{serverId}")
    public ResponseEntity<ApiResponse<ServerEntity>> getServerById(@PathVariable String serverId) {
        logger.info("Received request to fetch server by ID: {}", serverId);

        try {
            Optional<ServerEntity> serverOpt = serverManager.getServerById(serverId);
            return serverOpt
                    .map(s -> ResponseEntity.ok(new ApiResponse<>(200, "Server found", s)))
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponse<>(404, "Server not found: " + serverId, null)));
        } catch (Exception e) {
            logger.error("Error occurred while fetching server by ID: {}", serverId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }

    @GetMapping("/{serverId}/active-clients")
    public ResponseEntity<ApiResponse<List<String>>> getActiveClients(@PathVariable String serverId) {
        logger.info("Received request to fetch active clients for server: {}", serverId);

        try {
            List<String> clients = serverManager.getActiveClients(serverId);
            logger.info("Fetched {} active clients for server: {}", clients.size(), serverId);
            return ResponseEntity.ok(new ApiResponse<>(200, "Active clients fetched successfully", clients));
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid server ID: {}", serverId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(400, "Invalid server ID", null));
        } catch (Exception e) {
            logger.error("Error fetching active clients for server: {}", serverId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }
}