package com.example.toolexample.config;

import com.google.api.client.util.Value;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.WebSearchTool;
import dev.langchain4j.web.search.google.customsearch.GoogleCustomWebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {


    @Resource
    private GoogleConfig googleConfig;

    @Resource
    private TavilyConfig tavilyConfig;

    @Bean
    public WebSearchEngine googleWebSearchEngine() {
        return GoogleCustomWebSearchEngine.withApiKeyAndCsi(
                googleConfig.getApiKey(),
                googleConfig.getSearchEngineId());
    }

    @Bean
    public WebSearchTool googleWebSearchTool(WebSearchEngine googleWebSearchEngine) {
        return WebSearchTool.from(googleWebSearchEngine);
    }

    @Bean
    public WebSearchEngine tavilyWebSearchEngine() {
        return TavilyWebSearchEngine.builder()
                .apiKey(tavilyConfig.getApiKey())
                .includeAnswer(true)
                .build();
    }

    @Bean
    public WebSearchTool tavilyWebSearchTool(WebSearchEngine tavilyWebSearchEngine) {
        return WebSearchTool.from(tavilyWebSearchEngine);
    }


}
