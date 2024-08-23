package com.example.toolexample.service;

import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel", tools = {"tavilyWebSearchTool"})
public interface ChatService {

    String chat(String query);
}
