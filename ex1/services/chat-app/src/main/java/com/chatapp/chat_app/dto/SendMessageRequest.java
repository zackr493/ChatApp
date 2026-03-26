package com.chatapp.chat_app.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private String clientId;
    private String content;
    private String sessionId;

}
