package com.emt.dms1.archiving;

import com.emt.dms1.backup.Backup;
import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.*;

import static net.sf.jasperreports.engine.query.ExcelQueryExecuter.createDataSource;

@Service
@Slf4j
public class ArchivingService {

    private final DocumentRepository documentRepository;
    private final ArchivingRepository archivingRepository;
    private final Scheduler scheduler;


    @Autowired
    public ArchivingService(DocumentRepository documentRepository, ArchivingRepository archivingRepository, Scheduler scheduler) {
        this.documentRepository = documentRepository;
        this.archivingRepository = archivingRepository;
        this.scheduler = scheduler;
    }



    public EntityResponse scheduleArchiving(int Days) {
        EntityResponse entityResponse = new EntityResponse<>();
        log.info("Setting up archiving schedule...");

        try {
            // Generate cron expression based on the interval in days
            String cronExpression = generateCronExpression(Days);

            // Prepare job data map with necessary parameters
            JobDataMap jobDataMap = new JobDataMap();
            // You can add any necessary parameters to the jobDataMap here if needed

            // Define job and trigger
            JobDetail jobDetail = JobBuilder.newJob(ArchivingJob.class)
                    .withIdentity("archivingJob", "archivingGroup")
                    .usingJobData(jobDataMap)  // Pass the JobDataMap to the JobDetail
                    .build();

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("archivingTrigger", "archivingGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // Schedule the job
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);

            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Archiving scheduled to run every " + Days + " days.");
        } catch (SchedulerException e) {
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setMessage("Failed to schedule archiving.");
            log.error("Error scheduling archiving", e);
        }

        return entityResponse;
    }

    private String generateCronExpression(int intervalInDays) {
        // Cron expression to run at midnight every intervalInDays days
        return String.format("0 0 0 */%d * ?", intervalInDays);
    }

    public EntityResponse scheduleDocumentRetention(int days) {
        EntityResponse entityResponse = new EntityResponse<>();
        log.info("Setting up document retention schedule...");

        try {
            // Generate cron expression to run the retention job every specified number of days
            String cronExpression = generateCronExpression(days);

            // Define the job to perform document retention
            JobDetail jobDetail = JobBuilder.newJob(RetentionJob.class)
                    .withIdentity("retentionJob", "retentionGroup")
                    .build();

            // Define the trigger with the cron expression
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity("retentionTrigger", "retentionGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            // Schedule the job
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();
            scheduler.scheduleJob(jobDetail, trigger);

            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Document retention scheduled every " + days + " days.");
        } catch (SchedulerException e) {
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setMessage("Failed to schedule document retention.");
            log.error("Error scheduling document retention", e);
        }

        return entityResponse;
    }



    public EntityResponse performArchiving(
            String archiveLocation,
            String ipAddress,
            String port,
            String dataBaseName,
            String databaseUsername,
            String databasePassword,
            String isRemote) throws IOException {
        EntityResponse entityResponse = new EntityResponse<>();
        List<Archiving> archiving;
        List<DocumentModel> newDocuments;
        List<DocumentModel> documentModels = documentRepository.findAll();

        if (isRemote.equals("true")) {
            String databaseUrl;
            if (ipAddress != null && port != null && dataBaseName != null) {
                databaseUrl = "jdbc:mariadb://" + ipAddress + ":" + port + "/" + dataBaseName;
            } else {
                entityResponse.setMessage("Invalid database connection details.");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }

            log.info("Starting archiving process...");

            try {
                // Create a DataSource with the provided connection details
                DataSource dataSource = createDataSource(databaseUrl, databaseUsername, databasePassword);
                // Test the connection to ensure it's valid
                try (Connection connection = dataSource.getConnection()) {
                    if (connection == null || connection.isClosed()) {
                        throw new SQLException("Unable to establish a valid connection to the database.");
                    }

                    // Check if the 'archive' table exists
                    if (!doesArchivingTableExist(connection, "Archiving")) {
                        log.warn("Table 'archive' does not exist. Creating table...");
                        createArchivingTable(connection);
                    }

                } catch (SQLException e) {
                    log.error("Failed to establish a database connection or table creation error: {}", e.getMessage());
                    entityResponse.setMessage("Failed to connect to the database or create necessary tables.");
                    entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    return entityResponse;
                }

                // Fetch existing archives and documents from the provided database
                List<Archiving> existingArchives = fetchExistingArchives(dataSource);

                newDocuments = documentModels.stream()
                        .filter(document -> !document.isArchivedRemotely()) // Exclude already archived documents
                        .filter(document -> !existingArchives.stream()
                                .anyMatch(existingArchive -> existingArchive.getDocumentModel().getId().equals(document.getId())))
                        .collect(Collectors.toList());

                if (newDocuments.isEmpty()) {
                    entityResponse.setMessage("No new documents to archive");
                    entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                    return entityResponse;
                }

                archiving = saveArchivesToRemoteDatabase(newDocuments, archiveLocation,dataSource);

                log.info("Saved {} archives to both databases", archiving.size());
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setMessage("New documents archived and uploaded successfully");
                entityResponse.setEntity(archiving);

            } catch (Exception e) {
                entityResponse.setMessage("Archiving failed: " + e.getMessage());
                entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                log.error("Error occurred during archiving process", e);
            }
        } else {
            List<Archiving> existingArchives = archivingRepository.findAll();
            newDocuments = documentModels.stream()
                    .filter(document -> !document.isArchivedLocally()) // Exclude already archived documents
                    .filter(document -> !existingArchives.stream()
                            .anyMatch(existingArchive -> existingArchive.getDocumentModel().getId().equals(document.getId())))
                    .collect(Collectors.toList());
            if (newDocuments.isEmpty()) {
                entityResponse.setMessage("No new documents to archive");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            archiving = saveArchivesLocally(newDocuments, archiveLocation);

            log.info("Saved {} archives to both databases", archiving.size());
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("New documents archived and uploaded successfully");
            entityResponse.setEntity(archiving);
        }
        return entityResponse;
    }
    private boolean doesArchivingTableExist(Connection connection, String archivingTableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, archivingTableName, new String[] { "TABLE" })) {
            return resultSet.next();
        }
    }
    public void markDocumentsForDeletion() {
        try {

            List<Archiving> documentsToDelete = archivingRepository.findAll();

            // Mark documents as deleted
            for (Archiving document : documentsToDelete) {
                document.setDeleted(true); // Assuming you have a 'deleted' field
                archivingRepository.save(document);
            }

            log.info("Marked {} documents as deleted.", documentsToDelete.size());
        } catch (Exception e) {
            log.error("Error marking documents for deletion: {}", e.getMessage());
            throw new RuntimeException("Error marking documents for deletion", e);
        }
    }
    private void createArchivingTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE Archiving ("
                + "Archiving_id BIGINT AUTO_INCREMENT PRIMARY KEY, " // This column is set as the primary key
                + "document_name VARCHAR(255), "
                + "document_id BIGINT, "
                + "file_type VARCHAR(255), "
                + "document_data LONGBLOB, "
                + "notes TEXT, "
                + "department VARCHAR(255), "
                + "created_by VARCHAR(255), "
                + "Archiving_date DATE DEFAULT CURRENT_DATE, "
                + "date_uploaded DATE, "
                + "due_date DATE, "
                + "document_delete_flag CHAR(1), "
                + "file_size VARCHAR(255), "
                + "file_location VARCHAR(255), "
                + "status VARCHAR(255), "
                + "Archive_location VARCHAR(255), "
                + "file_path VARCHAR(255)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            log.info("Table 'archiving' created successfully.");
        }
    }


    private DataSource createDataSource(String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");  // Use MariaDB driver
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
    private List<Archiving> fetchExistingArchives(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "SELECT * FROM Archiving";
        return jdbcTemplate.query(sql, new ArchivingRowMapper());
    }
    public List<Archiving> saveArchivesLocally(List<DocumentModel> documents, String archiveLocation) throws IOException {
        log.info("Saving archives locally for {} documents", documents.size());
        List<Archiving> archives = new ArrayList<>();

        for (DocumentModel documentModel : documents) {
            String filePath = archiveLocation +"/Dmsarchives/"+ File.separator + documentModel.getDocumentName();
            Archiving archive = mapDocumentToArchive(documentModel, filePath);

            // Save document data to a local file
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Create parent directories if they don't exist
            Files.write(path, documentModel.getDocumentData()); // Write the document data to the file

            documentModel.setArchivedLocally(true); // Mark document as archived
            archive.setArchiveLocation(filePath);
            archive.setArchiveDate(LocalDate.now());
            archives.add(archive);
            archivingRepository.save(archive);
        }
        archivingRepository.saveAll(archives);
        log.info("Saved {} archives locally.", archives.size());
        return archives;
    }
    public List<Archiving> saveArchivesToRemoteDatabase(List<DocumentModel> documents, String archiveLocation, DataSource dataSource) {
        log.info("Saving backups for {} documents", documents.size());
        List<Archiving> archives = new ArrayList<>();

        for (DocumentModel documentModel : documents) {
            String filePath = archiveLocation + "/Dmsarchivess/" + documentModel.getDocumentName();
            Archiving archive = mapDocumentToArchive(documentModel, filePath);

            archive.setArchiveLocation(filePath);
            archives.add(archive);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String documentDeleteFlagStr = archive.getDocumentDeleteFlag() != null ? String.valueOf(archive.getDocumentDeleteFlag()) : null;
            // Save backup to remote database
            String remoteInsertSql = "INSERT INTO Archiving ( document_name,document_id,file_type,document_data,notes,department,created_by,Archiving_date,date_uploaded,due_date,document_delete_flag," +
                    "file_size,file_location, status,Archive_location,file_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
            jdbcTemplate.update(remoteInsertSql,
                    archive.getDocumentName(),
                    archive.getDocumentModel().getId(),
                    archive.getFileType(),
                    archive.getDocumentData(),
                    archive.getNotes(),
                    archive.getDepartment(),
                    archive.getCreatedBy(),
                    archive.getArchiveDate(),
                    archive.getDateUploaded(),
                    archive.getDueDate(),
                    documentDeleteFlagStr,
                    archive.getFileSize(),
                    archive.getFileLocation(),
                    archive.getStatus() != null ? archive.getStatus().name() : null,
                    archive.getArchiveLocation(),
                    archive.getFilePath()
            );
            documentModel.setArchivedRemotely(true);
        }

        documentRepository.saveAll(documents);
        return archives;
    }

    private Archiving mapDocumentToArchive(DocumentModel documentModel, String filePath) {
        Archiving archive = new Archiving();
        archive.setDocumentName(documentModel.getDocumentName());
        archive.setDocumentModel(documentModel);
        archive.setFileType(documentModel.getFileType());
        archive.setDocumentData(documentModel.getDocumentData());
        archive.setNotes(documentModel.getNotes());
        archive.setDepartment(documentModel.getDepartment());
        archive.setCreatedBy(documentModel.getCreatedBy());
        archive.setArchiveDate(LocalDate.now());
        archive.setDateUploaded(documentModel.getCreateDate());
        archive.setDueDate(documentModel.getDueDate());
        archive.setDocumentDeleteFlag(documentModel.getDocumentDeleteFlag());
        archive.setFileSize(documentModel.getFileSize());
        archive.setFileLocation(documentModel.getFileLocation());
        archive.setStatus(documentModel.getStatus());
        archive.setArchiveLocation(filePath);
        archive.setFilePath(documentModel.getFilepath());

        return archive;
    }



    private byte[] zipDocument(byte[] documentData) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            ZipEntry entry = new ZipEntry("document");
            zos.putNextEntry(entry);
            zos.write(documentData);
            zos.closeEntry();
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to zip document data", e);
            return null;
        }
    }

    private byte[] unzipDocument(byte[] zippedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zippedData);
             ZipInputStream zis = new ZipInputStream(bais)) {
            zis.getNextEntry();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            log.error("Failed to unzip document data", e);
            return null;
        }
    }

    public EntityResponse<List<ArchivingDTO>> getAllArchivedDocuments() {
        EntityResponse<List<ArchivingDTO>> entityResponse = new EntityResponse<>();
        List<ArchivingDTO> archiveDTOList = new ArrayList<>();

        try {
            List<Archiving> archives = archivingRepository.findAll();

            for (Archiving archive : archives) {
                String archiveLocation = archive.getArchiveLocation();
                Path filePath = Paths.get(archiveLocation);

                // Check if the file exists and is not a directory
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    entityResponse.setMessage("No file found at path: " + filePath);
                    entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                    continue; // Skip to the next archive
                }

                // Attempt to read the file content
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    ArchivingDTO archivingDTO = mapArchiveToArchiveDTO(archive);
                    archiveDTOList.add(archivingDTO);
                } catch (IOException e) {
                    // Handle IOExceptions while reading the file
                    entityResponse.setMessage("Error reading file at path: " + filePath + " - " + e.getMessage());
                    entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    continue; // Skip to the next archive
                }
            }

            // If no files were successfully retrieved, set an appropriate message
            if (archiveDTOList.isEmpty()) {
                entityResponse.setMessage("No valid archives found.");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                // Set successful response
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setMessage("Archived files retrieved successfully.");
                entityResponse.setEntity(archiveDTOList);
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions
            entityResponse.setMessage("An error occurred while retrieving archives: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }

    private ArchivingDTO mapArchiveToArchiveDTO(Archiving archive) {
        ArchivingDTO archiveDTO = new ArchivingDTO();
        archiveDTO.setDocumentName(archive.getDocumentName());
        archiveDTO.setDepartment(archive.getDepartment());
        archiveDTO.setCreatedBy(archive.getCreatedBy());
        archiveDTO.setFileType(archive.getFileType());
        archiveDTO.setDocumentData(archive.getDocumentData());
        return archiveDTO;
    }
}
