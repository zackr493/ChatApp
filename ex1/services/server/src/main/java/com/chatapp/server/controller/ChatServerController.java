package com.chatapp.server.controller;

// Config
import com.chatapp.server.config.ServerConfig;

// Dtos
import com.chatapp.server.dto.ApiResponse;
import com.chatapp.server.dto.MessageResponse;
import com.chatapp.server.dto.Requests.*;

// Services
import com.chatapp.server.service.ChatService;
import com.chatapp.server.service.HeartbeatService;
import com.chatapp.server.service.SessionManager;

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

    private final ServerConfig serverConfig;

    private final SessionManager sessionManager;


    // called when a client sends a message
    @PostMapping("/message")
    public ApiResponse<MessageResponse> handleMessage(@RequestBody MessageRequest request) {
        logger.info("Message received: sessionId={}, clientId={}", request.getSessionId(), request.getClientId());
        try {
            String reply = chatService.handleMessage(
                    request.getSessionId(),
                    request.getClientId(),
                    request.getContent()
            );
            return new ApiResponse<>(200, "OK", new MessageResponse(reply, serverConfig.getServerHost()));
        } catch (Exception e) {
            logger.error("Error handling message for sessionId={}", request.getSessionId(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    @PostMapping("/sessions/assign")
    public ApiResponse<String> assignSession(@RequestBody AssignSessionRequest request) {
        logger.info("Assign session: sessionId={}, clientId={}", request.getSessionId(), request.getClientId());
        boolean assigned = sessionManager.assignSession(request.getSessionId());
        if (!assigned) {
            return new ApiResponse<>(503, "Server at capacity", null);
        }
        return new ApiResponse<>(200, "Session assigned", serverConfig.getServerHost());
    }

    @PostMapping("/sessions/finish")
    public ApiResponse<String> finishSession(@RequestBody FinishSessionRequest request) {
        logger.info("Finish session: sessionId={}", request.getSessionId());
        sessionManager.releaseSession(request.getSessionId());
        return new ApiResponse<>(200, "Session released", request.getSessionId());
    }



    // health
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return new ApiResponse<>(200,
                "active=" + sessionManager.getActiveSessionCount() + "/" + sessionManager.getMaxCapacity(),
                heartbeatService.getServerHost());
    }
}
