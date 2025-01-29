package com.emt.dms1.backup;

import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BackupService {


    private final BackupRepository backupRepository;
    private final DocumentRepository documentRepository;

    private final Scheduler scheduler;
    @Autowired
    private JdbcTemplate remoteJdbcTemplate;


    @Autowired
    public BackupService(BackupRepository backupRepository, DocumentRepository documentRepository, Scheduler scheduler) {
        this.backupRepository = backupRepository;
        this.documentRepository = documentRepository;
        this.scheduler = scheduler;

    }

    private void saveBackupsToLocation(List<Backup> backups, String location) {
        File backupDirectory = new File(location);
        if (!backupDirectory.exists()) {
            backupDirectory.mkdirs();
        }

        for (Backup backup : backups) {
            File file = new File(backupDirectory, backup.getDocumentName() + ".zip");
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bos = new BufferedOutputStream(fos)) {
                bos.write(backup.getDocumentData());
            } catch (IOException e) {
                log.error("Failed to save backup to location: " + location, e);
            }
        }
    }

//    private Backup mapDocumentToBackup(DocumentModel documentModel, String filePath) {
//        Backup backup = new Backup();
//
//        backup.setDocumentName(documentModel.getDocumentName());
//        backup.setNotes(documentModel.getNotes());
//        backup.setDepartment(documentModel.getDepartment());
//        backup.setDueDate(documentModel.getDueDate());
//        backup.setCreatedBy(documentModel.getCreatedBy());
//        backup.setFileType(documentModel.getFileType());
//        backup.setFileSize(documentModel.getFileSize());
//        backup.setFileLocation(documentModel.getFileLocation());
//        backup.setDateUploaded(documentModel.getCreateDate());
//        backup.setStatus(documentModel.getStatus());
//        backup.setDocumentModel(documentModel);
//        backup.setBackupLocation(filePath);
//        backup.setBackUpDate(LocalDate.now());
//
//        return backup;
//    }




//    public EntityResponse performBackup(String backupLocation) {
//        EntityResponse entityResponse = new EntityResponse<>();
//        log.info("Starting backup process...");
//
//        try {
//            List<Backup> existingBackups = backupRepository.findAll();
//            List<DocumentModel> documentModels = documentRepository.findAll();
//            List<DocumentModel> newDocuments = documentModels.stream()
//                    .filter(document -> !document.isBackedUp()) // Exclude archived documents
//                    .filter(document -> !existingBackups.stream()
//                            .anyMatch(existingBackup -> existingBackup.getDocumentModel().getId().equals(document.getId())))
//                    .collect(Collectors.toList());
//
//            if (newDocuments.isEmpty()) {
//                entityResponse.setMessage("No new documents to backup");
//                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
//                return entityResponse;
//            }
//
//            // First, save the backups to the repository
//            List<Backup> backupList = saveBackups(newDocuments, backupLocation);
//
//            entityResponse.setStatusCode(HttpStatus.OK.value());
//            entityResponse.setMessage("New documents backed up and uploaded successfully");
//            entityResponse.setEntity(backupList);
//
//        } catch (Exception e) {
//            entityResponse.setMessage("Backup failed");
//            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
//            log.error("Error occurred during backup process", e);
//        }
//        return entityResponse;
//    }

    public EntityResponse scheduleBackup(int minutes, String backupLocation) {
        EntityResponse entityResponse = new EntityResponse<>();
        log.info("Setting up backup schedule...");

        try {
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("backupLocation", backupLocation);

            JobDetail jobDetail = JobBuilder.newJob(BackupJob.class)
                    .withIdentity("backupJob", "backupGroup")
                    .usingJobData(jobDataMap)
                    .build();

            // Create a cron expression to trigger every 'minutes' interval
            String cronExpression = String.format("0 0/%d * * * ?", minutes);

            CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                    .withIdentity("backupTrigger", "backupGroup")
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                    .build();

            scheduler.start();
            scheduler.scheduleJob(jobDetail, cronTrigger);

            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Backup scheduled to run every " + minutes + " minutes.");
        } catch (SchedulerException e) {
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setMessage("Failed to schedule backup.");
            log.error("Error scheduling backup", e);
        }

        return entityResponse;
    }

//    private List<Backup> saveBackups(List<DocumentModel> documents, String backupLocation) throws IOException {
//        log.info("Saving backups for {} documents", documents.size());
//        List<Backup> backups = new ArrayList<>();
//
//        for (DocumentModel documentModel : documents) {
//
//            String filePath = backupLocation + "\\" + documentModel.getDocumentName();
//            Backup backup = mapDocumentToBackup(documentModel, filePath);
//
//            Path path = Paths.get(filePath);
//            Files.createDirectories(path.getParent());
//            Files.write(path, documentModel.getDocumentData());
//
//            documentModel.setBackedUp(true);
//            backup.setBackupLocation(filePath);
//            backups.add(backup);
//        }
//
//        List<Backup> savedBackups = backupRepository.saveAll(backups);
//        log.info("Saved {} backups to repository", savedBackups.size());
//        return savedBackups;
//    }

    private BackupDTO mapBackupToBackupDTO(Backup backup, byte[] fileContent ) {
        BackupDTO backupDTO = new BackupDTO();
        backupDTO.setBackupId(backup.getBackup_id());
        backupDTO.setDocumentName(backup.getDocumentName());
        backupDTO.setBackUpDate(backup.getBackUpDate());
        backupDTO.setDepartment(backup.getDepartment());
        backupDTO.setCreatedBy(backup.getCreatedBy());
        backupDTO.setFileType(backup.getFileType());
        backupDTO.setBackupData(fileContent);

        return backupDTO;
    }

    public EntityResponse getAllBackedUpDocuments() {
        EntityResponse<List<BackupDTO>> entityResponse = new EntityResponse<>();
        List<BackupDTO> backupDTOList = new ArrayList<>();

        try {
            List<Backup> backups = backupRepository.findAll();

            for (Backup backup : backups) {
                String backupLocation = backup.getBackupLocation();
                Path filePath = Paths.get(backupLocation);

                // Check if the file exists and is not a directory
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    entityResponse.setMessage("No file found at path: " + filePath);
                    entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                    continue; // Skip to the next backup
                }

                // Attempt to read the file content
                try {
                    byte[] fileContent = Files.readAllBytes(filePath);
                    BackupDTO backupDTO = mapBackupToBackupDTO(backup, fileContent);
                    backupDTOList.add(backupDTO);
                } catch (IOException e) {
                    // Handle IOExceptions while reading the file
                    entityResponse.setMessage("Error reading file at path: " + filePath + " - " + e.getMessage());
                    entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    continue; // Skip to the next backup
                }
            }

            // If no files were successfully retrieved, set an appropriate message
            if (backupDTOList.isEmpty()) {
                entityResponse.setMessage("No valid backups found.");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                // Set successful response
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setMessage("Files retrieved successfully.");
                entityResponse.setEntity(backupDTOList);
            }
        } catch (Exception e) {
            // Handle any unexpected exceptions
            entityResponse.setMessage("An error occurred while retrieving backups: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }


    public EntityResponse<List<DocumentModel>> restoreExistingDocuments() {
        EntityResponse<List<DocumentModel>> entityResponse = new EntityResponse<>();
        List<DocumentModel> updatedDocuments = new ArrayList<>();

        try {
            List<Backup> backups = backupRepository.findAll();

            for (Backup backup : backups) {
                String backupLocation = backup.getBackupLocation();
                Path filePath = Paths.get(backupLocation);

                // Check if the file exists and is not a directory
                if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                    continue; // Skip invalid backup locations
                }

                // Attempt to restore the document
                List<DocumentModel> existingDocumentOptional = documentRepository.findByDocumentName(backup.getDocumentName());

                DocumentModel document = new DocumentModel();
                document.setDocumentName(backup.getDocumentName());
                document.setDocumentData(Files.readAllBytes(filePath));

                document.setNotes(backup.getNotes());
                document.setDepartment(backup.getDepartment());
                document.setDueDate(backup.getDueDate());
                document.setCreatedBy(backup.getCreatedBy());
                document.setFileLocation(backup.getFileLocation());
                document.setFileSize(backup.getFileSize());

                document.setStatus(backup.getStatus());

                document.setDocumentDeleteFlag(backup.getDocumentDeleteFlag());
               documentRepository.save(document);
                updatedDocuments.add(document);
            }

            // Set the response based on the restoration process
            if (updatedDocuments.isEmpty()) {
                entityResponse.setStatusCode(HttpStatus.NOT_MODIFIED.value());
                entityResponse.setMessage("No documents were restored as all are up-to-date.");
            } else {
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setMessage("Documents restored/updated successfully.");
                entityResponse.setEntity(updatedDocuments);
            }
        } catch (IOException e) {
            // Handle IOExceptions
            entityResponse.setMessage("An error occurred while reading backup files: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            entityResponse.setMessage("An error occurred during restoration: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }

    public EntityResponse performBackup(
            String backupLocation,
            String ipAddress,
            String  port,
            String dataBaseName,
            String databaseUsername,
            String databasePassword,
            String isRemote) throws IOException {
        EntityResponse entityResponse = new EntityResponse<>();
        List<Backup> backups;
        List<DocumentModel> newDocuments;
        List<DocumentModel> documentModels = documentRepository.findAll();

        if (isRemote.equals("true")){
            String databaseUrl;
            if (ipAddress != null && port != null && dataBaseName != null) {
                databaseUrl = "jdbc:mariadb://" + ipAddress + ":" + port + "/" + dataBaseName;
            } else {
                entityResponse.setMessage("Invalid database connection details.");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }

            log.info("Starting backup process...");

            try {
                // Create a DataSource with the provided connection details
                DataSource dataSource = createDataSource(databaseUrl, databaseUsername, databasePassword);
                // Test the connection to ensure it's valid
                try (Connection connection = dataSource.getConnection()) {
                    if (connection == null || connection.isClosed()) {
                        throw new SQLException("Unable to establish a valid connection to the database.");
                    }

                    // Check if the 'backup' table exists
                    if (!doesTableExist(connection, "backup")) {
                        log.warn("Table 'backup' does not exist. Creating table...");
                        createBackupTable(connection);
                    }

                } catch (SQLException e) {
                    log.error("Failed to establish a database connection or table creation error: {}", e.getMessage());
                    entityResponse.setMessage("Failed to connect to the database or create necessary tables.");
                    entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    return entityResponse;
                }
                // Fetch existing backups and documents from the provided database
                List<Backup> existingBackups = fetchExistingBackups(dataSource);

                newDocuments = documentModels.stream()
                        .filter(document -> !document.isBackedUpRemotely()) // Exclude archived documents
                        .filter(document -> !existingBackups.stream()
                                .anyMatch(existingBackup -> existingBackup.getDocumentModel().getId().equals(document.getId())))
                        .collect(Collectors.toList());

                if (newDocuments.isEmpty()) {
                    entityResponse.setMessage("No new documents to backup to remote");
                    entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                    return entityResponse;
                }

                backups =  saveBackups(newDocuments, backupLocation, dataSource);

                log.info("Saved {} backups to remote databases", backups.size());
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setMessage("New "+backups.size()+" documents backed up remotely");
                entityResponse.setEntity(backups);

            } catch (Exception e) {
                entityResponse.setMessage("Backup failed"+e.getMessage());
                entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                log.error("Error occurred during backup process", e);
            }
        }
        else {

            List<Backup> existingBackups = backupRepository.findAll();
            newDocuments = documentModels.stream()
                    .filter(document -> !document.isBackedUpLocally()) // Exclude archived documents
                    .filter(document -> !existingBackups.stream()
                            .anyMatch(existingBackup -> existingBackup.getDocumentModel().getId().equals(document.getId())))
                    .collect(Collectors.toList());
            if (newDocuments.isEmpty()) {
                entityResponse.setMessage("No new documents to backup to local");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            backups =  saveBackupsLocally(newDocuments, backupLocation);

            log.info("Saved {} backups to local databases", backups.size());
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("New "+backups.size()+" documents backed up locally");
            entityResponse.setEntity(backups);
        }
        return entityResponse;
    }

    // Method to check if a table exists
    private boolean doesTableExist(Connection connection, String tableName) throws SQLException {
        try (ResultSet resultSet = connection.getMetaData().getTables(null, null, tableName, null)) {
            return resultSet.next();
        }
    }

    // Method to create the 'backup' table if it doesn't exist
    private void createBackupTable(Connection connection) throws SQLException {
        String createTableSQL = "CREATE TABLE backup ("
                + "backup_id BIGINT AUTO_INCREMENT PRIMARY KEY, "
                + "document_name VARCHAR(255), "
                + "document_id BIGINT, "
                + "file_type VARCHAR(255), "
                + "document_data LONGBLOB, "
                + "notes TEXT, "
                + "department VARCHAR(255), "
                + "created_by VARCHAR(255), "
                + "back_up_date DATE DEFAULT CURRENT_DATE, "
                + "date_uploaded DATE, "
                + "due_date DATE, "
                + "document_delete_flag CHAR(1), "
                + "file_size VARCHAR(255), "
                + "file_location VARCHAR(255), "
                + "status VARCHAR(255), "
                + "backup_location VARCHAR(255), "
                + "file_path VARCHAR(255)"
                + ")";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            log.info("Table 'backup' created successfully.");
        }
    }


    // Helper method to create a DataSource based on provided connection details
    private DataSource createDataSource(String url, String username, String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.mariadb.jdbc.Driver");  // Use MariaDB driver
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }

    // Helper method to fetch existing backups from the provided DataSource
    private List<Backup> fetchExistingBackups(DataSource dataSource) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String sql = "SELECT * FROM backup";
        return jdbcTemplate.query(sql, new BackupRowMapper());
    }

    // Backup saving logic remains the same
    public List<Backup> saveBackups(List<DocumentModel> documents, String backupLocation, DataSource dataSource) throws IOException {
        log.info("Saving backups for {} documents", documents.size());
        List<Backup> backups = new ArrayList<>();

        for (DocumentModel documentModel : documents) {
            String filePath = backupLocation + "/Dmsbackups/" + documentModel.getDocumentName();
            Backup backup = mapDocumentToBackup(documentModel, filePath);

            backup.setBackupLocation(filePath);
            backups.add(backup);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String documentDeleteFlagStr = backup.getDocumentDeleteFlag() != null ? String.valueOf(backup.getDocumentDeleteFlag()) : null;
            // Save backup to remote database
            String remoteInsertSql = "INSERT INTO backup (document_name, document_id, file_type, document_data, notes, department, created_by, date_uploaded, due_date, document_delete_flag, file_size, file_location, status, backup_location, file_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(remoteInsertSql,
                    backup.getDocumentName(),
                    backup.getDocumentModel().getId(),
                    backup.getFileType(),
                    backup.getDocumentData(),
                    backup.getNotes(),
                    backup.getDepartment(),
                    backup.getCreatedBy(),
                    backup.getDateUploaded(),
                    backup.getDueDate(),
                    documentDeleteFlagStr,
                    backup.getFileSize(),
                    backup.getFileLocation(),
                    backup.getStatus() != null ? backup.getStatus().name() : null,
                    backup.getBackupLocation(),
                    backup.getFilePath()
            );
            documentModel.setBackedUpRemotely(true);
            documentRepository.save(documentModel);

        }
        return backups;
    }

    public List<Backup> saveBackupsLocally(List<DocumentModel> documents, String backupLocation) throws IOException {
        log.info("Saving backups locally for {} documents", documents.size());
        List<Backup> backups = new ArrayList<>();

        for (DocumentModel documentModel : documents) {
            String filePath = backupLocation + "/Dmsbackups/" + File.separator + documentModel.getDocumentName();
            Backup backup = mapDocumentToBackup(documentModel, filePath);

            // Save document data to a local file
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent()); // Create parent directories if they don't exist
            Files.write(path, documentModel.getDocumentData()); // Write the document data to the file

            documentModel.setBackedUpLocally(true); // Mark document as backed up
            backup.setBackupLocation(filePath);
            backups.add(backup);
            backupRepository.save(backup);
        }

        log.info("Saved {} backups locally.", backups.size());
        return backups;
    }

    public List<Backup> saveBackupsToRemoteDatabase(List<Backup> backups, DataSource dataSource) {
        log.info("Saving backups to remote database for {} backups", backups.size());
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        String remoteInsertSql = "INSERT INTO backup (document_name,  file_type, document_data, notes, department, created_by, date_uploaded, due_date, document_delete_flag, file_size, file_location, status, backup_location, file_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        for (Backup backup : backups) {
            try {
                jdbcTemplate.update(remoteInsertSql,
                        backup.getDocumentName(),
                        backup.getFileType(),
                        backup.getDocumentData(),
                        backup.getNotes(),
                        backup.getDepartment(),
                        backup.getCreatedBy(),
                        backup.getDateUploaded(),
                        backup.getDueDate(),
                        backup.getDocumentDeleteFlag(),
                        backup.getFileSize(),
                        backup.getFileLocation(),
                        backup.getStatus().name(),
                        backup.getBackupLocation(),
                        backup.getFilePath()
                );
            } catch (DataAccessException e) {
                log.error("Failed to save backup for document: {} to remote database", backup.getDocumentName(), e);
            }
        }

        log.info("Saved {} backups to remote database.", backups.size());
        return backups;
    }


    // Your existing mapping function remains the same
    private Backup mapDocumentToBackup(DocumentModel documentModel, String filePath) {
        Backup backup = new Backup();
        backup.setDocumentName(documentModel.getDocumentName());
        backup.setDocumentModel(documentModel);
        backup.setNotes(documentModel.getNotes());
        backup.setDepartment(documentModel.getDepartment());
        backup.setDueDate(documentModel.getDueDate());
        backup.setCreatedBy(documentModel.getCreatedBy());
        backup.setFileType(documentModel.getFileType());
        backup.setFileSize(documentModel.getFileSize());
        backup.setFileLocation(documentModel.getFileLocation());
        backup.setDateUploaded(documentModel.getCreateDate());
        backup.setDocumentData(documentModel.getDocumentData());
        backup.setStatus(documentModel.getStatus());
        backup.setDocumentModel(documentModel);
        backup.setBackupLocation(filePath);
        backup.setBackUpDate(LocalDate.now());
        backup.setDocumentDeleteFlag(documentModel.getDocumentDeleteFlag());
        return backup;
    }



}