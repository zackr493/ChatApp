package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.dto.ClientRequest;
import com.chatapp.chat_app.service.ClientService;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    // CREATE
    @PostMapping
    public ApiResponse<ClientEntity> createClient(@RequestBody ClientRequest request) {
        if (request.getClientName() == null || request.getClientName().isBlank()) {
            return new ApiResponse<>(400, "Client Name cannot be empty", null);
        }

        ClientEntity client = clientService.createClient(request.getClientName());


        return new ApiResponse<>(200, "Client created successfully", client);
    }

    // READ all
    @GetMapping
    public List<ClientEntity> getAllClients() {
        return clientService.getAllClients();
    }

    // READ by client id
    @GetMapping("/{clientId}")
    public ApiResponse<ClientEntity> getClientById(@PathVariable String clientId) {
        ClientEntity client = clientService.getClient(clientId);
        if (client == null) {
            return new ApiResponse<>(404, "Client not found: " + clientId, null);
        }
        return new ApiResponse<>(200, "Client found", client);
    }




}