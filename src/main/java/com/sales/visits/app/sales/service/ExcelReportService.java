package com.sales.visits.app.sales.service;

import com.sales.visits.app.sales.dto.response.VisitReviewDetailResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExcelReportService {
    public byte[] generateVisitsReport(List<VisitReviewDetailResponse> visits) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Visits");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            CellStyle wrapStyle = workbook.createCellStyle();
            wrapStyle.setWrapText(true);

            String[] headers = {
                    "Organization", "Type", "Submitted By", "Visit Date",
                    "Impression", "Physicians (Name / NPI)", "Notes"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (VisitReviewDetailResponse v : visits) {
                Row row = sheet.createRow(rowIdx++);

                row.createCell(0).setCellValue(v.account() != null ? v.account().organizationName() : "");
                row.createCell(1).setCellValue(v.visitType() != null ? v.visitType().name() : "");
                row.createCell(2).setCellValue(v.submittedByUsername() != null ? v.submittedByUsername() : "");
                row.createCell(3).setCellValue(v.visitDate() != null ? v.visitDate().toString() : "");
                row.createCell(4).setCellValue(v.visitImpression() != null ? v.visitImpression().name() : "");

                String physiciansSummary = v.physicians() != null && !v.physicians().isEmpty()
                        ? v.physicians().stream()
                        .map(p -> p.name() + " (NPI: " + p.npi() + ")")
                        .collect(Collectors.joining("\n"))
                        : "";
                Cell physiciansCell = row.createCell(5);
                physiciansCell.setCellValue(physiciansSummary);
                physiciansCell.setCellStyle(wrapStyle);

                row.createCell(6).setCellValue(v.notes() != null ? v.notes() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            for (int i = 1; i < rowIdx; i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    row.setHeight((short) -1);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
