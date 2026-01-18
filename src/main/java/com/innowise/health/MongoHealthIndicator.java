package com.innowise.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for MongoDB connectivity.
 * Checks if MongoDB database is available and responsive.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MongoHealthIndicator implements HealthIndicator {

    private final MongoTemplate mongoTemplate;

    @Override
    public Health health() {
        try {
            Document result = mongoTemplate.executeCommand("{ ping: 1 }");
            if (result.getDouble("ok") == 1.0) {
                return Health.up()
                        .withDetail("database", "MongoDB")
                        .withDetail("status", "reachable")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "MongoDB")
                        .withDetail("status", "unreachable")
                        .build();
            }
        } catch (Exception e) {
            log.error("MongoDB health check failed", e);
            return Health.down()
                    .withDetail("database", "MongoDB")
                    .withDetail("status", "error")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
