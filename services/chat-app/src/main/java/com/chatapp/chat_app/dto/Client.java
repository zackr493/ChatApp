package com.chatapp.chat_app.dto;

import lombok.Data;

import java.time.LocalDateTime;



@Data
public class Client {
    private String id;
    private LocalDateTime arrivedAt;
    private ClientStatus status;


    public Client (String name) {
        this.id = name ;
        this.arrivedAt = LocalDateTime.now() ;
        this.status = ClientStatus.CREATED;

    }

}
