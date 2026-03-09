package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.*;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


// inject final fields
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerManager serverManager;


    @PostMapping("/join")
    public ApiResponse<String> joinServer(@RequestBody JoinRequest request) {


        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return new ApiResponse<>(400, "Client name cannot be empty", null);
        }


        Client client = serverManager.getClient(request.getClientId());
        if (client == null) {
            return new ApiResponse<>(404, "Client not found. Please create the client first.", null);
        }

        int timeout = (request.getTimeoutMs() != null) ? request.getTimeoutMs() : 300000;


        new Thread(() -> serverManager.handleClientJoining(client,  timeout)).start();

        return new ApiResponse<>(200,
                "Client " + client.getId() + " is trying to join a server",
                client.getId());




    }


    @PostMapping("/finish")
    public String finishSession(@RequestBody FinishRequest request) {
        String sessionId = request.getSessionId();
        int rating = request.getRating();

        try {
            serverManager.finishSession(sessionId, rating); // your ServerManager handles server updates safely
            return "Session finished successfully: " + sessionId;
        } catch (Exception e) {
            return "Failed to finish session: " + e.getMessage();
        }
    }

    // GET all servers
    @GetMapping
    public List<Server> getAllServers() {
        return serverManager.getServers();
    }

    // GET server by name
    @GetMapping("/{serverName}")
    public Server getServerByName(@PathVariable String serverName) {
        Server server = serverManager.getServerByName(serverName);
        if (server == null) {
            throw new RuntimeException("Server not found: " + serverName);
        }
        return server;
    }


}