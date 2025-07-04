package com.enterprise.dependency.model.sources;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ApiGatewayCall}.
 */
class ApiGatewayCallTest {
    @Test
    void builderShouldCreateValidCall() {
        ApiGatewayCall call = ApiGatewayCall.builder()
                .timestamp(Instant.now())
                .sourceService("web-portal")
                .targetService("user-service")
                .endpoint("/api/users")
                .method("GET")
                .responseTime(125)
                .build();
        assertEquals("web-portal", call.getSourceService());
        assertEquals("user-service", call.getTargetService());
        assertEquals("/api/users", call.getEndpoint());
        assertEquals("GET", call.getMethod());
        assertEquals(125, call.getResponseTime());
        assertNotNull(call.getTimestamp());
    }

    @Test
    void builderShouldThrowOnMissingFields() {
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
            ApiGatewayCall.builder().build()
        );
        assertTrue(ex.getMessage().contains("timestamp"));
    }
}
