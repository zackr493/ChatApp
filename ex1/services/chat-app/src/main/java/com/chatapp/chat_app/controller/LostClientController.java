package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.LostClientEntity;
import com.chatapp.chat_app.repository.LostClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lost-clients")
@RequiredArgsConstructor
public class LostClientController {
    private final LostClientRepository lostClientRepository;

    @GetMapping
    public ApiResponse<List<LostClientEntity>> getLostClients() {
        return new ApiResponse<>(200, "OK", lostClientRepository.findAll());
    }
}