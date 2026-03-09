package com.chatapp.chat_app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Client {
    private String name;

    private LocalDateTime arrivedAt;

    private Server currServer ;

    public Client (String name) {
        this.name = name ;
        this.arrivedAt = LocalDateTime.now() ;


    }

}
