package com.innowise.service;

/**
 * Client interface for generating random numbers from an external API.
 * <p>
 * This service is used to simulate payment processing outcomes
 * by generating random numbers that determine success or failure.
 * </p>
 */
public interface RandomNumberClient {

    /**
     * Generates a random number from an external API.
     * <p>
     * The implementation should handle failures gracefully and
     * provide fallback values when the API is unavailable.
     * </p>
     *
     * @return a random integer
     */
    int generateRandomNumber();
}
