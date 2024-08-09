package com.example.ragexample.service;


import dev.langchain4j.service.Result;
import dev.langchain4j.service.spring.AiService;

@AiService(contentRetriever = "")
public interface AssistantService {

    String chat(String message);

    Result<String> chatWithSource(String query);

}
