package com.example.ragexample.config;

import com.example.ragexample.service.store.PersistentChatMemoryStore;
import com.example.ragexample.util.CommonUtil;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static dev.langchain4j.store.embedding.filter.MetadataFilterBuilder.metadataKey;

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

    //用于压缩查询语句
    @Bean
    public CompressingQueryTransformer compressingQueryTransformer(ChatLanguageModel chatLanguageModel) {
        return new CompressingQueryTransformer(chatLanguageModel);
    }

    @Bean
    public RetrievalAugmentor retrievalAugmentor(ChatLanguageModel chatLanguageModel, EmbeddingModel embeddingModel,
                                                 CompressingQueryTransformer compressingQueryTransformer,
                                                 EmbeddingStore<TextSegment> embeddingStore) {

        // Let's create a separate embedding store specifically for biographies.
        EmbeddingStore<TextSegment> biographyEmbeddingStore =
                CommonUtil.embed(CommonUtil.toPath("documents/biography-of-john-doe.txt"), embeddingModel, embeddingStore);
        ContentRetriever biographyContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(biographyEmbeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(4)
                .minScore(0.6)
                .build();

        // Additionally, let's create a separate embedding store dedicated to terms of use.
        EmbeddingStore<TextSegment> termsOfUseEmbeddingStore =
                CommonUtil.embed(CommonUtil.toPath("documents/miles-of-smiles-terms-of-use.txt"), embeddingModel, embeddingStore);
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
    public ChatMemoryProvider chatMemoryProvider(PersistentChatMemoryStore persistentChatMemoryStore) {
        ChatMemoryProvider chatMemoryProvider = memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(persistentChatMemoryStore)
                .build();
        return chatMemoryProvider;
    }

    @Bean
    public ContentRetriever filterContentRetriever(EmbeddingStore embeddingStore,
                                            EmbeddingModel embeddingModel) {
        Function<Query, Filter> filterByUserId =
                (query) -> metadataKey("userId").isEqualTo(query.metadata().chatMemoryId().toString());

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                // 动态过滤, 只检索MemoryId等于当前用户id的文档数据
                .dynamicFilter(filterByUserId)
                .build();
        return contentRetriever;
    }
}
