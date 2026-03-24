package com.chatapp.chat_app.dto;

import lombok.Data;

@Data
public class RegisterServerRequest {
    private String serverName;
    // hostname that is predefined in nginx
    private String host;
}