package com.chatapp.chat_app.service;

import com.chatapp.chat_app.model.ClientEntity;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientEntity createClient(String clientName) {

        // since we generate uuid for each clientname , we accept repeats for simplicity

        ClientEntity client = new ClientEntity(clientName);
        clientRepository.save(client);



        return client;
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public ClientEntity getClient(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }
}
