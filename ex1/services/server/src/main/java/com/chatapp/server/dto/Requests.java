package com.chatapp.server.dto;

import lombok.Data;

public class Requests {
    @Data
    public static class AssignSessionRequest {
        private String sessionId;
        private String clientId;
    }

    @Data
    public static class MessageRequest {
        private String sessionId;
        private String clientId;
        private String content;
    }

    @Data
    public static class FinishSessionRequest {
        private String sessionId;
        private int rating;
    }
}
