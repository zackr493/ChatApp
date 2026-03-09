package com.chatservice.controller;

import com.chatservice.dto.ChatMessage;
import com.chatservice.service.ChatService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Send a message
    @PostMapping("/send")
    public ChatMessage sendMessage(@RequestParam String user, @RequestParam String message) {
        return chatService.sendMessage(user, message);
    }

    // Get all messages
    @GetMapping("/messages")
    public List<ChatMessage> getAllMessages() {
        return chatService.getAllMessages();
    }
}