package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.core.Claim;
import com.enterprise.dependency.model.core.ConfidenceScore;
import com.enterprise.dependency.model.sources.CodebaseDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CodebaseAdapter parses Maven dependencies from pom.xml and build files,
 * extracting dependency relationships with high confidence scores.
 * 
 * <p>Supported formats:
 * <ul>
 *   <li>Maven dependencies from pom.xml</li>
 *   <li>Gradle dependencies from build.gradle</li>
 *   <li>Package.json dependencies</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 *   CodebaseAdapter adapter = new CodebaseAdapter();
 *   String pomContent = readPomFile();
 *   List&lt;Claim&gt; claims = adapter.parseDependencies(pomContent);
 * </pre>
 */
@Component
public class CodebaseAdapter {
    private static final Logger logger = LoggerFactory.getLogger(CodebaseAdapter.class);
    
    // Maven dependency pattern
    private static final Pattern MAVEN_DEPENDENCY_PATTERN = Pattern.compile(
        "<dependency>.*?<groupId>([^<]+)</groupId>.*?<artifactId>([^<]+)</artifactId>.*?<version>([^<]+)</version>.*?</dependency>",
        Pattern.DOTALL | Pattern.CASE_INSENSITIVE
    );
    
    // Gradle dependency pattern
    private static final Pattern GRADLE_DEPENDENCY_PATTERN = Pattern.compile(
        "implementation\\s+['\"]([^:]+):([^:]+):([^'\"]+)['\"]"
    );
    
    // Package.json dependency pattern
    private static final Pattern NPM_DEPENDENCY_PATTERN = Pattern.compile(
        "\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\""
    );

    /**
     * Parses codebase dependencies from various build files.
     * 
     * @param content The content of the build file (pom.xml, build.gradle, package.json)
     * @param buildType The type of build file ("maven", "gradle", "npm")
     * @param sourceApplication The application that owns this build file
     * @return List of dependency claims
     */
    public List<Claim> parseDependencies(String content, String buildType, String sourceApplication) {
        if (content == null || content.trim().isEmpty()) {
            logger.warn("Empty content provided for dependency parsing");
            return new ArrayList<>();
        }
        
        logger.info("Parsing {} dependencies for application: {}", buildType, sourceApplication);
        
        List<Claim> claims = new ArrayList<>();
        
        switch (buildType.toLowerCase()) {
            case "maven":
                claims.addAll(parseMavenDependencies(content, sourceApplication));
                break;
            case "gradle":
                claims.addAll(parseGradleDependencies(content, sourceApplication));
                break;
            case "npm":
                claims.addAll(parseNpmDependencies(content, sourceApplication));
                break;
            default:
                logger.warn("Unsupported build type: {}", buildType);
        }
        
        logger.info("Extracted {} dependency claims from {} build file for {}", 
            claims.size(), buildType, sourceApplication);
        
        return claims;
    }
    
    /**
     * Parses Maven dependencies from pom.xml content.
     */
    private List<Claim> parseMavenDependencies(String pomContent, String sourceApplication) {
        List<Claim> claims = new ArrayList<>();
        Matcher matcher = MAVEN_DEPENDENCY_PATTERN.matcher(pomContent);
        
        while (matcher.find()) {
            String groupId = matcher.group(1).trim();
            String artifactId = matcher.group(2).trim();
            String version = matcher.group(3).trim();
            String rawXml = matcher.group(0); // Full match as raw XML
            
            String targetApplication = deriveApplicationName(groupId, artifactId);
            
            CodebaseDependency dependency = CodebaseDependency.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .dependencyType("maven")
                .sourceApplication(sourceApplication)
                .targetApplication(targetApplication)
                .rawXml(rawXml)
                .timestamp(Instant.now())
                .build();
            
            Claim claim = createClaimFromDependency(dependency);
            claims.add(claim);
            
            logger.debug("Parsed Maven dependency: {} -> {}", sourceApplication, targetApplication);
        }
        
        return claims;
    }
    
    /**
     * Parses Gradle dependencies from build.gradle content.
     */
    private List<Claim> parseGradleDependencies(String gradleContent, String sourceApplication) {
        List<Claim> claims = new ArrayList<>();
        Matcher matcher = GRADLE_DEPENDENCY_PATTERN.matcher(gradleContent);
        
        while (matcher.find()) {
            String groupId = matcher.group(1).trim();
            String artifactId = matcher.group(2).trim();
            String version = matcher.group(3).trim();
            String rawGradle = matcher.group(0); // Full match as raw Gradle dependency
            
            String targetApplication = deriveApplicationName(groupId, artifactId);
            
            CodebaseDependency dependency = CodebaseDependency.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .artifactId(artifactId)
                .version(version)
                .dependencyType("gradle")
                .sourceApplication(sourceApplication)
                .targetApplication(targetApplication)
                .rawXml(rawGradle) // Store gradle syntax in rawXml field
                .timestamp(Instant.now())
                .build();
            
            Claim claim = createClaimFromDependency(dependency);
            claims.add(claim);
            
            logger.debug("Parsed Gradle dependency: {} -> {}", sourceApplication, targetApplication);
        }
        
        return claims;
    }
    
    /**
     * Parses NPM dependencies from package.json content.
     */
    private List<Claim> parseNpmDependencies(String packageJsonContent, String sourceApplication) {
        List<Claim> claims = new ArrayList<>();
        
        // Extract dependencies section
        int dependenciesStart = packageJsonContent.indexOf("\"dependencies\"");
        if (dependenciesStart == -1) {
            logger.debug("No dependencies section found in package.json");
            return claims;
        }
        
        // Find the dependencies object
        int braceStart = packageJsonContent.indexOf("{", dependenciesStart);
        int braceEnd = findMatchingBrace(packageJsonContent, braceStart);
        
        if (braceStart != -1 && braceEnd != -1) {
            String dependenciesSection = packageJsonContent.substring(braceStart + 1, braceEnd);
            Matcher matcher = NPM_DEPENDENCY_PATTERN.matcher(dependenciesSection);
            
            while (matcher.find()) {
                String packageName = matcher.group(1).trim();
                String version = matcher.group(2).trim();
                String rawJson = matcher.group(0); // Full match as raw JSON dependency
                
                String targetApplication = deriveApplicationName("npm", packageName);
                
                CodebaseDependency dependency = CodebaseDependency.builder()
                    .id(UUID.randomUUID().toString())
                    .groupId("npm")
                    .artifactId(packageName)
                    .version(version)
                    .dependencyType("npm")
                    .sourceApplication(sourceApplication)
                    .targetApplication(targetApplication)
                    .rawXml(rawJson) // Store JSON syntax in rawXml field
                    .timestamp(Instant.now())
                    .build();
                
                Claim claim = createClaimFromDependency(dependency);
                claims.add(claim);
                
                logger.debug("Parsed NPM dependency: {} -> {}", sourceApplication, targetApplication);
            }
        }
        
        return claims;
    }
    
    /**
     * Derives an application name from groupId and artifactId.
     */
    private String deriveApplicationName(String groupId, String artifactId) {
        // Remove common prefixes/suffixes and normalize
        String name = artifactId
            .replaceAll("(-client|-service|-api|-lib|-library|-common)$", "")
            .replaceAll("^(lib-|api-|service-)", "");
        
        // If from enterprise package, use artifactId as app name
        if (groupId.contains("enterprise") || groupId.contains("company")) {
            return name;
        }
        
        // For external dependencies, use full groupId.artifactId
        return groupId + "." + name;
    }
    
    /**
     * Creates a Claim from a CodebaseDependency.
     */
    private Claim createClaimFromDependency(CodebaseDependency dependency) {
        String processedData = String.format("%s -> %s", 
            dependency.getSourceApplication(), 
            dependency.getTargetApplication());
        
        String rawData = String.format("%s:%s:%s (%s)", 
            dependency.getGroupId(), 
            dependency.getArtifactId(), 
            dependency.getVersion(),
            dependency.getDependencyType());
        
        return Claim.builder()
            .id("codebase_" + dependency.getId())
            .sourceType("CODEBASE")
            .rawData(rawData)
            .processedData(processedData)
            .timestamp(dependency.getTimestamp())
            .confidenceScore(ConfidenceScore.of(0.95)) // High confidence for codebase dependencies
            .build();
    }
    
    /**
     * Finds the matching closing brace for a given opening brace position.
     */
    private int findMatchingBrace(String content, int openBracePos) {
        int braceCount = 1;
        for (int i = openBracePos + 1; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') braceCount++;
            else if (c == '}') {
                braceCount--;
                if (braceCount == 0) return i;
            }
        }
        return -1; // No matching brace found
    }
    
    /**
     * Parses codebase dependencies from string data.
     * Expected format: "buildType:groupId:artifactId:version" 
     * Examples: "maven:com.enterprise:user-service-client:2.1.0"
     * 
     * @param dependencyData List of dependency strings
     * @return List of claims extracted from dependency data
     */
    public List<Claim> parseCodebaseDependencies(List<String> dependencyData) {
        List<Claim> claims = new ArrayList<>();
        
        if (dependencyData == null || dependencyData.isEmpty()) {
            logger.warn("No codebase dependency data provided for parsing");
            return claims;
        }
        
        logger.info("Parsing {} codebase dependency entries", dependencyData.size());
        
        for (int i = 0; i < dependencyData.size(); i++) {
            String depString = dependencyData.get(i);
            try {
                Claim claim = parseSimpleDependencyString(depString);
                if (claim != null) {
                    claims.add(claim);
                    logger.debug("Parsed dependency claim from line {}: {}", i + 1, claim);
                } else {
                    logger.debug("Could not parse dependency string: {}", depString);
                }
            } catch (Exception e) {
                logger.warn("Failed to parse dependency string {}: {}", i + 1, depString, e);
            }
        }
        
        logger.info("Successfully parsed {} claims from codebase dependency data", claims.size());
        return claims;
    }
    
    /**
     * Parses a simple dependency string in format "buildType:groupId:artifactId:version"
     * or Maven XML format
     */
    private Claim parseSimpleDependencyString(String depString) {
        if (depString == null || depString.trim().isEmpty()) {
            return null;
        }
        
        // Handle XML format dependencies
        if (depString.trim().startsWith("<dependency>")) {
            return parseXmlDependency(depString);
        }
        
        // Handle simple format: maven:groupId:artifactId:version
        String[] parts = depString.trim().split(":");
        if (parts.length < 3) {
            logger.debug("Invalid dependency format: {}", depString);
            return null;
        }
        
        String buildType = parts.length >= 4 ? parts[0] : "unknown";
        String groupId = parts.length >= 4 ? parts[1] : parts[0];
        String artifactId = parts.length >= 4 ? parts[2] : parts[1];
        String version;
        if (parts.length >= 4) {
            version = parts[3];
        } else if (parts.length >= 3) {
            version = parts[2];
        } else {
            version = "unknown";
        }
        
        return createClaimFromDependency(buildType, groupId, artifactId, version, depString);
    }
    
    /**
     * Parse XML format dependency
     */
    private Claim parseXmlDependency(String xmlString) {
        Matcher matcher = MAVEN_DEPENDENCY_PATTERN.matcher(xmlString);
        if (matcher.find()) {
            String groupId = matcher.group(1).trim();
            String artifactId = matcher.group(2).trim();
            String version = matcher.group(3).trim();
            
            return createClaimFromDependency("maven", groupId, artifactId, version, xmlString);
        }
        
        logger.debug("Could not parse XML dependency: {}", xmlString);
        return null;
    }
    
    /**
     * Create a claim from dependency information
     */
    private Claim createClaimFromDependency(String buildType, String groupId, String artifactId, String version, String rawData) {
        // Extract meaningful service names
        String sourceApp = extractSourceApplication(groupId);
        String targetApp = extractTargetApplication(artifactId);
        
        // Create CodebaseDependency
        CodebaseDependency dependency = CodebaseDependency.builder()
            .id(UUID.randomUUID().toString())
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .dependencyType(buildType)
            .sourceApplication(sourceApp)
            .targetApplication(targetApp)
            .timestamp(Instant.now())
            .rawXml(rawData)
            .build();
        
        // Convert to claim
        return Claim.builder()
            .id("codebase_" + UUID.randomUUID().toString())
            .sourceType("CODEBASE")
            .rawData(rawData)
            .processedData(dependency.getSourceApplication() + " -> " + dependency.getTargetApplication())
            .timestamp(Instant.now())
            .confidenceScore(ConfidenceScore.of(0.9)) // High confidence for codebase dependencies
            .build();
    }
    
    /**
     * Extract source application from group ID
     */
    private String extractSourceApplication(String groupId) {
        if (groupId == null) return "unknown-app";
        
        // Extract company/organization and assume it's the source system
        if (groupId.contains(".")) {
            String[] parts = groupId.split("\\.");
            return parts[parts.length - 1] + "-system";
        }
        
        return groupId + "-app";
    }
    
    /**
     * Extracts target application name from artifact ID
     */
    private String extractTargetApplication(String artifactId) {
        if (artifactId == null) return "unknown-service";
        
        // Remove common suffixes and normalize
        String appName = artifactId
            .replaceAll("(-client|-api|-service|-lib|-core)$", "")
            .replace("-", " ")
            .trim();
        
        return appName.isEmpty() ? artifactId : appName + "-service";
    }
}
