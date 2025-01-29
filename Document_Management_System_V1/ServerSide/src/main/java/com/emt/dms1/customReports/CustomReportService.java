package com.emt.dms1.customReports;

import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.export.JasperXlsExporterBuilder;
import net.sf.dynamicreports.jasper.constant.JasperProperty;
import net.sf.dynamicreports.report.builder.column.ColumnBuilder;
import net.sf.dynamicreports.report.exception.DRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

@Service
@Slf4j
public class CustomReportService {

    @Autowired
    private CustomReportRepository customReportRepository;

    private Map<String, String> nameDate(String tableId) {
        int parsedInt = Integer.parseInt(tableId);

        Map<String, String> nameDateMap = new HashMap<>();

        switch (parsedInt) {
            case 1:
                nameDateMap.put("tableName", "archiving");
                nameDateMap.put("dateName", "date_uploaded");
                break;
            case 2:
                nameDateMap.put("tableName", "backup");
                nameDateMap.put("dateName", "back_up_date");
                break;
            case 3:
                nameDateMap.put("tableName", "Documents");
                nameDateMap.put("dateName", "create_date");
                break;
            case 4:
                nameDateMap.put("tableName", "document_log");
                nameDateMap.put("dateName", "timestamp");
                break;
            case 5:
                nameDateMap.put("tableName", "users");
                nameDateMap.put("dateName", "created_on");
                break;
            case 6:
                nameDateMap.put("tableName", "user_log");
                nameDateMap.put("dateName", "timestamp");
                break;
            case 7:
                nameDateMap.put("tableName", "versions");
                nameDateMap.put("dateName", "date_uploaded");
                break;
            case 8:
                nameDateMap.put("tableName", "workflows");
                nameDateMap.put("dateName", "start_time");
                break;
            default:
                throw new IllegalArgumentException("Invalid tableId: " + tableId);
        }

        return nameDateMap;
    }


    public EntityResponse getColumnNames(String tableId) {
        EntityResponse entityResponse = new EntityResponse<>();
        String tableName = nameDate(tableId).get("tableName");
        List<String> columnNames = customReportRepository.getColumnNames(tableName);

        entityResponse.setEntity(columnNames);
        entityResponse.setMessage("Data retrieved successfully");
        entityResponse.setStatusCode(HttpStatus.OK.value());
        log.info(tableName + columnNames);
        return entityResponse;
    }


    public EntityResponse customReportData(String tableId, List<String> columnNames, LocalDate startDate, LocalDate endDate) {
        EntityResponse entityResponse = new EntityResponse();

        if (columnNames.isEmpty()){
            entityResponse.setMessage("No data selected to fetch.");
            entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        }

        String tableName = nameDate(tableId).get("tableName");
        String dateName = nameDate(tableId).get("dateName");
        List<Object[]> reportData;
        try {
            reportData = customReportRepository.customReportData(tableName, dateName, columnNames, startDate, endDate);
        } catch (Exception e) {
            // Handle exception, set error response
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setMessage("Error retrieving report data: " + e.getMessage());
            return entityResponse;
        }

        // Prepare a list to hold maps for each row
        List<Map<String, Object>> mappedData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // Process each row of data
        for (Object[] row : reportData) {
            Map<String, Object> rowMap = new HashMap<>();

            for (int i = 0; i < columnNames.size(); i++) {
                if (i < row.length) {
                    Object value = row[i];
                    if (value instanceof Date) {
                        value = dateFormat.format((Date) value);
                    }
                    rowMap.put(columnNames.get(i), value);
                }
            }
            mappedData.add(rowMap);
        }

        entityResponse.setEntity(mappedData);
        entityResponse.setStatusCode(HttpStatus.OK.value());
        entityResponse.setMessage("Report data retrieved successfully");

        return entityResponse;
    }

    private void buildExcelReport(List<Map<String, Object>> mappedData) {
        try {
            JasperXlsExporterBuilder xlsExporter = export.xlsExporter("E:/Jasper Reports/generatedReports/report.xls")
                    .setDetectCellType(true)
                    .setIgnorePageMargins(true)
                    .setWhitePageBackground(false)
                    .setRemoveEmptySpaceBetweenColumns(true);

            // Extract column names from the first row of mappedData
            if (mappedData.isEmpty()) {
                throw new RuntimeException("No data available to generate report");
            }
            Map<String, Object> firstRow = mappedData.getFirst();
            Set<String> columnNames = firstRow.keySet();

            // Dynamically create columns based on the column names
            List<ColumnBuilder<?, ?>> columns = new ArrayList<>();
            for (String columnName : columnNames) {
                columns.add(col.column(columnName, columnName, type.stringType())); // Adjust type if needed
            }
            report()
                    .addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2")
                    .ignorePageWidth()
                    .ignorePagination()
                    .columns(columns.toArray(new ColumnBuilder<?, ?>[0]))
                    .setDataSource(new CustomJRDataSource(mappedData)) // Use the custom data source
                    .toXls(xlsExporter);
        } catch (DRException e) {
            e.printStackTrace();
        }
    }

//    public void buildReport(List<Map<String, Object>> mappedData, String outputPath, JasperExportFormat exportFormat) {
//        try {
//            // Extract column names from the first row of mappedData
//            if (mappedData.isEmpty()) {
//                throw new RuntimeException("No data available to generate report");
//            }
//            Map<String, Object> firstRow = mappedData.getFirst();
//            Set<String> columnNames = firstRow.keySet();
//
//            // Dynamically create columns based on the column names
//            List<ColumnBuilder<?, ?>> columns = new ArrayList<>();
//            for (String columnName : columnNames) {
//                columns.add(col.column(columnName, columnName, type.stringType())); // Adjust type if needed
//            }
//
//            // Create the report with dynamically defined columns
//            JasperReportBuilder reportBuilder = report()
//                    .addProperty(JasperProperty.EXPORT_XLS_FREEZE_ROW, "2")
//                    .ignorePageWidth()
//                    .ignorePagination()
//                    .columns(columns.toArray(new ColumnBuilder<?, ?>[0]))
//                    .setDataSource(new CustomJRDataSource(mappedData));
//
//            // Export based on the chosen format
//            switch (exportFormat) {
//                case XLS:
//                    reportBuilder.toXls(export.xlsExporter(outputPath)
//                            .setDetectCellType(true)
//                            .setIgnorePageMargins(true)
//                            .setWhitePageBackground(false)
//                            .setRemoveEmptySpaceBetweenColumns(true));
//                    break;
//                case PDF:
//                    reportBuilder.toPdf(export.pdfExporter(outputPath));
//                    break;
//                case JSON:
//                    reportBuilder.toJson(export.jsonExporter(outputPath));
//                    break;
//                case CSV:
//                    reportBuilder.toCsv(export.csvExporter(outputPath));
//                    break;
//                default:
//                    throw new IllegalArgumentException("Unsupported export format: " + exportFormat);
//            }
//        } catch (DRException e) {
//            e.printStackTrace();
//        }
//    }

}