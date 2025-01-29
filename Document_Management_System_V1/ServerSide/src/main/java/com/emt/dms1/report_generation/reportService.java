package com.emt.dms1.report_generation;


//import com.emt.dms1.Document.DocumentModel;
//import com.emt.dms1.Document.DocumentSearchService;
//import com.emt.dms1.Document.SearchMetrics;
//import com.emt.dms1.DocumentAudit.DocumentLog;
//import com.emt.dms1.DocumentAudit.DocumentLogService;
//import com.emt.dms1.User.Role;
//import com.emt.dms1.User.User;
//import com.emt.dms1.UserAudit.UserLog;
//import com.emt.dms1.UserAudit.UserLogService;
//import com.emt.dms1.Versioning.Version;
//import com.emt.dms1.WorkFlow.ApprovalWorkflow;
//import com.emt.dms1.WorkFlow.WorkflowService;
//import io.jsonwebtoken.io.IOException;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
//import org.apache.poi.xssf.usermodel.XSSFRow;
//import org.apache.poi.xssf.usermodel.XSSFSheet;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.io.ByteArrayOutputStream;
//import java.lang.reflect.Field;
//import java.lang.reflect.Modifier;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.time.format.DateTimeFormatter;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//@Service
//public class reportService {
//    @Autowired
//    private com.emt.dms1.report_generation.analyticsService analyticsService;
//    @Autowired
//    private DocumentSearchService documentSearchService;
//    @Autowired
//            private DocumentLogService documentLogService;
//    @Autowired
//            private UserLogService userLogService;
//    @Autowired
//            private WorkflowService workflowService;
//
//    XSSFWorkbook workbook = new XSSFWorkbook();
//    XSSFCreationHelper xssfCreationHelper = workbook.getCreationHelper();
//
//    //Version report
//    public byte[] generateVersionReport(LocalDate startDate,LocalDate endDate) throws IOException {
//        List<Version> versions = analyticsService.getAllVersions();
//
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Version Report");
//
//            // Create header row
//            createVersionHeaderRow(sheet);
//
//            // Populate the sheet with version information
//            int rowNum = 1; // Start from 1 since the header row is at 0
//            for (Version version : versions) {
//                createDataRow(sheet, rowNum++, version);
//            }
//
//            // Generate the report data in memory (as a byte array)
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//
//            return outputStream.toByteArray();
//        } catch (java.io.IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private void createVersionHeaderRow(XSSFSheet sheet) {
//        XSSFRow headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("Version Number");
//        headerRow.createCell(1).setCellValue("Created By");
//        headerRow.createCell(2).setCellValue("Date Uploaded");
//        headerRow.createCell(3).setCellValue("Approver Comments");
//        headerRow.createCell(4).setCellValue("Department");
//        headerRow.createCell(5).setCellValue("Document Name");
//        headerRow.createCell(6).setCellValue("File Type");
//    }
//
//    private void createDataRow(XSSFSheet sheet, int rowNum, Version version) {
//        XSSFRow dataRow = sheet.createRow(rowNum);
//        dataRow.createCell(0).setCellValue(String.valueOf(version.getVersionNumber()));
//        dataRow.createCell(1).setCellValue(String.valueOf(version.getCreatedBy()));
//        dataRow.createCell(2).setCellValue(version.getDateUploaded().format(DateTimeFormatter.ofPattern("yyyy-MM-dd ")));
//        dataRow.createCell(3).setCellValue(String.valueOf(version.getApproverComments()));
//        dataRow.createCell(4).setCellValue(String.valueOf(version.getDepartment()));
//        dataRow.createCell(5).setCellValue(String.valueOf(version.getDocumentName()));
//        dataRow.createCell(6).setCellValue(String.valueOf(version.getFileType()));
//    }
//
//
//    // Updated method to generate the user report
//    public byte[]generateUserReport(LocalDate startDate, LocalDate endDate) throws IOException {
//        List<User>  users = analyticsService.getAllUsers();
//
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("User Report");
//
//            // Create header row
//            createHeaderRow(sheet);
//
//            // Populate the sheet with user information
//            int rowNum = 1;
//            Map<String, Integer> employeesPerDepartment = new HashMap<>();
//            Map<Role, Integer> employeesPerRole = new HashMap<>();
//            Map<YearMonth, Integer> employeesAddedPerMonth = new HashMap<>();
//
//            for (User user : users) {
//                createUserDataRow(sheet, rowNum++, user);
//
//                // Update employees per department
//                String department = user.getDepartment();
//                employeesPerDepartment.put(department, employeesPerDepartment.getOrDefault(department, 0) + 1);
//
//                // Update employees per role
//                Role role = user.getRole();
//                employeesPerRole.put(role, employeesPerRole.getOrDefault(role, 0) + 1);
//
//                // Update employees added per month
//                YearMonth createdMonth = YearMonth.from(user.getCreatedOn());
//                employeesAddedPerMonth.put(createdMonth, employeesAddedPerMonth.getOrDefault(createdMonth, 0) + 1);
//            }
//
//            // Generate additional data
//            createAdditionalDataRows(sheet, rowNum, employeesPerDepartment, employeesPerRole, employeesAddedPerMonth);
//
//            // Generate the report data in memory (as a byte array)
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//
//            return outputStream.toByteArray();
//        } catch (java.io.IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    // Method to create additional data rows (e.g., total employees per department, per role, etc.)
//    private void createAdditionalDataRows(XSSFSheet sheet, int rowNum,
//                                          Map<String, Integer> employeesPerDepartment,
//                                          Map<Role, Integer> employeesPerRole,
//                                          Map<YearMonth, Integer> employeesAddedPerMonth) {
//        rowNum++; // Add an empty row for spacing
//
//        // Total number of employees
//        Row totalEmployeesRow = sheet.createRow(rowNum++);
//        totalEmployeesRow.createCell(0).setCellValue("Total Employees:");
//        totalEmployeesRow.createCell(1).setCellValue(employeesPerDepartment.values().stream().mapToInt(Integer::intValue).sum());
//
//        // Employees per department
//        for (Map.Entry<String, Integer> entry : employeesPerDepartment.entrySet()) {
//            Row departmentRow = sheet.createRow(rowNum++);
//            departmentRow.createCell(0).setCellValue("Employees in " + entry.getKey() + ":");
//            departmentRow.createCell(1).setCellValue(entry.getValue());
//        }
//
//        // Employees per role
//        for (Map.Entry<Role, Integer> entry : employeesPerRole.entrySet()) {
//            Row roleRow = sheet.createRow(rowNum++);
//            roleRow.createCell(0).setCellValue("Employees with role " + entry.getKey() + ":");
//            roleRow.createCell(1).setCellValue(entry.getValue());
//        }
//
//        // Total employees added per month
//        for (Map.Entry<YearMonth, Integer> entry : employeesAddedPerMonth.entrySet()) {
//            Row monthRow = sheet.createRow(rowNum++);
//            monthRow.createCell(0).setCellValue("Employees added in " + entry.getKey() + ":");
//            monthRow.createCell(1).setCellValue(entry.getValue());
//        }
//    }
//
//    private void createHeaderRow(XSSFSheet sheet) {
//        XSSFRow headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("First Name");
//        headerRow.createCell(1).setCellValue("Last Name");
//        headerRow.createCell(2).setCellValue("Employee Number");
//        headerRow.createCell(3).setCellValue("Email Address");
//        headerRow.createCell(4).setCellValue("Phone Number");
//        headerRow.createCell(5).setCellValue("Department");
//        headerRow.createCell(6).setCellValue("Created On");
//        headerRow.createCell(7).setCellValue("Role");
//    }
//
//    private void createUserDataRow(XSSFSheet sheet, int rowNum, User user) {
//        XSSFRow dataRow = sheet.createRow(rowNum);
//        dataRow.createCell(0).setCellValue(user.getFirstName());
//        dataRow.createCell(1).setCellValue(user.getLastName());
//        dataRow.createCell(2).setCellValue(user.getEmployeeNumber());
//        dataRow.createCell(3).setCellValue(user.getEmailAddress());
//        dataRow.createCell(4).setCellValue(user.getPhoneNumber());
//        dataRow.createCell(5).setCellValue(user.getDepartment());
//        dataRow.createCell(6).setCellValue(user.getCreatedOn().toString());
//        dataRow.createCell(7).setCellValue(user.getRole().toString());
//    }
//
//
//    public byte[] generateDocumentReport( LocalDate startDate,LocalDate endDate) throws IOException {
//        List<DocumentModel> allDocuments = analyticsService.getAllDocuments();
//
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Document Report");
//            createDocumentHeaderRow(sheet);
//            int rowNum = 1; // Start from 1 since the header row is at 0
//
//            int totalDocuments = allDocuments.size();
//            int successfulDocuments = 0;
//            int failedDocuments = 0;
//            Map<String, Integer> documentsPerDepartment = new HashMap<>();
//            Map<String, Integer> documentsPerCreator = new HashMap<>();
//
//            // Calculate counts for each category
//            for (DocumentModel document : allDocuments) {
//                if (document.isSuccessful()) {
//                    successfulDocuments++;
//                } else {
//                    failedDocuments++;
//                }
//                documentsPerDepartment.put(document.getDepartment(), documentsPerDepartment.getOrDefault(document.getDepartment(), 0) + 1);
//                documentsPerCreator.put(document.getCreatedBy(), documentsPerCreator.getOrDefault(document.getCreatedBy(), 0) + 1);
//            }
//
//            // Calculate success rate
//            double successRate = (double) successfulDocuments / totalDocuments * 100;
//
//            // Write data to the Excel sheet
//            createDataRow(sheet, rowNum++, "Total Documents", String.valueOf(totalDocuments));
//            createDataRow(sheet, rowNum++, "Successful Documents", String.valueOf(successfulDocuments));
//            createDataRow(sheet, rowNum++, "Failed Documents", String.valueOf(failedDocuments));
//            createDataRow(sheet, rowNum++, "Success Rate (%)", String.format("%.2f", successRate));
//
//            // Write documents per department
//            for (Map.Entry<String, Integer> entry : documentsPerDepartment.entrySet()) {
//                createDataRow(sheet, rowNum++, "Documents in " + entry.getKey(), String.valueOf(entry.getValue()));
//            }
//
//            // Write documents per creator
//            for (Map.Entry<String, Integer> entry : documentsPerCreator.entrySet()) {
//                createDataRow(sheet, rowNum++, "Documents created by " + entry.getKey(), String.valueOf(entry.getValue()));
//            }
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//
//            return outputStream.toByteArray();
//        } catch (IOException | java.io.IOException e) {
//            throw new RuntimeException("Failed to generate document report", e);
//        }
//    }
//
//
//    private void createDocumentHeaderRow(XSSFSheet sheet) {
//        XSSFRow headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("Category");
//        headerRow.createCell(1).setCellValue("Count");
//    }
//
//    private void createDataRow(XSSFSheet sheet, int rowNum, String category, String count) {
//        XSSFRow dataRow = sheet.createRow(rowNum);
//        dataRow.createCell(0).setCellValue(category);
//        dataRow.createCell(1).setCellValue(count);
//    }
//
//    // Monthly document storage report
//    public byte[] generateMonthlyDocumentReportByFileType(LocalDate startDate, LocalDate endDate) throws IOException, java.io.IOException {
//        List<DocumentModel> documents = analyticsService.getDocumentsByDateRange(startDate, endDate);
//
//        // Aggregate data and calculate storage usage over time by file type and department
//        Map<YearMonth, Map<String, Map<String, Long>>> monthlyReportData = new HashMap<>();
//        for (DocumentModel document : documents) {
//            LocalDate createDate = document.getCreateDate();
//            YearMonth yearMonth = YearMonth.of(createDate.getYear(), createDate.getMonth());
//            String documentSize = document.getFileSize();
//            String fileType = document.getFileType();
//            String department = document.getDepartment(); // Assuming there's a method to get the department
//
//            if (documentSize != null && !documentSize.isEmpty() && department != null) {
//                // Group by year-month, file type, and department
//                monthlyReportData
//                        .computeIfAbsent(yearMonth, k -> new HashMap<>())
//                        .computeIfAbsent(fileType, k -> new HashMap<>())
//                        .merge(department, Long.parseLong(documentSize), Long::sum);
//            }
//        }
//
//        // Generate Excel workbook
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Monthly Document Report by File Type and Department");
//
//            int rowNum = 0;
//            for (Map.Entry<YearMonth, Map<String, Map<String, Long>>> entry : monthlyReportData.entrySet()) {
//                // Month-Year as header row
//                Row headerRow = sheet.createRow(rowNum++);
//                headerRow.createCell(0).setCellValue("Month of Document Uploaded: " + entry.getKey().toString()); // Month-Year
//
//                // Header row for file types, departments, and total storage usage
//                Row fileTypeHeaderRow = sheet.createRow(rowNum++);
//                fileTypeHeaderRow.createCell(1).setCellValue("File Type");
//                fileTypeHeaderRow.createCell(2).setCellValue("Department");
//                fileTypeHeaderRow.createCell(3).setCellValue("Storage Usage");
//
//                // File type and department distribution
//                for (Map.Entry<String, Map<String, Long>> fileTypeEntry : entry.getValue().entrySet()) {
//                    for (Map.Entry<String, Long> departmentEntry : fileTypeEntry.getValue().entrySet()) {
//                        Row row = sheet.createRow(rowNum++);
//                        row.createCell(1).setCellValue(fileTypeEntry.getKey()); // File type
//                        row.createCell(2).setCellValue(departmentEntry.getKey()); // Department
//                        double storageInMB = departmentEntry.getValue() / 1024.0; // Convert KB to MB
//                        row.createCell(3).setCellValue(storageInMB); // Storage usage
//                    }
//                }
//
//                // Total storage usage for the month
//                Row totalStorageRow = sheet.createRow(rowNum++);
//                totalStorageRow.createCell(1).setCellValue("Total Monthly Document Usage:");
//                double totalStorage = entry.getValue().values()
//                        .stream()
//                        .flatMap(departmentMap -> departmentMap.values().stream())
//                        .mapToLong(Long::longValue)
//                        .sum() / 1024.0;
//                totalStorageRow.createCell(3).setCellValue(totalStorage);
//
//                // Add an empty row for spacing
//                rowNum++;
//            }
//
//            // Write workbook to byte array
//            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//                workbook.write(outputStream);
//                return outputStream.toByteArray();
//            }
//        }
//    }
//
//    // workflows report
//    public byte[] generateWorkflowReport(LocalDate startDate, LocalDate endDate) throws IOException {
//        List<ApprovalWorkflow> workflows = analyticsService.getAllWorkflows(startDate, endDate);
//        List<DocumentModel> documents = analyticsService.getAllDocuments(); // Retrieve all documents
//
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Workflow Report");
//
//            // Create header row
//            Row headerRow = sheet.createRow(0);
//            headerRow.createCell(0).setCellValue("Document Status");
//            headerRow.createCell(1).setCellValue("Department");
//            headerRow.createCell(2).setCellValue("Document ID");
//            headerRow.createCell(3).setCellValue("Total Duration");
//            headerRow.createCell(4).setCellValue("Start Time");
//            headerRow.createCell(5).setCellValue("Step Durations");
//
//            // Populate data rows
//            int rowNum = 1;
//            for (ApprovalWorkflow workflow : workflows) {
//                DocumentModel document = findDocumentForWorkflow(workflow, documents);
//                if (document != null) {
//                    Row row = sheet.createRow(rowNum++);
//                    row.createCell(0).setCellValue(String.valueOf(document.getStatus() != null ? document.getStatus() : ""));
//                    row.createCell(1).setCellValue(document.getDepartment() != null ? document.getDepartment() : "");
//                    row.createCell(2).setCellValue(workflow.getId());
//                    row.createCell(3).setCellValue(workflow.getTotalDuration() != null ? workflow.getTotalDuration() : "");
//                    row.createCell(4).setCellValue(workflow.getStartTime() != null ? workflow.getStartTime().toString() : ""); // Adjust date format as needed
//                    row.createCell(5).setCellValue(workflow.getStepDurations() != null ? String.join(", ", workflow.getStepDurations()) : "");
//                }
//            }
//
//            // Generate the report data in memory (as a byte array)
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        } catch (java.io.IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private DocumentModel findDocumentForWorkflow(ApprovalWorkflow workflow, List<DocumentModel> documents) {
//        return documents.stream()
//                .filter(doc -> Objects.equals(doc.getId(), workflow.getId()))
//                .findFirst()
//                .orElse(null);
//    }
//
//
//    // User logs report
//public byte[] generateUserLogsReport(LocalDate startDate, LocalDate endDate) throws java.io.IOException {
//    List<UserLog> logs = analyticsService.getAllUserLogs(startDate, endDate);
//
//    try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//        // Main report sheet
//        XSSFSheet mainSheet = workbook.createSheet("User_logs -Report");
//        createLogsHeaderRow(mainSheet);
//
//        int rowNum = 1;
//        for (UserLog log : logs) {
//            Row row = mainSheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(log.getTimestamp().toString());
//            row.createCell(1).setCellValue(log.getUserName());
//            row.createCell(2).setCellValue(log.getUserLogType().toString());
//        }
//
//        // Subreport sheet
//        XSSFSheet subSheet = workbook.createSheet("User_Log_Type_Summary");
//        createSubReportHeaderRow(subSheet);
//
//        Map<String, Map<String, Integer>> userLogTypeSummary = getUserLogTypeSummary(logs);
//
//        int subRowNum = 1;
//        for (Map.Entry<String, Map<String, Integer>> entry : userLogTypeSummary.entrySet()) {
//            Row subRow = subSheet.createRow(subRowNum++);
//            subRow.createCell(0).setCellValue(entry.getKey()); // User Name
//
//            int colNum = 1;
//            for (Map.Entry<String, Integer> logTypeEntry : entry.getValue().entrySet()) {
//                subRow.createCell(colNum++).setCellValue(logTypeEntry.getValue()); // Log Type count
//            }
//        }
//
//        // Generate the report data in memory (as a byte array)
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        workbook.write(outputStream);
//        return outputStream.toByteArray();
//    }
//}
//
//    private void createLogsHeaderRow(XSSFSheet sheet) {
//        Row headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("Timestamp");
//        headerRow.createCell(1).setCellValue("User Name");
//        headerRow.createCell(2).setCellValue("User Log Type");
//    }
//
//    private void createSubReportHeaderRow(XSSFSheet sheet) {
//        Row headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("User Name");
//        headerRow.createCell(1).setCellValue("LOGIN_SUCCESS");
//        headerRow.createCell(2).setCellValue("LOGIN_FAILURE");
//        // Add more headers for other log types if needed
//    }
//
//    private Map<String, Map<String, Integer>> getUserLogTypeSummary(List<UserLog> logs) {
//        Map<String, Map<String, Integer>> userLogTypeSummary = new HashMap<>();
//
//        for (UserLog log : logs) {
//            String userName = log.getUserName();
//            String logType = log.getUserLogType().toString();
//
//            userLogTypeSummary.putIfAbsent(userName, new HashMap<>());
//            Map<String, Integer> logTypeCount = userLogTypeSummary.get(userName);
//            logTypeCount.put(logType, logTypeCount.getOrDefault(logType, 0) + 1);
//        }
//
//        return userLogTypeSummary;
//    }
//
//
//
//    //Document search report
//    public byte[] generateDocumentSearchReport(LocalDate startDate, LocalDate endDate) throws IOException, java.io.IOException {
//        // Fetch all metrics from the database
//        List<SearchMetrics> searchResults = analyticsService.getAllMetrics(startDate,endDate);
//
//        // Generate report using search metrics
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Search-Metrics -Report");
//
//            // Create header row
//            createSearchHeaderRow(sheet);
//
//            // Add search metrics to the report
//            addMetricsToReport(sheet, searchResults);
//
//            // Generate the report data in memory (as a byte array)
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//
//    private void createSearchHeaderRow(XSSFSheet sheet) {
//        XSSFRow headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("Metric");
//        headerRow.createCell(1).setCellValue("Value");
//
//        int rowNum = 1;
//        for (Field field : SearchMetrics.class.getDeclaredFields()) {
//            if (!Modifier.isStatic(field.getModifiers())) {
//                // Get the name of the metric (field)
//                String metricName = field.getName();
//                // Create a new header row for the metric
//                XSSFRow metricHeaderRow = sheet.createRow(rowNum++);
//                metricHeaderRow.createCell(0).setCellValue(metricName);
//                // Set "N/A" as the value for the metric header row
//                metricHeaderRow.createCell(1).setCellValue("N/A");
//            }
//        }
//    }
//
//    private void addMetricsToReport(XSSFSheet sheet, List<SearchMetrics> searchResult) {
//        int rowNum = 0;
//
//        // Create header row with metric names
//        createSearchHeaderRow(sheet);
//
//        // Add metrics to the report
//        for (SearchMetrics result : searchResult) {
//            Row row = sheet.createRow(++rowNum);
//
//            // Add Timestamp
//            row.createCell(0).setCellValue("Timestamp");
//            row.createCell(1).setCellValue(result.getTimestamp().toString());
//
//            // Add Total Response Time (ms)
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Total Response Time (ms)");
//            row.createCell(1).setCellValue(result.getTotalResponseTime());
//
//            // Add Total Searches
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Total Searches");
//            row.createCell(1).setCellValue(result.getTotalSearches());
//
//            // Add Average Response Time (ms)
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Average Response Time (ms)");
//            row.createCell(1).setCellValue(result.getAverageResponseTime());
//
//            // Add Search Throughput (queries/second)
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Search Throughput (queries/second)");
//            row.createCell(1).setCellValue(result.getSearchThroughput());
//
//            // Add Query Parameters
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Query Parameters");
////            row.createCell(1).setCellValue(String.valueOf(result.getQueryParameters() != null ? result.getQueryParameters() : "N/A"));
//
//            // Add Zero Rate Results
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Zero Rate Results");
//            row.createCell(1).setCellValue(result.getZeroRateResults());
//
//            // Add Zero Rate Results Percentage
//            row = sheet.createRow(++rowNum);
//            row.createCell(0).setCellValue("Zero Rate Results Percentage");
//            row.createCell(1).setCellValue(result.getZeroRateResultsPercentage());
//
//            // Increment rowNum for spacing between metrics
//            rowNum++;
//        }
//    }
//
//    //Audit trail report
//
//
//    public byte[] generateDocumentTrailReport(List<DocumentLog> auditTrails, LocalDate startDate, LocalDate endDate) throws IOException, java.io.IOException {
//        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
//            XSSFSheet sheet = workbook.createSheet("Document Audit Trail Report");
//            createTrailHeaderRow(sheet);
//            populateDataRows(sheet, auditTrails);
//            return writeWorkbookToByteArray(workbook);
//        }
//    }
//
//    private void createTrailHeaderRow(XSSFSheet sheet) {
//        XSSFRow headerRow = sheet.createRow(0);
//        headerRow.createCell(0).setCellValue("Document NAME/ID");
//        headerRow.createCell(1).setCellValue("Timestamp");
//        headerRow.createCell(2).setCellValue("Username");
//        headerRow.createCell(3).setCellValue("Document Log Type");
//    }
//
//    private void populateDataRows(XSSFSheet sheet, List<DocumentLog> auditTrails) {
//        Map<Long, Integer> documentRowIndices = new HashMap<>();
//
//        for (DocumentLog auditTrail : auditTrails) {
//            DocumentModel document = auditTrail.getDocument();
//            long documentId = document.getId();
//
//            // Get the row index for the current document ID, or create a new row if it doesn't exist
//            int rowIndex = documentRowIndices.getOrDefault(documentId, sheet.getLastRowNum() + 1);
//            XSSFRow row = sheet.getRow(rowIndex);
//            if (row == null) {
//                row = sheet.createRow(rowIndex);
//                row.createCell(0).setCellValue(document.getId() + "/" + document.getDocumentName());
//                documentRowIndices.put(documentId, rowIndex);
//            }
//
//            // Populate data in the current row
//            populateRowWithData(row, auditTrail);
//
//            // Update the row index for the next iteration
//            documentRowIndices.put(documentId, rowIndex + 1);
//        }
//    }
//
//
//
//    private void populateRowWithData(XSSFRow row, DocumentLog auditTrail) {
//        int lastCellIndex = row.getLastCellNum();
//
//        // If there are no existing cells in the row, start populating from cell index 0
//        if (lastCellIndex == -1) {
//            lastCellIndex = 0;
//        }
//
//        // Populate the data in their corresponding cells
//        row.createCell(lastCellIndex++).setCellValue(formatLocalDateTime(auditTrail.getTimestamp()));
//        row.createCell(lastCellIndex++).setCellValue(auditTrail.getUserName());
//        row.createCell(lastCellIndex).setCellValue(auditTrail.getDocumentLogType().toString());
//    }
//
//    private String formatLocalDateTime(LocalDateTime dateTime) {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        return dateTime.format(formatter);
//    }
//
//    private byte[] writeWorkbookToByteArray(XSSFWorkbook workbook) throws IOException, java.io.IOException {
//        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
//            workbook.write(outputStream);
//            return outputStream.toByteArray();
//        }
//    }
//





//    public void generateDocumentReportByCreateDate(LocalDate startDate, LocalDate endDate) {
//        // Implement logic to generate document report by creation date
//    }
//
//    public void generateDocumentReportByFileType(List<DocumentModel> fileType) {
//        // Implement logic to generate document report by file type
//    }
//
//    public void generateDocumentReportByDepartment(String department) {
//        // Implementation to generate document report by department
//        List<DocumentModel> documents = analyticsService.getDocumentsByDepartment(department);
//        // Your logic here
//    }
//
//    public void generateDocumentReportByApproverComments(List<DocumentModel> approverComments) {
//        List<DocumentModel> documents = analyticsService.getDocumentsByApproverCommentsContainingIgnoreCase(approverComments);
//    }
//
//    public void generateDocumentReportByDueDate(LocalDate startDate, LocalDate endDate) {
//        List<DocumentModel> documents = analyticsService.getDocumentsByDueDateBetween(startDate, endDate);
//    }

//}