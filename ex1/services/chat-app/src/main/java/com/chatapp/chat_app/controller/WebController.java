
// Controllers
package com.chatapp.chat_app.controller;

// Repositories
import com.chatapp.chat_app.repository.LostClientRepository;
import com.chatapp.chat_app.repository.SessionRepository;

// Services
import com.chatapp.chat_app.service.ServerManager;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class WebController {

    private final ServerManager serverManager;
    private final SessionRepository sessionRepository;
    private final LostClientRepository lostClientRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }


}
