package com.example.databaseretrieverexample.config;

import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AiConfig {

    @Bean
    public ContentRetriever contentRetriever(DataSource dataSource, ChatLanguageModel chatLanguageModel) {
        return SqlDatabaseContentRetriever.builder()
                .dataSource(dataSource)
                .chatLanguageModel(chatLanguageModel)
                .build();
    }

}
