package com.chatapp.chat_app.controller;


import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.SessionEntity;
import com.chatapp.chat_app.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/session")
@RequiredArgsConstructor
public class SessionController {

    private final SessionRepository sessionRepository;

    @GetMapping
    public ApiResponse<List<SessionEntity>> getAllSessions() {
        List<SessionEntity> sessions = sessionRepository.findAll();
        return new ApiResponse<>(200, "Sessions fetched successfully", sessions);
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionEntity> getSessionById(@PathVariable String id) {
        return sessionRepository.findById(id)
                .map(session -> new ApiResponse<>(200, "Session found", session))
                .orElseGet(() -> new ApiResponse<>(404, "Session not found: " + id, null));
    }



}
