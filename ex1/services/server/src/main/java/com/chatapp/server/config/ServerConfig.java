package com.chatapp.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class ServerConfig {

    private String serverName;
    private String serverHost;
    private String mainAppUrl;
    private int maxSessions = 200;
}
