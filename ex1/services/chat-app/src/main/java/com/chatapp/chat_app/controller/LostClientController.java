package com.chatapp.chat_app.controller;

import com.chatapp.chat_app.dto.ApiResponse;
import com.chatapp.chat_app.model.LostClientEntity;
import com.chatapp.chat_app.repository.LostClientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/lost-clients")
@RequiredArgsConstructor
public class LostClientController {
    private final LostClientRepository lostClientRepository;

    private static final Logger logger = LoggerFactory.getLogger(LostClientController.class);


    @GetMapping
    public ResponseEntity<ApiResponse<List<LostClientEntity>>> getLostClients() {
        logger.info("Received request to fetch all lost clients");

        try {
            List<LostClientEntity> lostClients = lostClientRepository.findAll();
            logger.info("Fetched {} lost clients successfully", lostClients.size());
            return ResponseEntity.ok(new ApiResponse<>(200, "Lost clients fetched successfully", lostClients));
        } catch (Exception e) {
            logger.error("Error occurred while fetching lost clients", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(500, "Internal server error", null));
        }
    }
}