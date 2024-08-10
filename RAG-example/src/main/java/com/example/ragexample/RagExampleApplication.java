package com.example.ragexample;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan(basePackages = "com.example.ragexample.mapper")
@SpringBootApplication
public class RagExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RagExampleApplication.class, args);
    }

}
