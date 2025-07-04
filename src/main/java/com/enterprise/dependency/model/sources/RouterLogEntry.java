package com.enterprise.dependency.model.sources;

import java.time.LocalDateTime;

/**
 * Represents a single router log entry for dependency discovery.
 * <p>
 * Example usage:
 * <pre>
 *   RouterLogEntry entry = new RouterLogEntry();
 *   entry.setTimestamp(LocalDateTime.now());
 *   entry.setSourceIp("192.168.1.100");
 *   entry.setTargetIp("192.168.1.200");
 *   entry.setPort(8080);
 *   entry.setProtocol("HTTP");
 *   entry.setRequest("GET /api/users");
 *   entry.setResponseCode(200);
 *   entry.setResponseTimeMs(125);
 * </pre>
 */
public class RouterLogEntry {
    private LocalDateTime timestamp;
    private String sourceIp;
    private String targetIp;
    private int port;
    private String protocol;
    private String request;
    private int responseCode;
    private int responseTimeMs;

    // Getters and setters omitted for brevity
    // TODO: Add validation logic for IPs, port, and response codes
}
