package com.chatapp.chat_app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Session {

    private Client client;
    private Server server;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int rating;

    public Session(Client client, Server server) {
        this.client = client;
        this.server = server;
        this.startTime = LocalDateTime.now();

    }


}
