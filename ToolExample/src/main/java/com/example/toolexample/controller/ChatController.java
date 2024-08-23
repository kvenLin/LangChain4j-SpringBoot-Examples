package com.example.toolexample.controller;

import com.example.toolexample.service.ChatService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Slf4j
@RestController
public class ChatController {

    @Resource
    private ChatService chatService;

    @GetMapping("/ai/chat")
    public String chat(String query) {
        return chatService.chat(query);
    }



}
