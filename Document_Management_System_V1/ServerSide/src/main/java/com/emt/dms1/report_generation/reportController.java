package com.emt.dms1.report_generation;

import com.emt.dms1.documentAudit.DocumentLogRepository;
import com.emt.dms1.utils.EntityResponse;
import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.builder.DynamicReports;
import net.sf.dynamicreports.report.builder.column.Columns;
import net.sf.dynamicreports.report.builder.component.Components;
import net.sf.dynamicreports.report.builder.datatype.DataTypes;
import net.sf.dynamicreports.report.constant.HorizontalAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@RestController
@Slf4j
@CrossOrigin
@RequestMapping(value="/api/v1/reports",produces = MediaType.APPLICATION_JSON_VALUE)
public class reportController {
    //    @Autowired
 //  private com.emt.dms1.report_generation.reportService reportService;
    @Autowired
    private analyticsService analyticsService;
    @Autowired
    private DocumentLogRepository documentLogRepository;

    @Autowired
    private  reportRepo reportRepo;


    private final String report_icon = "";
    @Value("${reports.files.path}")
    private String report_path;


    @Value("${spring.datasource.url}")
    private String db;

    @Value("${spring.datasource.username}")
    private String dbusername;

    @Value("${spring.datasource.password}")
    private String dbpassword;

    private Connection establishConnection() throws SQLException {
        return DriverManager.getConnection(this.db, this.dbusername, this.dbpassword);
    }
   @GetMapping("/document-pdf")
    public EntityResponse generateDocumentsReportPdf(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return generateReport("document.jrxml", "Documents-report.pdf", startDate, endDate,
                (sDate, eDate) -> reportRepo.getDocumentsByDateRange(sDate, eDate));
    }



    @GetMapping("/deleted-documents-pdf")
    public EntityResponse generateDeletedDocumentReportPdf(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return generateReport("Deleted_Documents.jrxml", "deleted-documents-report.pdf", startDate, endDate,
                (sDate, eDate) -> reportRepo.getDocumentsByDateRange(sDate, eDate));
    }


    private EntityResponse generateReport(
            String reportFileName,
            String outputFileName,
            LocalDate startDate,
            LocalDate endDate,
            BiFunction<LocalDate, LocalDate, List<Object[]>> dataFetcher
    ) {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            List<Object[]> record = dataFetcher.apply(startDate, endDate);

            if (!record.isEmpty()) {
                log.info("Records found");

                Connection connection = establishConnection();
                JasperReport compileReport = JasperCompileManager.compileReport(new FileInputStream(report_path + reportFileName));
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("startDate", startDate);
                parameters.put("endDate", endDate);

                JasperPrint print = JasperFillManager.fillReport(compileReport, parameters, connection);
                byte[] data = JasperExportManager.exportReportToPdf(print);
                MediaType filetype = MediaType.APPLICATION_PDF;
                ReportDto report = new ReportDto();
                report.setFiletype(filetype);
                report.setFilename(outputFileName);
                report.setFileData(data);

                entityResponse.setMessage("Report generated successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(report);
            } else {
                entityResponse.setMessage("No records found for the specified date range");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(record);
            }
        } catch (FileNotFoundException e) {
        entityResponse.setMessage("Report file not found");
        entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        } catch (JRException e) {
        entityResponse.setMessage("Error generating" + reportFileName + "report: " + e.getMessage());
        entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
        entityResponse.setMessage("Unexpected error: " + e.getMessage());
        entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }
    @GetMapping("/user-pdf")
        public EntityResponse generateUsersReport(
                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
        ) {
            return generateReport("User.jrxml", "Users-report.pdf", startDate, endDate,
                    (sDate, eDate) -> reportRepo.getUsersByDateRange(sDate, eDate));
        }


    @GetMapping("/deleted_users-pdf")
    public EntityResponse generateDeletedUsersReport(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return generateReport("DeletedUsers.jrxml", "Deleted-users.pdf", startDate, endDate,
                (sDate, eDate) -> reportRepo.getDeletedUsersByDateRange(sDate, eDate));
    }

    // storage report pdf
    @GetMapping("/storage-pdf")
        public EntityResponse generateStorageReport(
                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
        ) {
            return generateReport("Storage.jrxml", "Documents_Storage-report.pdf", startDate, endDate,
                    (sDate, eDate) -> reportRepo.getStorageByDateRange(sDate, eDate));
        }
    //Workflows
   @GetMapping("/workflows-pdf")
        public EntityResponse generateWorkflowReport(
                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
        ) {
            return generateReport("workflow.jrxml", "Workflows-report.pdf", startDate, endDate,
                    (sDate, eDate) -> reportRepo.getWorkflowsByDateRange(sDate, eDate));
        }
//Backup report
@GetMapping("/back-up-pdf")
public EntityResponse generateBackUpReport(
        @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
) {
    return generateReport("backup.jrxml", "Documents_BackUp-report.pdf", startDate, endDate,
            (sDate, eDate) -> reportRepo.getBackUpByDateRange(sDate, eDate));
}
// Archiving report
    @GetMapping("/archiving-pdf")
    public EntityResponse generateArchivingReport(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {
        return generateReport("archiving.jrxml", "Documents_Archiving-report.pdf", startDate, endDate,
                (sDate, eDate) -> reportRepo.getArchivingByDateRange(sDate, eDate));
    }
    // versions report pdf report
     @GetMapping("/versions-pdf")
        public EntityResponse generateVersionsReport(
                @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
        ) {
            return generateReport("version.jrxml", "Documents_Versions-report.pdf", startDate, endDate,
                    (sDate, eDate) -> reportRepo.getVersionsByDateRange(sDate, eDate));
        }
    // document audit trail
       @GetMapping("/document_logs-pdf")
            public EntityResponse generateDocumentLogReport(
                    @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                    @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
            ) {
                return generateReport("logs.jrxml", "Documents_Logs-report.pdf", startDate, endDate,
                        (sDate, eDate) -> reportRepo.getDocumentLogsByDateRange(sDate, eDate));
            }
    //security audit Trail report
    @GetMapping("/user_logs-pdf")
                public EntityResponse  generateUserLogs1Report(
                        @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                        @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
                ) {
                    return generateReport("userLogs.jrxml", "User_Logs-report.pdf", startDate, endDate,
                            (sDate, eDate) -> reportRepo.getUserLogsByDateRange(sDate, eDate));
                }

    //search metrics pdf report
    @GetMapping("/search-pdf")
                    public EntityResponse  generateSearchMetricsReport(
                            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
                    ) {
                        return generateReport("search.jrxml", "Search-Metrics-report.pdf", startDate, endDate,
                                (sDate, eDate) -> reportRepo.getSearchMetricsByDateRange(sDate, eDate));
                    }

    //workflow
    @GetMapping("/workflow")
    public EntityResponse generateWorkflowsReport(){
        return analyticsService.getAllWorkflows();
      }
    @GetMapping("/document_logs")
    public EntityResponse generateDocumentLogsreport(){
        return analyticsService.getAllDocumentLogType();
      }
    @GetMapping("/search")
    public EntityResponse generateSearchMetrcicsReport(){
        return analyticsService.getAllMetrics();
}
    @GetMapping("user_logs")
    public EntityResponse generateUserLogsReport(){
        return analyticsService.getAllUserLogs();
}
    @GetMapping("/archiving")
    public EntityResponse generateArchivingReport(){
        return  analyticsService.getAllArchivedDocuments();
}
    @GetMapping("/back-up")
    public EntityResponse generateBackupReport(){
        return analyticsService.getAllBackedupDocuments();
}
    @GetMapping("/storage")
    public EntityResponse generateStorageReport(){
        return  analyticsService.getAllDocumentsStorageSize();
}
    @GetMapping("/deleted_documents")
    public EntityResponse generateDeletedDocumentsReport(){
        return  analyticsService.getAllDeletedDocuments();
    }
    @GetMapping("/deleted_users")
    public EntityResponse generateDeletedUsersReport(){
        return  analyticsService.getAllDeletedUsers();
    }

}


