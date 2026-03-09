package com.chatapp.chat_app.config;

import com.chatapp.chat_app.service.ServerManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServerConfig {

    @Bean
    public ServerManager serverManager() {
        int maxServers = 5; // You can configure this
        return new ServerManager(maxServers);
    }
}