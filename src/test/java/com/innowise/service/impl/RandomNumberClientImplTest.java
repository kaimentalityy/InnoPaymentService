package com.innowise.service.impl;

import com.innowise.config.RandomApiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RandomNumberClientImplTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private RandomApiProperties randomApiProperties;

    private RandomNumberClientImpl randomNumberClient;

    @BeforeEach
    void setUp() {
        lenient().when(randomApiProperties.getPath()).thenReturn("/integers");
        lenient().when(randomApiProperties.getMin()).thenReturn(1);
        lenient().when(randomApiProperties.getMax()).thenReturn(100);
        lenient().when(randomApiProperties.getCount()).thenReturn(1);

        randomNumberClient = new RandomNumberClientImpl(webClient, randomApiProperties);
    }

    @Test
    void generateRandomNumber_shouldReturnRandomNumber_whenApiCallSucceeds() {
        Integer[] expectedNumbers = { 42 };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(expectedNumbers));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(42);
        verify(webClient).get();
    }

    @Test
    void generateRandomNumber_shouldReturnMinValue_whenApiReturnsNull() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.empty());

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void generateRandomNumber_shouldReturnMinValue_whenApiReturnsEmptyArray() {
        Integer[] emptyArray = {};

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(emptyArray));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void generateRandomNumber_shouldReturnFirstNumber_whenApiReturnsMultipleNumbers() {
        Integer[] multipleNumbers = { 15, 25, 35 };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(multipleNumbers));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(15);
    }

    @Test
    void generateRandomNumber_shouldReturnMinValue_whenApiCallFails() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class))
                .thenReturn(Mono.error(new RuntimeException("API error")));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(1);
    }

    @Test
    void generateRandomNumber_shouldHandleZeroValue() {
        Integer[] zeroArray = { 0 };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(zeroArray));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(0);
    }

    @Test
    void generateRandomNumber_shouldHandleNegativeValue() {
        Integer[] negativeArray = { -5 };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(negativeArray));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(-5);
    }

    @Test
    void generateRandomNumber_shouldHandleLargeValue() {
        Integer[] largeArray = { 999999 };

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.just(largeArray));

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(999999);
    }

    @Test
    void generateRandomNumber_shouldReturnConfiguredMinValue_whenApiFails() {
        when(randomApiProperties.getMin()).thenReturn(5);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.empty());

        int result = randomNumberClient.generateRandomNumber();

        assertThat(result).isEqualTo(5);
    }
}
