package com.chatapp.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Client {
    private String name;

    private LocalDateTime created_at;

    private String curr_client;

}
