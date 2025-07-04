package com.enterprise.dependency;

import com.enterprise.dependency.model.DependencyClaim;
import com.enterprise.dependency.model.DependencyType;
import com.enterprise.dependency.model.SourceType;
import com.enterprise.dependency.service.DependencyClaimService;
import com.enterprise.dependency.service.DependencyExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

/**
 * CommandLineRunner to demonstrate data ingestion, console output, and export.
 */
@Configuration
public class DemoRunnerConfig {
    private static final Logger logger = LoggerFactory.getLogger(DemoRunnerConfig.class);

    @Bean
    public CommandLineRunner demoRunner(DependencyClaimService claimService, DependencyExportService exportService) {
        return args -> {
            logger.info("--- Ingesting sample DependencyClaims ---");
            DependencyClaim claim1 = new DependencyClaim();
            claim1.setSourceApplication("web-portal");
            claim1.setTargetApplication("user-service");
            claim1.setDependencyType(DependencyType.RUNTIME);
            claim1.setSourceType(SourceType.LOG);
            claim1.setSourceDetails("{\"protocol\":\"HTTP\"}");
            claim1.setTimestamp(LocalDateTime.now());
            claim1.setConfidenceScore(0.9f);
            claim1.setSourceIdentifier("router-1");
            claimService.save(claim1);

            DependencyClaim claim2 = new DependencyClaim();
            claim2.setSourceApplication("user-service");
            claim2.setTargetApplication("db-service");
            claim2.setDependencyType(DependencyType.RUNTIME);
            claim2.setSourceType(SourceType.CODEBASE);
            claim2.setSourceDetails("<dependency>...</dependency>");
            claim2.setTimestamp(LocalDateTime.now());
            claim2.setConfidenceScore(0.95f);
            claim2.setSourceIdentifier("codebase-1");
            claimService.save(claim2);

            logger.info("--- Printing Dependency Matrix to Console ---");
            List<DependencyClaim> claims = claimService.findAll();
            System.out.printf("%-20s %-20s %-10s %-10s %-5s\n", "Source", "Target", "Type", "Source", "Conf");
            for (DependencyClaim c : claims) {
                System.out.printf("%-20s %-20s %-10s %-10s %-5.2f\n",
                        c.getSourceApplication(), c.getTargetApplication(),
                        c.getDependencyType(), c.getSourceType(), c.getConfidenceScore());
            }
            // Export to CSV and Excel
            try {
                exportService.exportToCsv(claims, Paths.get("exports/dependency_matrix.csv"));
                exportService.exportToExcel(claims, Paths.get("exports/dependency_matrix.xlsx"));
                logger.info("Exported dependency matrix to exports/dependency_matrix.csv and .xlsx");
            } catch (Exception e) {
                logger.error("Failed to export dependency matrix: {}", e.getMessage(), e);
            }
        };
    }
}
