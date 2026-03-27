package com.chatapp.chat_app.exception;

public class ClientTimedOutException extends RuntimeException {
    public ClientTimedOutException(String message) {
        super(message);
    }
}