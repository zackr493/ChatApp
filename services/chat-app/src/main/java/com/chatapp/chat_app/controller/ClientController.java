package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.dto.Client;
import com.chatapp.chat_app.dto.ClientRequest;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ServerManager serverManager;

    // CREATE
    @PostMapping
    public ApiResponse<Client> createClient(@RequestBody ClientRequest request) {

        // make sure clientId exists
        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return new ApiResponse<>(400, "Client ID cannot be empty", null);
        }

        System.out.println(request.getClientId() + "client id");
        Client client = new Client(request.getClientId());
        boolean added = serverManager.addClient(client);

        if (added) {
            return new ApiResponse<>(200, "Client created successfully", client);
        } else {
            return new ApiResponse<>(409, "Client already exists", client);
        }
    }

    // READ all
    @GetMapping
    public List<Client> getAllClients() {
        return serverManager.getAllClients();
    }

    // READ by client name
    @GetMapping("/{clientName}")
    public ApiResponse<Client> getClientByName(@PathVariable String clientName) {
        Client client = serverManager.getClient(clientName);

        System.out.println(client + "CLIENT");
        if (client == null) {
            return new ApiResponse<>(404, "Client not found: " + clientName, null);
        }
        return new ApiResponse<>(
                200,
                "Client found" ,
                client
        );
    }




}