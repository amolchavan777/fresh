package com.enterprise.dependency.model.core;

import java.time.Instant;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a claim about a dependency, as produced by a data source adapter.
 * <p>
 * Example:
 * <pre>
 *   Claim claim = Claim.builder()
 *       .id("claim-001")
 *       .sourceType("ROUTER_LOG")
 *       .rawData("2024-07-04 10:30:45 ...")
 *       .processedData("app-001 -> app-002")
 *       .timestamp(Instant.now())
 *       .build();
 * </pre>
 */
public class Claim {
    private static final Logger logger = LoggerFactory.getLogger(Claim.class);

    @NotNull
    private final String id;
    @NotNull
    private final String sourceType;
    @NotNull
    private final String rawData;
    @NotNull
    private final String processedData;
    @NotNull
    private final Instant timestamp;
    private final ConfidenceScore confidenceScore;

    private Claim(Builder builder) {
        this.id = builder.id;
        this.sourceType = builder.sourceType;
        this.rawData = builder.rawData;
        this.processedData = builder.processedData;
        this.timestamp = builder.timestamp;
        this.confidenceScore = builder.confidenceScore;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id is required");
        if (sourceType == null || sourceType.isEmpty()) throw new IllegalArgumentException("sourceType is required");
        if (rawData == null || rawData.isEmpty()) throw new IllegalArgumentException("rawData is required");
        if (processedData == null || processedData.isEmpty()) throw new IllegalArgumentException("processedData is required");
        if (timestamp == null) throw new IllegalArgumentException("timestamp is required");
        logger.debug("Validated Claim: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public String getId() { return id; }
    public String getSourceType() { return sourceType; }
    public String getRawData() { return rawData; }
    public String getProcessedData() { return processedData; }
    public Instant getTimestamp() { return timestamp; }
    public ConfidenceScore getConfidenceScore() { return confidenceScore; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return Objects.equals(id, claim.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Claim{" +
                "id='" + id + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", rawData='" + rawData + '\'' +
                ", processedData='" + processedData + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    /**
     * Builder for Claim.
     */
    public static class Builder {
        private String id;
        private String sourceType;
        private String rawData;
        private String processedData;
        private Instant timestamp;
        private ConfidenceScore confidenceScore;

        public Builder id(String id) { this.id = id; return this; }
        public Builder sourceType(String sourceType) { this.sourceType = sourceType; return this; }
        public Builder rawData(String rawData) { this.rawData = rawData; return this; }
        public Builder processedData(String processedData) { this.processedData = processedData; return this; }
        public Builder timestamp(Instant timestamp) { this.timestamp = timestamp; return this; }
        public Builder confidenceScore(ConfidenceScore confidenceScore) { this.confidenceScore = confidenceScore; return this; }
        public Claim build() { return new Claim(this); }
    }
}
