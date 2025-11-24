package com.innowise.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebClientConfigTest {

    @Mock
    private RandomApiProperties randomApiProperties;

    private WebClientConfig webClientConfig;

    @BeforeEach
    void setUp() {
        webClientConfig = new WebClientConfig(randomApiProperties);
    }

    @Test
    void randomApiWebClient_shouldCreateWebClient_withCorrectBaseUrl() {
        when(randomApiProperties.getBaseUrl()).thenReturn("https:

        WebClient webClient = webClientConfig.randomApiWebClient();

        assertThat(webClient).isNotNull();
    }

    @Test
    void randomApiWebClient_shouldCreateWebClient_withDifferentBaseUrl() {
        when(randomApiProperties.getBaseUrl()).thenReturn("https:

        WebClient webClient = webClientConfig.randomApiWebClient();

        assertThat(webClient).isNotNull();
    }

    @Test
    void randomApiWebClient_shouldCreateWebClient_withLocalhost() {
        when(randomApiProperties.getBaseUrl()).thenReturn("http:

        WebClient webClient = webClientConfig.randomApiWebClient();

        assertThat(webClient).isNotNull();
    }

    @Test
    void randomApiWebClient_shouldCreateWebClient_withHttpsUrl() {
        when(randomApiProperties.getBaseUrl()).thenReturn("https:

        WebClient webClient = webClientConfig.randomApiWebClient();

        assertThat(webClient).isNotNull();
    }

    @Test
    void randomApiWebClient_shouldCreateWebClient_withHttpUrl() {
        when(randomApiProperties.getBaseUrl()).thenReturn("http:

        WebClient webClient = webClientConfig.randomApiWebClient();

        assertThat(webClient).isNotNull();
    }
}
