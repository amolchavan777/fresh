package com.enterprise.dependency.model.sources;

import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a call record from an API Gateway.
 * <p>
 * Example:
 * <pre>
 *   ApiGatewayCall call = ApiGatewayCall.builder()
 *       .timestamp(Instant.now())
 *       .sourceService("web-portal")
 *       .targetService("user-service")
 *       .endpoint("/api/users")
 *       .method("GET")
 *       .responseTime(125)
 *       .build();
 * </pre>
 */
public class ApiGatewayCall {
    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayCall.class);

    @NotNull
    private final Instant timestamp;
    @NotBlank
    private final String sourceService;
    @NotBlank
    private final String targetService;
    @NotBlank
    private final String endpoint;
    @NotBlank
    private final String method;
    private final Integer responseTime;

    private ApiGatewayCall(Builder builder) {
        this.timestamp = builder.timestamp;
        this.sourceService = builder.sourceService;
        this.targetService = builder.targetService;
        this.endpoint = builder.endpoint;
        this.method = builder.method;
        this.responseTime = builder.responseTime;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (timestamp == null) throw new IllegalArgumentException("timestamp is required");
        if (sourceService == null || sourceService.isEmpty()) throw new IllegalArgumentException("sourceService is required");
        if (targetService == null || targetService.isEmpty()) throw new IllegalArgumentException("targetService is required");
        if (endpoint == null || endpoint.isEmpty()) throw new IllegalArgumentException("endpoint is required");
        if (method == null || method.isEmpty()) throw new IllegalArgumentException("method is required");
        logger.debug("Validated ApiGatewayCall: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public Instant getTimestamp() { return timestamp; }
    public String getSourceService() { return sourceService; }
    public String getTargetService() { return targetService; }
    public String getEndpoint() { return endpoint; }
    public String getMethod() { return method; }
    public Integer getResponseTime() { return responseTime; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiGatewayCall that = (ApiGatewayCall) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(sourceService, that.sourceService) &&
               Objects.equals(targetService, that.targetService) &&
               Objects.equals(endpoint, that.endpoint) &&
               Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, sourceService, targetService, endpoint, method);
    }

    @Override
    public String toString() {
        return "ApiGatewayCall{" +
                "timestamp=" + timestamp +
                ", sourceService='" + sourceService + '\'' +
                ", targetService='" + targetService + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", method='" + method + '\'' +
                ", responseTime=" + responseTime +
                '}';
    }

    /**
     * Builder for ApiGatewayCall.
     */
    public static class Builder {
        private Instant timestamp;
        private String sourceService;
        private String targetService;
        private String endpoint;
        private String method;
        private Integer responseTime;

        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder sourceService(String sourceService) { this.sourceService = sourceService; return this; }
        public Builder targetService(String targetService) { this.targetService = targetService; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder responseTime(Integer responseTime) { this.responseTime = responseTime; return this; }
        public ApiGatewayCall build() { return new ApiGatewayCall(this); }
    }
}
