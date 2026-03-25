package com.chatapp.server.controller;


import com.chatapp.server.dto.ApiResponse;
import com.chatapp.server.dto.Requests.*;
import com.chatapp.server.service.ChatService;
import com.chatapp.server.service.HeartbeatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/server")
@RequiredArgsConstructor
public class ChatServerController {

    private static final Logger logger = LoggerFactory.getLogger(ChatServerController.class);

    private final ChatService      chatService;
    private final HeartbeatService heartbeatService;


    // called when a client sends a message
    @PostMapping("/message")
    public ApiResponse<String> handleMessage(@RequestBody MessageRequest request) {
        logger.info("Message received: sessionId={}, clientId={}", request.getSessionId(), request.getClientId());
        try {
            String reply = chatService.handleMessage(request.getSessionId(), request.getClientId(), request.getContent());
            return new ApiResponse<>(200, "OK", reply);
        } catch (Exception e) {
            logger.error("Error handling message for session {}", request.getSessionId(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }



    // health
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return new ApiResponse<>(200, "OK", heartbeatService.getServerHost());
    }
}
