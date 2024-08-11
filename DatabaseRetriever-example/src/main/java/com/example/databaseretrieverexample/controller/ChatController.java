package com.example.databaseretrieverexample.controller;

import com.example.databaseretrieverexample.service.DBAssistant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@Slf4j
@RestController
class ChatController {

    @Resource
    private DBAssistant dbAssistant;

    @GetMapping("/ai/dbAssistant")
    String dbAssistant(@RequestParam(defaultValue = "What can you do for me?") String message) {
        return dbAssistant.chat(message);
    }

}
