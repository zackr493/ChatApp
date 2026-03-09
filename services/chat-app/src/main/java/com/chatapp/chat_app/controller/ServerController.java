package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.Client;
import com.chatapp.chat_app.dto.FinishRequest;
import com.chatapp.chat_app.dto.JoinRequest;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


// inject final fields
@RestController
@RequestMapping("/servers")
@RequiredArgsConstructor
public class ServerController {

    private final ServerManager serverManager;


    @PostMapping("/join")
    public String joinServer(@RequestBody JoinRequest request) {
        Client client = new Client(request.getClientName());
        new Thread(() -> serverManager.handleClientJoining(client, 300000)).start();
        return "Client " + client.getName() + " is trying to join a server.";
    }

    @PostMapping("/finish")
    public String finishSession(
            @RequestBody FinishRequest request) {

//        Client client = serverManager.getClientByName(clientName);
//        Server server = serverManager.getServerByName(serverName);
//
//        if (client == null || server == null || client.getCurrServer() != server) {
//            return "Invalid client or server, or client not connected to this server.";
//        }
//
//        serverManager.finishSession(client, server, rating);
//        return "Session finished for client " + clientName + " on server " + serverName;
        return null ;
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