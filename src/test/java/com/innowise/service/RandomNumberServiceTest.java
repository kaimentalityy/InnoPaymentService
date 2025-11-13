package com.innowise.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RandomNumberServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private RandomNumberService randomNumberService;

    @BeforeEach
    void setUp() {
        // Inject the mocked WebClient into the service
        ReflectionTestUtils.setField(randomNumberService, "webClient", webClient);
    }

    @Test
    void isEven_WhenNumberIsEven_ReturnsTrue() {
        // Arrange
        Integer[] evenNumberArray = {100};
        setupWebClientMock(evenNumberArray);

        // Act
        boolean result = randomNumberService.isEven();

        // Assert
        assertTrue(result);
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenNumberIsOdd_ReturnsFalse() {
        // Arrange
        Integer[] oddNumberArray = {101};
        setupWebClientMock(oddNumberArray);

        // Act
        boolean result = randomNumberService.isEven();

        // Assert
        assertFalse(result);
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenNumberIsLargeAndEven_ReturnsTrue() {
        // Arrange
        Integer[] largeEvenNumber = {998};
        setupWebClientMock(largeEvenNumber);

        // Act
        boolean result = randomNumberService.isEven();

        // Assert
        assertTrue(result);
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenNumberIsLargeAndOdd_ReturnsFalse() {
        // Arrange
        Integer[] largeOddNumber = {999};
        setupWebClientMock(largeOddNumber);

        // Act
        boolean result = randomNumberService.isEven();

        // Assert
        assertFalse(result);
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenApiReturnsNull_ThrowsNullPointerException() {
        // Arrange
        setupWebClientMock(null);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> randomNumberService.isEven());
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenApiReturnsEmptyArray_ThrowsArrayIndexOutOfBoundsException() {
        // Arrange
        Integer[] emptyArray = {};
        setupWebClientMock(emptyArray);

        // Act & Assert
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> randomNumberService.isEven());
        verifyWebClientInteractions();
    }

    @Test
    void isEven_WhenZeroIsReturned_ReturnsTrue() {
        // Arrange - zero is technically even
        Integer[] zeroArray = {0};
        setupWebClientMock(zeroArray);

        // Act
        boolean result = randomNumberService.isEven();

        // Assert
        assertTrue(result);
        verifyWebClientInteractions();
    }

    @Test
    void isEven_VerifiesCorrectApiEndpoint() {
        // Arrange
        Integer[] numberArray = {100};
        setupWebClientMock(numberArray);

        // Act
        randomNumberService.isEven();

        // Assert
        verify(requestHeadersUriSpec).uri("/api/v1.0/random?min=100&max=1000&count=1");
    }

    /**
     * Helper method to setup WebClient mock chain
     */
    private void setupWebClientMock(Integer[] returnValue) {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Integer[].class)).thenReturn(Mono.justOrEmpty(returnValue));
    }

    /**
     * Helper method to verify WebClient interactions
     */
    private void verifyWebClientInteractions() {
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("/api/v1.0/random?min=100&max=1000&count=1");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(Integer[].class);
    }
}