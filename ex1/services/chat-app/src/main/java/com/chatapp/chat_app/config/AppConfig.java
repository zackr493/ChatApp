package com.chatapp.chat_app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Semaphore;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public Semaphore serverCapacitySemaphore(@Value("${chat.total-capacity:30}") int totalCapacity) {
        return new Semaphore(totalCapacity);
    }
}