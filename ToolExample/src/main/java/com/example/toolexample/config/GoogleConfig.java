package com.example.toolexample.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = GoogleConfig.PREFIX)
public class GoogleConfig {

    static final String PREFIX = "google";

    private String apiKey;

    private String searchEngineId;

}
