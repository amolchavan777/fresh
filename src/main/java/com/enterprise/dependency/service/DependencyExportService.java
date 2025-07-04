package com.enterprise.dependency.service;

import com.enterprise.dependency.model.DependencyClaim;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Utility for exporting DependencyClaims to CSV and Excel.
 */
@Component
public class DependencyExportService {
    private static final Logger logger = LoggerFactory.getLogger(DependencyExportService.class);

    public void exportToCsv(List<DependencyClaim> claims, Path filePath) throws IOException {
        logger.info("Exporting {} DependencyClaims to CSV: {}", claims.size(), filePath);
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath.toFile()))) {
            String[] header = {"Source", "Target", "Type", "SourceType", "Confidence", "Timestamp", "SourceId"};
            writer.writeNext(header);
            for (DependencyClaim c : claims) {
                writer.writeNext(new String[] {
                        c.getSourceApplication(),
                        c.getTargetApplication(),
                        c.getDependencyType().name(),
                        c.getSourceType().name(),
                        String.valueOf(c.getConfidenceScore()),
                        c.getTimestamp().toString(),
                        c.getSourceIdentifier()
                });
            }
        }
    }

    public void exportToExcel(List<DependencyClaim> claims, Path filePath) throws IOException {
        logger.info("Exporting {} DependencyClaims to Excel: {}", claims.size(), filePath);
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("DependencyClaims");
            Row header = sheet.createRow(0);
            String[] columns = {"Source", "Target", "Type", "SourceType", "Confidence", "Timestamp", "SourceId"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (DependencyClaim c : claims) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getSourceApplication());
                row.createCell(1).setCellValue(c.getTargetApplication());
                row.createCell(2).setCellValue(c.getDependencyType().name());
                row.createCell(3).setCellValue(c.getSourceType().name());
                row.createCell(4).setCellValue(c.getConfidenceScore());
                row.createCell(5).setCellValue(c.getTimestamp().toString());
                row.createCell(6).setCellValue(c.getSourceIdentifier());
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(filePath.toFile())) {
                workbook.write(fos);
            }
        }
    }
}
