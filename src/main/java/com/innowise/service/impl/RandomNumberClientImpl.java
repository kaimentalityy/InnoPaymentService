package com.innowise.service.impl;

import com.innowise.config.RandomApiProperties;
import com.innowise.service.RandomNumberClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Implementation of {@link RandomNumberClient} that fetches random numbers from
 * an external API.
 * <p>
 * This client uses Spring WebFlux's {@link WebClient} to make non-blocking HTTP
 * requests
 * to a random number generation API. It includes retry logic and fallback
 * mechanisms
 * to ensure resilience.
 * </p>
 *
 * @see RandomNumberClient
 * @see RandomApiProperties
 */
@Component
@RequiredArgsConstructor
public class RandomNumberClientImpl implements RandomNumberClient {

    private final WebClient randomApiWebClient;
    private final RandomApiProperties randomApiProperties;

    /**
     * Generates a random number by calling an external random number API.
     * <p>
     * The method makes a GET request to the configured API endpoint with query
     * parameters
     * for min, max, and count values. It implements exponential backoff retry logic
     * (3 attempts with 2-second initial delay) to handle transient failures.
     * </p>
     * <p>
     * If the API call fails or returns no data, the method returns the configured
     * minimum value as a fallback.
     * </p>
     *
     * @return a random integer between the configured min and max values,
     *         or the minimum value if the API call fails
     */
    @Override
    public int generateRandomNumber() {
        Integer[] numbers = randomApiWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(randomApiProperties.getPath())
                        .queryParam("min", randomApiProperties.getMin())
                        .queryParam("max", randomApiProperties.getMax())
                        .queryParam("count", randomApiProperties.getCount())
                        .build())
                .retrieve()
                .bodyToMono(Integer[].class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .onErrorReturn(new Integer[0])
                .block();

        return (numbers != null && numbers.length > 0) ? numbers[0] : randomApiProperties.getMin();
    }
}
