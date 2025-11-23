package com.innowise.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for the Random Number API.
 * <p>
 * This class binds properties with the prefix "random-api" from
 * application configuration files (e.g., application.yml).
 * </p>
 * <p>
 * Properties include:
 * <ul>
 * <li>baseUrl - The base URL of the random number API</li>
 * <li>path - The API endpoint path</li>
 * <li>min - Minimum value for random number generation</li>
 * <li>max - Maximum value for random number generation</li>
 * <li>count - Number of random numbers to request</li>
 * </ul>
 * </p>
 *
 * @see WebClientConfig
 */
@Data
@Component
@ConfigurationProperties(prefix = "random-api")
public class RandomApiProperties {
    private String baseUrl;
    private String path;
    private int min;
    private int max;
    private int count;
}
