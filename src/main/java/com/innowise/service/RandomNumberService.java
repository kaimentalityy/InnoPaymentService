package com.innowise.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Service
public class RandomNumberService {

    private final WebClient webClient = WebClient.create("https://www.randomnumberapi.com");

    public boolean isEven() {
        Integer number = Objects.requireNonNull(webClient.get()
                .uri("/api/v1.0/random?min=100&max=1000&count=1")
                .retrieve()
                .bodyToMono(Integer[].class)
                .block())[0];

        return number % 2 == 0;
    }
}
