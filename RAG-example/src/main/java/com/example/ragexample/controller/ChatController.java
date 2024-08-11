package com.example.ragexample.controller;

import com.example.ragexample.service.aiservice.AssistantService;
import com.example.ragexample.service.aiservice.ChatWithFileService;
import com.example.ragexample.service.store.PersistentChatMemoryStore;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.Result;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@Slf4j
@RestController
class ChatController {

    @Resource
    private AssistantService assistantService;
    @Resource
    private ChatWithFileService chatWithFileService;
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;
    @Resource
    private EmbeddingModel embeddingModel;
    @Resource
    private PersistentChatMemoryStore persistentChatMemoryStore;

    @GetMapping("/ai/assistant/{userId}")
    String assistant(@RequestParam(defaultValue = "What can you do for me?") String message,
                     @PathVariable("userId") Long userId) {
        return assistantService.chat(userId, message);
    }

    @ResponseBody
    @GetMapping("/ai/assistantWithSource/{userId}")
    Map<String, Object> assistantWithSource(@RequestParam(defaultValue = "What is the meaning of life?") String question,
                                            @PathVariable("userId") Long userId) {
        Result<String> answer = assistantService.chatWithSource(userId, question);
        return Map.of("answer", answer.content(), "sources", answer.sources().stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    @GetMapping("/ai/chatWithFile/{userId}")
    String chatWithFile(@RequestParam(defaultValue = "Summarize the text in the file") String question,
                        @RequestParam String fileName,
                        @PathVariable("userId") Long userId) {
        return chatWithFileService.chatWithFile(userId, fileName, question);
    }

    @PostMapping(value = "/ai/upload/{userId}", consumes = "multipart/form-data")
    String upload(@RequestParam("file") MultipartFile file,
                  @PathVariable Long userId) {
        String fileName = file.getOriginalFilename();
        log.info("upload file: {}", fileName);
        try {
            TextDocumentParser parser = new TextDocumentParser();
            InputStream inputStream = file.getInputStream();
            Document document = parser.parse(inputStream);
            document.metadata().put("userId", userId);
            document.metadata().put("file_name", fileName);
            EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                    .documentSplitter(DocumentSplitters.recursive(300, 0))
                    .embeddingModel(embeddingModel)
                    .embeddingStore(embeddingStore)
                    .build();

            ingestor.ingest(document);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "fail";
        }
    }

    @PostMapping(value = "/ai/clearMemory/{userId}")
    String clearMemory(@PathVariable Long userId) {
        persistentChatMemoryStore.deleteMessages(userId);
        return "success";
    }
}
