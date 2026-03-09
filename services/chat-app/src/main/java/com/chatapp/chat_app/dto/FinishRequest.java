package com.chatapp.chat_app.dto;

import lombok.Data;

@Data
public class FinishRequest {
    private String sessionId;
    private int rating;
}
