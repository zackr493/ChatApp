package com.chatapp.chat_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JoinRequest {
    private String clientId;
    private Integer timeoutMs = 300000; // default
}
