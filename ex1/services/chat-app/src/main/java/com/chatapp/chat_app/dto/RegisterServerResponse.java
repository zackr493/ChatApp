package com.chatapp.chat_app.dto;

import com.chatapp.chat_app.model.ServerEntity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterServerResponse {
    private String status;   // "REGISTERED" or "ALREADY_REGISTERED"
    private String message;
    private String serverId; // null when ALREADY_REGISTERED
    private ServerEntity server; // full server data
}
