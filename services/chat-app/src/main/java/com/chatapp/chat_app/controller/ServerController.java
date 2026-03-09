package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.Client;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor  // Lombok generates constructor for final fields
public class ServerController {

    private final ServerManager serverManager;

    /**
     * Simulate a client joining the server system
     * @param clientName - name of the client
     * @param timeoutMs - time to wait for a free server (in milliseconds)
     */
    @PostMapping("/join")
    public String joinServer(@RequestParam String clientName,
                             @RequestParam(defaultValue = "5000") int timeoutMs) {
        Client client = new Client(clientName);
        new Thread(() -> serverManager.handleClientJoining(client, timeoutMs)).start();
        return "Client " + clientName + " is trying to join a server.";
    }

    /**
     * Get current server statistics
     */
    @GetMapping("/stats")
    public String getServerStats() {
        serverManager.printServerStats();
        return "Server stats printed in console.";
    }
}