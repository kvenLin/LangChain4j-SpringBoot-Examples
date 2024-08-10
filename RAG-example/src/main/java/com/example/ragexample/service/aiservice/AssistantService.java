package com.example.ragexample.service.aiservice;


import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.Result;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface AssistantService {

    String chat(@MemoryId Long userId, @UserMessage String message);

    Result<String> chatWithSource(@MemoryId Long userId, @UserMessage String query);
}
