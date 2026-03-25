package com.chatapp.chat_app.controller;


import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.dto.SendMessageRequest;
import com.chatapp.chat_app.dto.SendMessageResponse;
import com.chatapp.chat_app.model.MessageEntity;
import com.chatapp.chat_app.repository.MessageRepository;
import com.chatapp.chat_app.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;
    private final MessageRepository messageRepository;

    @PostMapping("/send")
    public ApiResponse<SendMessageResponse> sendMessage(@RequestBody SendMessageRequest request) {
        logger.info("Message request received: clientId={}, sessionId={}",
                request.getClientId(), request.getSessionId());

        if (request.getClientId() == null || request.getClientId().isBlank()) {
            return new ApiResponse<>(400, "Client ID cannot be empty", null);
        }

        if (request.getContent() == null || request.getContent().isBlank()) {
            return new ApiResponse<>(400, "Message content cannot be empty", null);
        }

        try {
            SendMessageResponse response = messageService.sendMessage(
                    request.getClientId(),
                    request.getSessionId(),
                    request.getContent()
            );
            return new ApiResponse<>(200, "Message sent", response);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Request interrupted for clientId={}", request.getClientId());
            return new ApiResponse<>(500, "Request interrupted", null);

        } catch (RuntimeException e) {
            logger.warn("Send message failed: {}", e.getMessage());
            return new ApiResponse<>(400, e.getMessage(), null);

        } catch (Exception e) {
            logger.error("Unexpected error for clientId={}", request.getClientId(), e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // to load messages from db
    @GetMapping("/session/{sessionId}")
    public ApiResponse<List<MessageEntity>> getMessagesBySession(@PathVariable String sessionId) {
        logger.info("Getting messages for sessionId={}", sessionId);
        try {
            List<MessageEntity> messages = messageRepository.findBySessionIdOrderBySentAtAsc(sessionId);
            return new ApiResponse<>(200, "Messages fetched", messages);
        } catch (Exception e) {
            logger.error("Error getting messages for sessionId={}", sessionId, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // get single message by ID
    @GetMapping("/{messageId}")
    public ApiResponse<MessageEntity> getMessageById(@PathVariable String messageId) {
        logger.info("Fetching message: messageId={}", messageId);

        Optional<MessageEntity> message = messageRepository.findById(messageId);

        if (message.isPresent()) {
            return new ApiResponse<MessageEntity>(200, "Message found", message.get());
        }

        return new ApiResponse<>(404, "Message not found" , null) ;


    }



    // delete message by id
    @DeleteMapping("/{messageId}")
    public ApiResponse<String> deleteMessage(@PathVariable String messageId) {
        logger.info("Deleting message: messageId={}", messageId);
        try {
            if (!messageRepository.existsById(messageId)) {
                return new ApiResponse<>(404, "Message not found: " + messageId, null);
            }
            messageRepository.deleteById(messageId);
            return new ApiResponse<>(200, "Message deleted", messageId);
        } catch (Exception e) {
            logger.error("Error deleting message: messageId={}", messageId, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }

    // delete all messages for a session
    @DeleteMapping("/session/{sessionId}")
    public ApiResponse<String> deleteMessagesBySession(@PathVariable String sessionId) {
        logger.info("Deleting all messages for sessionId={}", sessionId);
        try {
            messageRepository.deleteBySessionId(sessionId);
            return new ApiResponse<>(200, "Messages deleted for session", sessionId);
        } catch (Exception e) {
            logger.error("Error deleting messages for sessionId={}", sessionId, e);
            return new ApiResponse<>(500, "Internal server error", null);
        }
    }
}