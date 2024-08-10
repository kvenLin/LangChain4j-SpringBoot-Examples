package com.example.ragexample.service.aiservice;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "openAiChatModel", contentRetriever = "filterContentRetriever", chatMemoryProvider = "chatMemoryProvider")
public interface ChatWithFileService {

    @SystemMessage("""
            you are a file assistant, when user choose a file: {{file}},
            you can answer user's question about the file content.
            """)
    String chatWithFile(@MemoryId Long userId, @V("file") String file, @UserMessage String question);
}
