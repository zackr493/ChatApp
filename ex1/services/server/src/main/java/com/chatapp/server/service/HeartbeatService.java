package com.chatapp.server.service;

// Config
import com.chatapp.server.config.ServerConfig;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class HeartbeatService {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatService.class);

    private final RestTemplate restTemplate;
    private final ServerConfig serverConfig;

    private String serverHost;

    // Registers this instance with the main Spring Boot app on startup
    @PostConstruct
    public void register() {
        try {
            Map response = restTemplate.postForObject(
                    serverConfig.getMainAppUrl() + "/servers/register",
                    Map.of(
                            "serverName", serverConfig.getServerName(),
                            "host",       serverConfig.getServerHost()
                    ),
                    Map.class
            );

            if (response != null && response.get("data") != null) {
                Map data = (Map) response.get("data");
                this.serverHost = (String) data.get("host");
                logger.info("Registered with main app — host={}", serverHost);
            }

        } catch (Exception e) {
            logger.error("Failed to register with main app: {}", e.getMessage());
        }
    }

//    // Sends heartbeat to main app every 30 seconds
//    @Scheduled(fixedDelayString = "${chat.heartbeat-interval-ms:30000}")
//    public void sendHeartbeat() {
//        if (serverHost == null) {
//            logger.warn("Heartbeat skipped — not registered yet");
//            return;
//        }
//        try {
//            restTemplate.postForObject(
//                    serverConfig.getMainAppUrl() + "/servers/heartbeat",
//                    Map.of("serverHost", serverHost),
//                    Void.class
//            );
//            logger.debug("Heartbeat sent for host={}", serverHost);
//        } catch (Exception e) {
//            logger.warn("Heartbeat failed: {}", e.getMessage());
//        }
//    }

    public String getServerHost() {
        return serverHost;
    }
}