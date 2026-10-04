package com.tradematching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradeMatchingEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeMatchingEngineApplication.class, args);
    }
}
