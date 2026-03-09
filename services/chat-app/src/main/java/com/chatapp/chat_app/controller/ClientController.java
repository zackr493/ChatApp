package com.chatapp.chat_app.controller;

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
    public String createClient(@RequestBody ClientRequest request) {
        Client client = new Client(request.getClientName());
        boolean added = serverManager.addClient(client);
        return added ? "Client created: " + client.getName() : "Client already exists";
    }

    // READ all
    @GetMapping
    public List<Client> getAllClients() {
        return serverManager.getAllClients();
    }




}