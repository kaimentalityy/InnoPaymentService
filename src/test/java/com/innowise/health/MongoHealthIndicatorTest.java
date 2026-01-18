package com.innowise.health;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.mongodb.core.MongoTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MongoHealthIndicator.
 */
@ExtendWith(MockitoExtension.class)
class MongoHealthIndicatorTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private MongoHealthIndicator healthIndicator;

    @Test
    void health_WhenMongoIsHealthy_ShouldReturnUp() {
        Document pingResult = new Document("ok", 1.0);
        when(mongoTemplate.executeCommand("{ ping: 1 }")).thenReturn(pingResult);

        Health health = healthIndicator.health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals("MongoDB", health.getDetails().get("database"));
        assertEquals("reachable", health.getDetails().get("status"));
    }

    @Test
    void health_WhenMongoIsDown_ShouldReturnDown() {
        when(mongoTemplate.executeCommand("{ ping: 1 }")).thenThrow(new RuntimeException("Mongo connection failed"));

        Health health = healthIndicator.health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals("MongoDB", health.getDetails().get("database"));
        assertEquals("error", health.getDetails().get("status"));
        assertTrue(health.getDetails().containsKey("error"));
    }
}
