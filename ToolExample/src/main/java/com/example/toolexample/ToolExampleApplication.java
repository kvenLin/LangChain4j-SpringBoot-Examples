package com.example.toolexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ToolExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ToolExampleApplication.class, args);
    }

}
