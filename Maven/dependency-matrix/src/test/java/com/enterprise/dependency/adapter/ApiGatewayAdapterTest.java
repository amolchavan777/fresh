package com.enterprise.dependency.adapter;

import com.enterprise.dependency.model.core.Claim;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ApiGatewayAdapter.
 */
class ApiGatewayAdapterTest {
    
    private ApiGatewayAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new ApiGatewayAdapter();
    }
    
    @Test
    @DisplayName("Should parse JSON formatted API Gateway logs")
    void shouldParseJsonLogs() {
        String jsonLogs = "{\"timestamp\":\"2024-01-15T10:30:00Z\",\"method\":\"GET\",\"path\":\"/api/v1/users\",\"sourceService\":\"web-portal\",\"responseTime\":125}\n" +
                         "{\"timestamp\":\"2024-01-15T10:31:00Z\",\"method\":\"POST\",\"path\":\"/api/v1/orders\",\"sourceService\":\"mobile-app\",\"responseTime\":250}";
        
        List<Claim> claims = adapter.parseApiCalls(jsonLogs, "json");
        
        assertEquals(2, claims.size());
        
        Claim claim1 = claims.get(0);
        assertTrue(claim1.getId().startsWith("apigateway_"));
        assertEquals("API_GATEWAY", claim1.getSourceType());
        assertEquals("web-portal -> users-service", claim1.getProcessedData());
        assertTrue(claim1.getRawData().contains("GET /api/v1/users"));
        assertTrue(claim1.getConfidenceScore().getValue() > 0.7);
        
        Claim claim2 = claims.get(1);
        assertEquals("mobile-app -> orders-service", claim2.getProcessedData());
        assertTrue(claim2.getRawData().contains("POST /api/v1/orders"));
    }
    
    @Test
    @DisplayName("Should parse Common Log Format API Gateway logs")
    void shouldParseClfLogs() {
        String clfLogs = "192.168.1.100 - - [15/Jan/2024:10:30:00 +0000] \"GET /api/v1/users HTTP/1.1\" 200 1234 \"-\" \"UserService/1.0\" 125ms\n" +
                        "10.0.0.50 - - [15/Jan/2024:10:31:00 +0000] \"POST /api/v1/orders HTTP/1.1\" 201 567 \"-\" \"OrderClient/2.0\" 250ms";
        
        List<Claim> claims = adapter.parseApiCalls(clfLogs, "clf");
        
        assertEquals(2, claims.size());
        
        Claim claim1 = claims.get(0);
        assertTrue(claim1.getProcessedData().contains("-> users-service"));
        assertTrue(claim1.getRawData().contains("GET /api/v1/users"));
        
        Claim claim2 = claims.get(1);
        assertTrue(claim2.getProcessedData().contains("-> orders-service"));
        assertTrue(claim2.getRawData().contains("POST /api/v1/orders"));
    }
    
    @Test
    @DisplayName("Should handle empty and invalid log data")
    void shouldHandleEmptyAndInvalidLogs() {
        // Empty data
        List<Claim> emptyClaims = adapter.parseApiCalls("", "json");
        assertTrue(emptyClaims.isEmpty());
        
        // Null data
        List<Claim> nullClaims = adapter.parseApiCalls(null, "json");
        assertTrue(nullClaims.isEmpty());
        
        // Invalid JSON
        List<Claim> invalidJsonClaims = adapter.parseApiCalls("invalid json data", "json");
        assertTrue(invalidJsonClaims.isEmpty());
        
        // Unsupported format
        List<Claim> unsupportedClaims = adapter.parseApiCalls("some data", "unsupported");
        assertTrue(unsupportedClaims.isEmpty());
    }
    
    @Test
    @DisplayName("Should extract target service from various endpoint patterns")
    void shouldExtractTargetServiceFromEndpoints() {
        String jsonWithDifferentEndpoints = "{\"timestamp\":\"2024-01-15T10:30:00Z\",\"method\":\"GET\",\"path\":\"/api/users\",\"sourceService\":\"client\"}\n" +
                                          "{\"timestamp\":\"2024-01-15T10:31:00Z\",\"method\":\"GET\",\"path\":\"/v1/products\",\"sourceService\":\"client\"}\n" +
                                          "{\"timestamp\":\"2024-01-15T10:32:00Z\",\"method\":\"GET\",\"path\":\"/orders/123\",\"sourceService\":\"client\"}\n" +
                                          "{\"timestamp\":\"2024-01-15T10:33:00Z\",\"method\":\"GET\",\"path\":\"/unknown/path/here\",\"sourceService\":\"client\"}";
        
        List<Claim> claims = adapter.parseApiCalls(jsonWithDifferentEndpoints, "json");
        
        assertEquals(4, claims.size());
        assertEquals("client -> users-service", claims.get(0).getProcessedData());
        assertEquals("client -> products-service", claims.get(1).getProcessedData());
        assertEquals("client -> orders-service", claims.get(2).getProcessedData());
        assertEquals("client -> unknown-service", claims.get(3).getProcessedData());
    }
    
    @Test
    @DisplayName("Should calculate confidence scores based on call characteristics")
    void shouldCalculateConfidenceScores() {
        String jsonWithVariousQuality = "{\"timestamp\":\"2024-01-15T10:30:00Z\",\"method\":\"GET\",\"path\":\"/api/v1/users\",\"sourceService\":\"web-portal\",\"responseTime\":125}\n" +
                                       "{\"timestamp\":\"2024-01-15T10:31:00Z\",\"method\":\"UNKNOWN\",\"path\":\"/weird-path\",\"sourceService\":\"unknown\",\"responseTime\":5}\n" +
                                       "{\"timestamp\":\"2024-01-15T10:32:00Z\",\"method\":\"POST\",\"path\":\"/api/v2/orders\",\"sourceService\":\"mobile-app\",\"responseTime\":15000}";
        
        List<Claim> claims = adapter.parseApiCalls(jsonWithVariousQuality, "json");
        
        assertEquals(3, claims.size());
        
        // Well-structured API call should have high confidence
        assertTrue(claims.get(0).getConfidenceScore().getValue() > 0.8);
        
        // Poor quality call should have lower confidence
        assertTrue(claims.get(1).getConfidenceScore().getValue() < 0.7);
        
        // Slow response should have moderate confidence
        double slowResponseConfidence = claims.get(2).getConfidenceScore().getValue();
        assertTrue(slowResponseConfidence > 0.5 && slowResponseConfidence < 0.9);
    }
    
    @Test
    @DisplayName("Should handle AWS CloudWatch format")
    void shouldHandleAwsCloudWatchFormat() {
        String awsCloudWatchLog = "{\"eventTime\":\"2024-01-15T10:30:00.000Z\",\"httpMethod\":\"GET\",\"resource\":\"/api/users\",\"requestId\":\"123-456\",\"responseTime\":125}";
        
        List<Claim> claims = adapter.parseApiCalls(awsCloudWatchLog, "aws-cloudwatch");
        
        assertEquals(1, claims.size());
        Claim claim = claims.get(0);
        assertTrue(claim.getRawData().contains("GET /api/users"));
        assertTrue(claim.getProcessedData().contains("-> users-service"));
    }
    
    @Test
    @DisplayName("Should handle incomplete log entries gracefully")
    void shouldHandleIncompleteLogEntries() {
        String incompleteJsonLogs = "{\"timestamp\":\"2024-01-15T10:30:00Z\"}\n" +
                                   "{\"method\":\"GET\",\"path\":\"/api/users\"}\n" +
                                   "{\"timestamp\":\"2024-01-15T10:31:00Z\",\"method\":\"POST\",\"path\":\"/api/orders\",\"sourceService\":\"client\"}";
        
        List<Claim> claims = adapter.parseApiCalls(incompleteJsonLogs, "json");
        
        // Only the complete entry should be processed
        assertEquals(1, claims.size());
        assertEquals("client -> orders-service", claims.get(0).getProcessedData());
    }
    
    @Test
    @DisplayName("Should derive source service from user agent patterns")
    void shouldDeriveSourceFromUserAgent() {
        String clfWithUserAgents = "192.168.1.100 - - [15/Jan/2024:10:30:00 +0000] \"GET /api/users HTTP/1.1\" 200 1234 \"-\" \"mobile-client/1.0\" 125ms\n" +
                                  "10.0.0.50 - - [15/Jan/2024:10:31:00 +0000] \"POST /api/orders HTTP/1.1\" 201 567 \"-\" \"order-service-proxy/2.0\" 250ms";
        
        List<Claim> claims = adapter.parseApiCalls(clfWithUserAgents, "clf");
        
        assertEquals(2, claims.size());
        
        // Should extract service names from user agents
        assertTrue(claims.get(0).getProcessedData().contains("mobile-client ->"));
        assertTrue(claims.get(1).getProcessedData().contains("order-service-proxy ->"));
    }
}
