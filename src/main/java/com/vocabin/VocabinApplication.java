package com.vocabin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VocabinApplication {

    public static void main(String[] args) {
        SpringApplication.run(VocabinApplication.class, args);
    }
}
