package com.innowise.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebClient beans used in the application.
 * <p>
 * This configuration sets up a customized {@link WebClient} for making HTTP
 * requests
 * to the random number API with proper timeout settings, connection pooling,
 * and error handling filters.
 * </p>
 *
 * @see WebClient
 * @see RandomApiProperties
 */
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final RandomApiProperties randomApiProperties;

    /**
     * Creates and configures a {@link WebClient} bean for the random number API.
     * <p>
     * The WebClient is configured with:
     * <ul>
     * <li>Connection timeout: 5000ms</li>
     * <li>Response timeout: 5 seconds</li>
     * <li>Read timeout: 5 seconds</li>
     * <li>Write timeout: 5 seconds</li>
     * <li>Custom retry filter for 5xx server errors</li>
     * </ul>
     * </p>
     *
     * @return a configured WebClient instance for the random API
     */
    @Bean
    public WebClient randomApiWebClient() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)));

        return WebClient.builder()
                .baseUrl(randomApiProperties.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .filter(retryFilter())
                .build();
    }

    /**
     * Creates an exchange filter function that converts 5xx server errors into
     * exceptions.
     * <p>
     * This filter intercepts responses and checks for server errors (5xx status
     * codes).
     * When detected, it converts them into {@link RuntimeException} to trigger
     * retry logic in the calling code.
     * </p>
     *
     * @return an exchange filter function for error handling
     */
    private ExchangeFilterFunction retryFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().is5xxServerError()) {
                return Mono.error(new RuntimeException("Server error " + response.statusCode()));
            }
            return Mono.just(response);
        });
    }
}
