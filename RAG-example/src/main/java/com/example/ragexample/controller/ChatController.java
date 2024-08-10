package com.example.ragexample.controller;

import com.example.ragexample.service.AssistantService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.Result;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin
@Slf4j
@RestController
class ChatController implements InitializingBean {

    @Resource
    private AssistantService assistantService;
    @Resource
    private EmbeddingStore<TextSegment> embeddingStore;
    @Resource
    private EmbeddingModel embeddingModel;

    @GetMapping("/ai/assistant")
    String assistant(@RequestParam(defaultValue = "What can you do for me?") String message) {
        String response = assistantService.chat(message);
        return response;
    }

    @ResponseBody
    @GetMapping("/ai/assistantWithSource")
    Map<String, Object> assistantWithSource(@RequestParam(defaultValue = "What is the meaning of life?") String question) {
        Result<String> answer = assistantService.chatWithSource(question);
        Map<String, Object> result = Map.of("answer", answer.content(), "sources", answer.sources().stream().map(Object::toString).collect(Collectors.joining(",")));
        return result;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        Document document = FileSystemDocumentLoader.loadDocument(toPath("documents/biography-of-john-doe.txt"), new TextDocumentParser());

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
    }

    public static Path toPath(String relativePath) {
        try {
            URL fileUrl = Utils.class.getClassLoader().getResource(relativePath);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
