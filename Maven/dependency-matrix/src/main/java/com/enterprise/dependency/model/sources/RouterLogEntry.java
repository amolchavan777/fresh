package com.enterprise.dependency.model.sources;

import java.time.LocalDateTime;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a single entry from a router log, used for dependency inference.
 * <p>
 * Example:
 * <pre>
 *   RouterLogEntry entry = RouterLogEntry.builder()
 *       .timestamp(LocalDateTime.now())
 *       .sourceIp("192.168.1.100")
 *       .targetIp("192.168.1.200")
 *       .targetPort(8080)
 *       .protocol("HTTP")
 *       .method("GET")
 *       .endpoint("/api/users")
 *       .statusCode(200)
 *       .responseTimeMs(125)
 *       .rawLine("2024-07-04 10:30:45 [INFO] ...")
 *       .build();
 * </pre>
 */
public class RouterLogEntry {
    private static final Logger logger = LoggerFactory.getLogger(RouterLogEntry.class);

    @NotNull
    private final LocalDateTime timestamp;
    @NotBlank
    private final String sourceIp;
    @NotBlank
    private final String targetIp;
    private final int targetPort;
    @NotBlank
    private final String protocol;
    private final String method;
    private final String endpoint;
    private final Integer statusCode;
    private final Integer responseTimeMs;
    @NotBlank
    private final String rawLine;

    private RouterLogEntry(Builder builder) {
        this.timestamp = builder.timestamp;
        this.sourceIp = builder.sourceIp;
        this.targetIp = builder.targetIp;
        this.targetPort = builder.targetPort;
        this.protocol = builder.protocol;
        this.method = builder.method;
        this.endpoint = builder.endpoint;
        this.statusCode = builder.statusCode;
        this.responseTimeMs = builder.responseTimeMs;
        this.rawLine = builder.rawLine;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (timestamp == null) throw new IllegalArgumentException("timestamp is required");
        if (sourceIp == null || sourceIp.isEmpty()) throw new IllegalArgumentException("sourceIp is required");
        if (targetIp == null || targetIp.isEmpty()) throw new IllegalArgumentException("targetIp is required");
        if (protocol == null || protocol.isEmpty()) throw new IllegalArgumentException("protocol is required");
        if (rawLine == null || rawLine.isEmpty()) throw new IllegalArgumentException("rawLine is required");
        logger.debug("Validated RouterLogEntry: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public LocalDateTime getTimestamp() { return timestamp; }
    public String getSourceIp() { return sourceIp; }
    public String getTargetIp() { return targetIp; }
    public int getTargetPort() { return targetPort; }
    public String getProtocol() { return protocol; }
    public String getMethod() { return method; }
    public String getEndpoint() { return endpoint; }
    public Integer getStatusCode() { return statusCode; }
    public Integer getResponseTimeMs() { return responseTimeMs; }
    public String getRawLine() { return rawLine; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RouterLogEntry that = (RouterLogEntry) o;
        return Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(sourceIp, that.sourceIp) &&
               Objects.equals(targetIp, that.targetIp) &&
               Objects.equals(rawLine, that.rawLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timestamp, sourceIp, targetIp, rawLine);
    }

    @Override
    public String toString() {
        return "RouterLogEntry{" +
                "timestamp=" + timestamp +
                ", sourceIp='" + sourceIp + '\'' +
                ", targetIp='" + targetIp + '\'' +
                ", targetPort=" + targetPort +
                ", protocol='" + protocol + '\'' +
                ", method='" + method + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", statusCode=" + statusCode +
                ", responseTimeMs=" + responseTimeMs +
                ", rawLine='" + rawLine + '\'' +
                '}';
    }

    /**
     * Builder for RouterLogEntry.
     */
    public static class Builder {
        private LocalDateTime timestamp;
        private String sourceIp;
        private String targetIp;
        private int targetPort;
        private String protocol;
        private String method;
        private String endpoint;
        private Integer statusCode;
        private Integer responseTimeMs;
        private String rawLine;

        public Builder timestamp(LocalDateTime timestamp) { this.timestamp = timestamp; return this; }
        public Builder sourceIp(String sourceIp) { this.sourceIp = sourceIp; return this; }
        public Builder targetIp(String targetIp) { this.targetIp = targetIp; return this; }
        public Builder targetPort(int targetPort) { this.targetPort = targetPort; return this; }
        public Builder protocol(String protocol) { this.protocol = protocol; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder endpoint(String endpoint) { this.endpoint = endpoint; return this; }
        public Builder statusCode(Integer statusCode) { this.statusCode = statusCode; return this; }
        public Builder responseTimeMs(Integer responseTimeMs) { this.responseTimeMs = responseTimeMs; return this; }
        public Builder rawLine(String rawLine) { this.rawLine = rawLine; return this; }
        public RouterLogEntry build() { return new RouterLogEntry(this); }
    }
}
