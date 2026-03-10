package com.chatapp.chat_app.config;

import com.chatapp.chat_app.repository.ClientRepository;
import com.chatapp.chat_app.repository.LostClientRepository;
import com.chatapp.chat_app.repository.ServerRepository;
import com.chatapp.chat_app.repository.SessionRepository;
import com.chatapp.chat_app.service.ServerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    @Bean
    public ServerManager serverManager(ClientRepository clientRepository,
                                       SessionRepository sessionRepository,
                                       ServerRepository serverRepository,
                                       LostClientRepository lostClientRepository) {
        int maxServers = 5;
        return new ServerManager(clientRepository, sessionRepository, serverRepository, lostClientRepository, maxServers);
    }
}