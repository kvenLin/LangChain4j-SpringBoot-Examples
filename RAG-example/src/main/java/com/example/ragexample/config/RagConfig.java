package com.example.ragexample.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Configuration
public class RagConfig {

    @Bean
    public EmbeddingStore embeddingStore(ElasticsearchEmbeddingStore elasticsearchEmbeddingStore) {
        return elasticsearchEmbeddingStore;
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public CompressingQueryTransformer compressingQueryTransformer(ChatLanguageModel chatLanguageModel) {
        return new CompressingQueryTransformer(chatLanguageModel);
    }

    public static Path toPath(String relativePath) {
        try {
            URL fileUrl = Utils.class.getClassLoader().getResource(relativePath);
            return Paths.get(fileUrl.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }

    @Bean
    public RetrievalAugmentor retrievalAugmentor(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel,
                                                 CompressingQueryTransformer compressingQueryTransformer,
                                                 EmbeddingStore<TextSegment> embeddingStore) {

        // Let's create a separate embedding store specifically for biographies.
        EmbeddingStore<TextSegment> biographyEmbeddingStore =
                embed(toPath("documents/biography-of-john-doe.txt"), embeddingModel, embeddingStore);
        ContentRetriever biographyContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(biographyEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        // Additionally, let's create a separate embedding store dedicated to terms of use.
        EmbeddingStore<TextSegment> termsOfUseEmbeddingStore =
                embed(toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel, embeddingStore);
        ContentRetriever termsOfUseContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(termsOfUseEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
        retrieverToDescription.put(biographyContentRetriever, "biography of John Doe");
        retrieverToDescription.put(termsOfUseContentRetriever, "terms of use of car rental company");

        return DefaultRetrievalAugmentor.builder()
                .queryRouter(new LanguageModelQueryRouter(chatLanguageModel, retrieverToDescription))
                .queryTransformer(compressingQueryTransformer)
                .build();
    }


    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.withMaxMessages(10);
    }
}
