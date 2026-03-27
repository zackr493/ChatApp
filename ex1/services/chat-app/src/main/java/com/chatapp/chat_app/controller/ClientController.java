package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.dto.ClientRequest;
import com.chatapp.chat_app.service.ClientService;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResponse<ClientEntity>> createClient(@RequestBody ClientRequest request) {
        logger.info("Received request to client: {}", request.getClientName());

        if (request.getClientName() == null || request.getClientName().isBlank()) {
            logger.warn("Client creation failed: Client Name is empty");
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(400, "Client Name cannot be empty", null));
        }

        try {
            logger.debug("Checking if client exists with name: {}", request.getClientName());
            boolean exists = clientService.clientExists(request.getClientName());
            logger.info("Client existence check result: {} - exists: {}", request.getClientName(), exists);

            if (exists) {
                logger.warn("Client creation failed: Client already exists with name: {}", request.getClientName());
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiResponse<>(409, "Client with this name already exists", null));
            }

            ClientEntity client = clientService.createClient(request.getClientName());
            logger.info("Client created successfully: id={}, name={}", client.getId(), client.getClientName());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(200, "Client created successfully", client));
        } catch (Exception e) {
            logger.error("Error occurred while creating client: {}", request.getClientName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }


    // READ all
    @GetMapping
    public ResponseEntity<ApiResponse<List<ClientEntity>>> getAllClients() {
        logger.info("Received request to fetch all clients");

        try {
            List<ClientEntity> clients = clientService.getAllClients();
            logger.info("Fetched {} clients successfully", clients.size());
            return ResponseEntity.ok(new ApiResponse<>(200, "Clients fetched successfully", clients));
        } catch (Exception e) {
            logger.error("Error occurred while fetching clients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }

    // READ by client id
    @GetMapping("/{clientId}")
    public ResponseEntity<ApiResponse<ClientEntity>> getClientById(@PathVariable String clientId) {
        logger.info("Received request to fetch client by ID: {}", clientId);

        try {
            ClientEntity client = clientService.getClient(clientId);
            if (client == null) {
                logger.warn("Client not found: {}", clientId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(404, "Client not found: " + clientId, null));
            }

            logger.info("Client found: id={}, name={}", client.getId(), client.getClientName());
            return ResponseEntity.ok(new ApiResponse<>(200, "Client found", client));
        } catch (Exception e) {
            logger.error("Error occurred while fetching client by ID: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }




}