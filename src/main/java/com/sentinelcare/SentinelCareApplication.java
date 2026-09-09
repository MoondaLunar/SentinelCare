package com.sentinelcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SentinelCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(SentinelCareApplication.class, args);
    }
}
