package com.enterprise.dependency.model.sources;

import java.time.LocalDateTime;

/**
 * Represents an API gateway call for dependency discovery.
 * <p>
 * Example usage:
 * <pre>
 *   ApiGatewayCall call = new ApiGatewayCall();
 *   call.setTimestamp(LocalDateTime.now());
 *   call.setSourceService("web-portal");
 *   call.setTargetService("user-service");
 *   call.setEndpoint("/api/users");
 *   call.setMethod("GET");
 *   call.setResponseTime(125);
 * </pre>
 */
public class ApiGatewayCall {
    private LocalDateTime timestamp;
    private String sourceService;
    private String targetService;
    private String endpoint;
    private String method;
    private int responseTime;

    // Getters and setters omitted for brevity
    // TODO: Add validation logic for method, endpoint, and responseTime
}
