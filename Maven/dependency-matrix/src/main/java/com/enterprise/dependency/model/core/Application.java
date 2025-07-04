package com.enterprise.dependency.model.core;

import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an application in the dependency matrix system.
 * <p>
 * Example:
 * <pre>
 *   Application app = Application.builder()
 *       .id("app-001")
 *       .name("User Service")
 *       .type("microservice")
 *       .environment("prod")
 *       .owner("team-a")
 *       .build();
 * </pre>
 */
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @NotBlank
    private final String id;
    @NotBlank
    private final String name;
    @NotBlank
    private final String type;
    @NotBlank
    private final String environment;
    @NotBlank
    private final String owner;

    private Application(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.environment = builder.environment;
        this.owner = builder.owner;
        validate();
    }

    private void validate() {
        // TODO: Add more comprehensive validation logic
        if (id == null || id.isEmpty()) throw new IllegalArgumentException("id is required");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("name is required");
        if (type == null || type.isEmpty()) throw new IllegalArgumentException("type is required");
        if (environment == null || environment.isEmpty()) throw new IllegalArgumentException("environment is required");
        if (owner == null || owner.isEmpty()) throw new IllegalArgumentException("owner is required");
        logger.debug("Validated Application: {}", this);
    }

    public static Builder builder() { return new Builder(); }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getEnvironment() { return environment; }
    public String getOwner() { return owner; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Application that = (Application) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "Application{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", environment='" + environment + '\'' +
                ", owner='" + owner + '\'' +
                '}';
    }

    /**
     * Builder for Application.
     */
    public static class Builder {
        private String id;
        private String name;
        private String type;
        private String environment;
        private String owner;

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder type(String type) { this.type = type; return this; }
        public Builder environment(String environment) { this.environment = environment; return this; }
        public Builder owner(String owner) { this.owner = owner; return this; }
        public Application build() { return new Application(this); }
    }
}
