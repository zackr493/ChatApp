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
    private final SessionRepository sessionRepository;

    public ClientEntity createClient(String clientId) {
        if (clientRepository.existsById(clientId)) return null;

        ClientEntity client = new ClientEntity(clientId);
        clientRepository.save(client);

        SessionEntity session = new SessionEntity(client);
        session.setClientEntity(client);
        sessionRepository.save(session);

        return client;
    }

    public List<ClientEntity> getAllClients() {
        return clientRepository.findAll();
    }

    public ClientEntity getClient(String clientId) {
        return clientRepository.findById(clientId).orElse(null);
    }
}
