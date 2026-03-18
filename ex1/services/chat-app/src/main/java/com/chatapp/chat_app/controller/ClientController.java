package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.dto.ClientRequest;
import com.chatapp.chat_app.service.ClientService;
import com.chatapp.chat_app.service.ServerManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
@RequiredArgsConstructor
public class ClientController {

    private final ClientService clientService;

    private static final Logger logger = LoggerFactory.getLogger(ClientController.class);

    // CREATE
    @PostMapping
    public ApiResponse<ClientEntity> createClient(@RequestBody ClientRequest request) {
        logger.info("Received request to create client: {}", request.getClientName());

        if (request.getClientName() == null || request.getClientName().isBlank()) {
            logger.warn("Client creation failed: Client Name is empty");
            return new ApiResponse<>(400, "Client Name cannot be empty", null);
        }

        try {
            ClientEntity client = clientService.createClient(request.getClientName());
            logger.info("Client created successfully: id={}, name={}", client.getId(), client.getClientName());
            return new ApiResponse<>(200, "Client created successfully", client);
        } catch (Exception e) {
            logger.error("Error occurred while creating client: {}", request.getClientName(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // READ all
    @GetMapping
    public List<ClientEntity> getAllClients() {
        logger.info("Received request to fetch all clients");

        try {
            List<ClientEntity> clients = clientService.getAllClients();
            logger.info("Fetched {} clients successfully", clients.size());
            return clients;
        } catch (Exception e) {
            logger.error("Error occurred while fetching clients", e);
            throw e; // or handle with custom ApiResponse if needed
        }
    }

    // READ by client id
    @GetMapping("/{clientId}")
    public ApiResponse<ClientEntity> getClientById(@PathVariable String clientId) {
        logger.info("Received request to fetch client by ID: {}", clientId);

        try {
            ClientEntity client = clientService.getClient(clientId);
            if (client == null) {
                logger.warn("Client not found: {}", clientId);
                return new ApiResponse<>(404, "Client not found: " + clientId, null);
            }

            logger.info("Client found: id={}, name={}", client.getId(), client.getClientName());
            return new ApiResponse<>(200, "Client found", client);
        } catch (Exception e) {
            logger.error("Error occurred while fetching client by ID: {}", clientId, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }




}