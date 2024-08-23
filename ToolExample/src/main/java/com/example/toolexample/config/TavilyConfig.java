package com.example.toolexample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "tavily")
public class TavilyConfig {

    private String apiKey;
}
