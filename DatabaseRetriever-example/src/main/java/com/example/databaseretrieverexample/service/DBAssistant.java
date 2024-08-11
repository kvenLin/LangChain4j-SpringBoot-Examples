package com.example.databaseretrieverexample.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface DBAssistant {

    @SystemMessage("""
            you are a db assistant
            you can help me to retrieve data from database
            """)
    String chat(String query);

}
