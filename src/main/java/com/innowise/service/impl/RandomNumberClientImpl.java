package com.innowise.service.impl;

import com.innowise.service.RandomNumberClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class RandomNumberClientImpl implements RandomNumberClient {

    private final WebClient randomApiWebClient;

    @Override
    public int generateRandomNumber() {
        Integer[] numbers = randomApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1.0/random")
                        .queryParam("min", 1)
                        .queryParam("max", 100)
                        .queryParam("count", 1)
                        .build())
                .retrieve()
                .bodyToMono(Integer[].class)
                .block();

        return numbers != null ? numbers[0] : 0;
    }
}
