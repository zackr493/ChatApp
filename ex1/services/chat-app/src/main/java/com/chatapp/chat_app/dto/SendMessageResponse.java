package com.chatapp.chat_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SendMessageResponse {
    private String sessionId;  // client must store this for subsequent messages
    private String reply;
}