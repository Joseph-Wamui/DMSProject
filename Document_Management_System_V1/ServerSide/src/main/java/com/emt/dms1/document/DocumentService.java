package com.emt.dms1.document;


import com.emt.dms1.user.UserRepository;
import com.emt.dms1.documentAudit.DocumentLogService;
import com.emt.dms1.documentAudit.DocumentLogType;
import com.emt.dms1.notifications.NotificationModel;
import com.emt.dms1.notifications.NotificationService;
import com.emt.dms1.notifications.NotificationStatus;
import com.emt.dms1.notifications.notificationType;
import com.emt.dms1.user.User;
import com.emt.dms1.workFlow.Status;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5); // adjust thread pool size as needed
    private final DocumentLogService documentLogService;
    private final EncryptionUtils encryptionUtils;
    private static final String FILE_STORAGE_PATH = "D:\\DmsDocs\\";

    public DocumentService(DocumentRepository documentRepository, UserRepository userRepository, NotificationService notificationService, DocumentLogService documentLogService, EncryptionUtils encryptionUtils) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.documentLogService = documentLogService;
        this.encryptionUtils = encryptionUtils;
    }

//    @CacheEvict(value = {"myDocuments", "allDocuments", "documentNames", "documentStatuses"}, allEntries = true)
    public EntityResponse<List<DocumentModel>> uploadMultipleDocuments(
            List<MultipartFile> files) {
        EntityResponse<List<DocumentModel>> entityResponse = new EntityResponse<>();
        List<DocumentModel> savedDocuments = new ArrayList<>();

        try {
            // Validate if files are present
            if (files == null || files.isEmpty()) {
                log.warn("No files uploaded. Please select files to upload.");
                entityResponse.setMessage("No files uploaded. Please select files to upload.");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }

            for (MultipartFile file : files) {
                DocumentModel documentModel = new DocumentModel();

                String documentName = Objects.requireNonNull(file.getOriginalFilename());

                String newName = renameFile(documentName);

                documentModel.setDocumentName(newName);
                documentModel.setFileType(file.getContentType());
                documentModel.setFileSize(String.valueOf(file.getSize()));
                documentModel.setDocumentDeleteFlag('N');

                byte[] fileData = file.getBytes();
                documentModel.setDocumentData(fileData);
                LocalDate currentDate = LocalDate.now();

                // Get the email address of the currently logged-in user
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String loggedInUsername = authentication.getName();

                // Fetch the user from the database using the email address or unique identifier
                Optional<User> optionalUser = userRepository.findByEmailAddress(loggedInUsername); // Adjust this according to your UserRepository

                // If the user is found, set the department attribute of the document
                if (optionalUser.isPresent()) {
                    User loggedInUser = optionalUser.get();
                    documentModel.setDepartment(loggedInUser.getDepartment());
                } else {
                    log.warn("User not found. Please log in again.");
                    // User not found, set a default department or throw an exception
                    entityResponse.setMessage("User not found. Please log in again.");
                    entityResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                    return entityResponse;
                }
                documentModel.setCreatedBy(loggedInUsername);
                documentModel.setCreateDate(currentDate);

                // Save the document with all attributes
                DocumentModel savedDocument = documentRepository.save(documentModel);

                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Uploaded_multiple_documents, savedDocument);

                savedDocuments.add(savedDocument);
            }

            // Log successful document uploads
            log.info("Uploaded {} documents successfully", savedDocuments.size());

            // Set the response entity and return
            entityResponse.setEntity(savedDocuments);
            entityResponse.setMessage("Documents uploaded successfully.");
            entityResponse.setStatusCode(HttpStatus.OK.value());
        } catch (IOException e) {
            // Error reading files
            log.error("Error reading files: {}", e.getMessage());
            entityResponse.setMessage("Failed to read files. Please try again.");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            // Other unexpected errors
            log.error("Error uploading documents: {}", e.getMessage());
            entityResponse.setMessage("Failed to upload documents due to an unexpected error. Please try again later.");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public String renameFile(String documentName){
        // Split document name into base name and extension
        String baseDocumentName = documentName;
        String extension = "";
        int dotIndex = baseDocumentName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = baseDocumentName.substring(dotIndex); // get extension including the dot
            baseDocumentName = baseDocumentName.substring(0, dotIndex); // get the base name without extension
        }

        // Find documents containing the base document name
        List<DocumentModel> existingDocuments = documentRepository.findByDocumentNameContainingIgnoreCase(baseDocumentName);
        Set<String> allDocumentNames = existingDocuments.stream()
                .map(DocumentModel::getDocumentName)
                .collect(Collectors.toSet());

        // Check against documents containing the base name and generate a unique name
        String newDocumentName = baseDocumentName;
        int counter = 1; // Start counter from 1
        while (allDocumentNames.contains(newDocumentName + extension)) {
            newDocumentName = baseDocumentName + "(" + counter + ")";
            counter++;
        }
        documentName = newDocumentName + extension;
        return documentName;
    }

    public EntityResponse<DocumentModel> getDocumentById(Long id) {
        EntityResponse<DocumentModel> entityResponse = new EntityResponse<>();
        try {
            log.info("Fetching document with ID: " + id);

            Optional<DocumentModel> exists = documentRepository.findById(id);
            if (exists.isPresent()) {
                DocumentModel document = exists.get();
                // Decrypt the document data
                byte[] decryptedDocumentData = encryptionUtils.decrypt(document.getDocumentData());
                document.setDocumentData(decryptedDocumentData);

                // Log the user's access to the document
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String loggedInUsername = authentication.getName();
                log.info("User '" + loggedInUsername + "' accessed document '" + document.getDocumentName() + "'");

                // Log the audit trail for document access
                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Accessed_the_document, document);

                entityResponse.setMessage("Document with ID: " + id + " found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(document); // Set the decrypted document model as the entity
            } else {
                log.warn("Document with ID: " + id + " does not exist");

                entityResponse.setMessage("Document with ID: " + id + " does not exist");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            log.error("Error while fetching document with ID: " + id + ": " + exception.getMessage());
            entityResponse.setMessage("Failed to fetch document with ID: " + id + ": " + exception.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

//    @Cacheable("documentWithSpecificId")
    public EntityResponse<DocumentModel> getDocumentById2(Long id) {
        EntityResponse<DocumentModel> entityResponse = new EntityResponse<>();
        try {
            log.info("Fetching document with ID: " + id);

            Optional<DocumentModel> exists = documentRepository.findById(id);
            log.info("Doc with id Db");
            if (exists.isPresent()) {
                DocumentModel document = exists.get();
                // Decrypt the document data
//                byte[] decryptedDocumentData = encryptionUtils.decrypt(document.getDocumentData());
//                document.setDocumentData(decryptedDocumentData);

                // Log the user's access to the document
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String loggedInUsername = authentication.getName();
                log.info("User '" + loggedInUsername + "' accessed document '" + document.getDocumentName() + "'");

                // Log the audit trail for document access
                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Accessed_the_document, document);

                entityResponse.setMessage("Document with ID: " + id + " found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(document); // Set the decrypted document model as the entity
            } else {
                log.warn("Document with ID: " + id + " does not exist");

                entityResponse.setMessage("Document with ID: " + id + " does not exist");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            log.error("Error while fetching document with ID: " + id + ": " + exception.getMessage());
            entityResponse.setMessage("Failed to fetch document with ID: " + id + ": " + exception.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

//    @Cacheable("allDocuments")
    public EntityResponse fetchDocuments() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING DOCUMENTS<-------");
            List<DocumentModel> documentList = documentRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            if (documentList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("My documents is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                List<DocumentDto> decryptedList = new ArrayList<>();
                for (DocumentModel document : documentList) {
                    try {
//                        byte[] decryptedDocumentData = encryptionUtils.decrypt(document.getDocumentData());
//                        document.setDocumentData(decryptedDocumentData);
                        DocumentDto dto = mapEntityToDTO(document);
                        decryptedList.add(dto);
                    } catch (Exception e) {
                        log.error("Error decrypting document: " + document.getId());
                        // Handle decryption error, skip this document or add an error flag to the DTO
                    }
                }
                entityResponse.setMessage("Documents found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(decryptedList);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING MY DOCUMENTS<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

//    @Cacheable("documentNames")
    public EntityResponse fetchDocumentNames() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING DOCUMENT NAMES<-------");
            List<DocumentModel> documentList = documentRepository.findAll();
            if (documentList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("My documents is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
//
                List<String> documentNames = new ArrayList<>();
                for (DocumentModel document : documentList) {
                    String documentName = document.getDocumentName();
                    documentNames.add(documentName);
                }
                entityResponse.setMessage("Document names found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(documentNames);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENT NAMES<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

//    @CacheEvict(value = {"myDocuments", "allDocuments", "documentNames", "documentStatuses"}, allEntries = true)
    public EntityResponse<DocumentModel> deleteDocument(Long id) {
        EntityResponse<DocumentModel> entityResponse = new EntityResponse<>();

            log.info("Deleting document with ID: {}", id);
            Optional<DocumentModel> documentOptional = documentRepository.findById(id);

            if (documentOptional.isPresent()) {
                DocumentModel document = documentOptional.get();

                String documentName = document.getDocumentName();

                // Get the email address of the currently logged-in user
                String loggedInUsername = SecurityUtils.getCurrentUserLogin();

                document.setDocumentDeleteFlag('Y');
                document.setDeletedBy(loggedInUsername);
                document.setDeletedOn(LocalDateTime.now());

                // Log the action for deleting the document
                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Deleted_the_document, document);
                log.info("Document: '{}' ID: {} deleted successfully", document.getDocumentName(), document.getId());

                entityResponse.setMessage(String.format("%s deleted %s successfully", loggedInUsername, documentName));
                entityResponse.setStatusCode(HttpStatus.OK.value());
            } else {
                log.warn("Document with ID '{}' does not exist", id);
                entityResponse.setMessage("Document not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        return entityResponse;
    }

    public DocumentModel findDocumentById(Long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
    }

    public List<DocumentDto> mapEntitiesToDTOs(List<DocumentModel> document) {
        return document.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    private DocumentDto mapEntityToDTO(DocumentModel Document) {
        DocumentDto dto = new DocumentDto();
        dto.setDocumentId(Document.getId());
        dto.setStatus(Document.getStatus());
        dto.setDepartment(Document.getDepartment());
        dto.setDocumentName(Document.getDocumentName());
        dto.setCreateDate(Document.getCreateDate());
        dto.setCreatedBy(Document.getCreatedBy());
        dto.setNotes(Document.getNotes());
        dto.setTags(Collections.singletonList(Document.getTags()).toString());
        dto.setDueDate(Document.getDueDate());
        dto.setFileType(Document.getFileType());
        dto.setFileSize(Document.getFileSize());
        dto.setBackedUp(Document.isBackedUpLocally());

        return dto;
    }

//    @CacheEvict(value = {"myDocuments", "allDocuments", "documentNames", "documentStatuses"}, allEntries = true)
    public EntityResponse<DocumentModel> uploadDocument(String documentName, MultipartFile file, String notes,
                                                        List<String> tags, LocalDate dueDate, String fileLocation) throws Exception {

        EntityResponse<DocumentModel> entityResponse = new EntityResponse<>();

        try {
            // Validate file existence
            if (file == null || file.isEmpty()) {
                entityResponse.setMessage("No file selected. Please select a file to upload.");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }

            if(documentName == null){
                documentName= "newDocument";
            }

            String newName = renameFile(documentName);

            /*String filePath = FILE_STORAGE_PATH + documentName; // Adjust the file path as per your requirement

            // Create the directory if it doesn't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            // Save the file to the specified location
            Files.write(path, file.getBytes());

            log.info("files have been written");*/

            // Extract file information
            byte[] fileData = file.getBytes();
//            byte[] encryptedDocumentData = encryptionUtils.encrypt(fileData);

            // Create a new Document1Model instance
            DocumentModel documentModel = new DocumentModel();

            //Set the required document attributes
            documentModel.setDocumentName(newName);
            documentModel.setFileType(file.getContentType());
            documentModel.setFileSize(String.valueOf(file.getSize()));
            documentModel.setDocumentData(fileData);
            documentModel.setFileLocation(fileLocation);
            documentModel.setDocumentDeleteFlag('N');
//            documentModel.setFilepath(filePath);
//            documentModel.setBackedUp(false);

            // Set the additional attributes
            documentModel.setTags(tags);
            documentModel.setNotes(notes);

            // Get the email address of the currently logged-in user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();

            // Fetch the user from the database using the email address
            Optional<User> optionalUser = userRepository.findByEmailAddress(loggedInUsername);
            // If the user is found, set the department attribute of the document
            if (optionalUser.isPresent()) {
                User loggedInUser = optionalUser.get();
                documentModel.setDepartment(loggedInUser.getDepartment());
            } else {
                // User not found, set a default department or throw an exception
                entityResponse.setMessage("User not found. Please log in again.");
                entityResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                return entityResponse;
            }

            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Check if the due date is before the current date
            if (dueDate!=null && dueDate.isBefore(currentDate)) {
                // Due date is earlier than the current date, reject it
                entityResponse.setMessage("Due date cannot be earlier than the current date.");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            documentModel.setDueDate(dueDate);

            documentModel.setCreatedBy(loggedInUsername);
            documentModel.setCreateDate(currentDate);

            // Save the document with all attributes
            DocumentModel savedDocument = documentRepository.save(documentModel);

            // Log document creation
            log.info("Document '" + savedDocument.getDocumentName() + "' saved successfully.");

            // Log the audit trail for document creation
            documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Uploaded_document, savedDocument);
            //log the audit trail for adding tags
            log.info("User '" + loggedInUsername + "' added document metadata '" + documentName + "'");
            documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Added_document_metadata, savedDocument);

            // Create a notification for document creation
            String notificationMessage = "Document '" + savedDocument.getDocumentName() + "' has been uploaded successfully.";
            NotificationModel notification = new NotificationModel( notificationMessage, loggedInUsername, NotificationStatus.UNREAD, LocalDateTime.now(),documentModel.getId(), notificationType.NORMAL);
            notificationService.createNotification2(notification);

            // Set the response entity and return
            entityResponse.setEntity(savedDocument);
            entityResponse.setMessage("Document '" + savedDocument.getDocumentName() + "' uploaded successfully.");
            entityResponse.setStatusCode(HttpStatus.OK.value());
        } catch (IOException e) {
            // Error reading file
            log.error("Error reading file: " + e.getMessage());
            entityResponse.setMessage("Failed to read file. Please try again.");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        } catch (Exception e) {
            // Other unexpected errors
            log.error("Error uploading document: " + e.getMessage());
            entityResponse.setMessage("Failed to upload document due to an unexpected error. Please try again later.");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public byte[] getFileByName(String fileName) throws IOException {
    // Construct the file path
    Path filePath = Paths.get(FILE_STORAGE_PATH + fileName);

    // Check if the file exists
    if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
        throw new IOException("File not found: " + fileName);
    }

    // Read the file content
    return Files.readAllBytes(filePath);
    }

//    @Cacheable("myDocuments")
    public EntityResponse getMyDocuments() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING DOCUMENTS<-------");
            String email= SecurityUtils.getCurrentUserLogin();
            List<DocumentModel> documentList = documentRepository.findByCreatedByAndDocumentDeleteFlagOrderByIdDesc(email, 'N');
            if (documentList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("My documents is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                List<DocumentDto> decryptedList = new ArrayList<>();
                for (DocumentModel document : documentList) {
                    try {
//                        byte[] decryptedDocumentData = encryptionUtils.decrypt(document.getDocumentData());
////                        document.setDocumentData(decryptedDocumentData);
                        DocumentDto dto = mapEntityToDTO(document);
                        decryptedList.add(dto);
                    } catch (Exception e) {
                        log.error("Error decrypting document: " + document.getId());
                        // Handle decryption error, skip this document or add an error flag to the DTO
                    }
                }
                entityResponse.setMessage("Documents found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(decryptedList);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING MY DOCUMENTS<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }


    public CompletableFuture<List<DocumentModel>> saveMultipleDocuments2(List<MultipartFile> files,
                                                                         String notes,
                                                                         List<String> tags,
                                                                         List<String>  approverComments,
                                                                         LocalDate dueDate) {
        Optional<User> loggedInUser = userRepository.findByEmailAddress(SecurityContextHolder.getContext().getAuthentication().getName());
        String department = loggedInUser.isPresent() ? loggedInUser.get().getDepartment() : null;
        String createdBy = SecurityUtils.getCurrentUserLogin();

        List<DocumentModel> savedDocuments = new ArrayList<>();
        List<Future<DocumentModel>> futures = files.stream()
                .map(file -> executorService.submit(() -> saveDocument3(file, notes, tags, dueDate, department, createdBy)))
                .collect(Collectors.toList());

        for (Future<DocumentModel> future : futures) {
            try {
                savedDocuments.add(future.get()); // wait for each future to complete and get the DocumentModel
            } catch (InterruptedException | ExecutionException e) {
                // Handle exceptions during document saving
                e.printStackTrace();
            }
        }

        return CompletableFuture.completedFuture(savedDocuments);
    }

    private DocumentModel saveDocument3(MultipartFile file,
                                        String notes,
                                        List<String> tags,
                                        LocalDate dueDate,
                                        String department,
                                        String createdBy) throws IOException {
        DocumentModel documentModel = new DocumentModel();
        documentModel.setDocumentName(file.getOriginalFilename());
        documentModel.setFileType(file.getContentType());
        documentModel.setFileSize(String.valueOf(file.getSize()));

        try (InputStream inputStream = file.getInputStream()) {
            byte[] buffer = new byte[4096]; // adjust buffer size as needed
            int bytesRead;
            StringBuilder documentData = new StringBuilder();
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                documentData.append(new String(buffer, 0, bytesRead));
            }
            documentModel.setDocumentData(documentData.toString().getBytes()); // convert back to byte array
        }

        documentModel.setTags(tags);
        documentModel.setNotes(notes);
        documentModel.setDueDate(dueDate);
        documentModel.setDepartment(department);
        documentModel.setCreatedBy(createdBy);
        documentModel.setCreateDate(LocalDate.now());

        return documentModel;
    }


    public byte[] decryptDocumentData(byte[] documentData) {
        return documentData;
    }

    public List<DocumentModel> fetchDocumentModels() {
        return documentRepository.findAll(); }

    public EntityResponse  getStatusList(){
        EntityResponse<List<Status>> entityResponse = new EntityResponse<>();
        try {
            String email=SecurityUtils.getCurrentUserLogin();
            List<DocumentModel> documentList = documentRepository.findByCreatedBy(email);
            List<Status> statusList = new ArrayList<>();

            for (DocumentModel document : documentList) {
                Status status = document.getStatus(); // Assuming getStatus() method retrieves the status from DocumentModel
                if(status != null){
                    statusList.add(status);
                }
            }

            entityResponse.setEntity(statusList);
            entityResponse.setMessage("Status list retrieved successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
        } catch (Exception e) {
            entityResponse.setMessage("Error occurred while retrieving status list");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            // Handle exception or log error message
        }

        return entityResponse;
    }

    public EntityResponse getMydocs() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING DOCUMENTS<-------");
            String email= SecurityUtils.getCurrentUserLogin();
            List<DocumentModel> documentList = documentRepository.findByCreatedBy(email);
            if (documentList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("My documents is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                List<DocumentDto> decryptedList = new ArrayList<>();
                for (DocumentModel document : documentList) {
                    try {
//                      byte[] decryptedDocumentData = encryptionUtils.decrypt(document.getDocumentData());
//                      document.setDocumentData(decryptedDocumentData);
                        DocumentDto dto = mapEntityToDTO(document);
                        decryptedList.add(dto);
                    } catch (Exception e) {
                        log.error("Error decrypting document: " + document.getId());
                        // Handle decryption error, skip this document or add an error flag to the DTO
                    }
                }
                entityResponse.setMessage("Documents found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(decryptedList);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING MY DOCUMENTS<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse updateAttributes(Long documentId, String notes, List<String> tags,List<String>  approverComments, LocalDate dueDate) {
        EntityResponse entityResponse = new EntityResponse<>();
        Optional<DocumentModel> optionalDocumentModel = documentRepository.findById(documentId);
        if (optionalDocumentModel.isPresent()) {
            DocumentModel document = optionalDocumentModel.get();

            document.setNotes(notes);
            document.setTags(tags);
            LocalDate currentDate = LocalDate.now();
            if (dueDate.isBefore(currentDate)) {
                entityResponse.setMessage("Due date cannot be in the past");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            document.setDueDate(dueDate);
            documentRepository.save(document);
            documentLogService.logDocumentAction(SecurityUtils.getCurrentUserLogin(), DocumentLogType.Uploaded_multiple_documents, document);

            entityResponse.setMessage(String.format("Document with id %d updated successfully", documentId));
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(document);

        } else {
            entityResponse.setMessage(String.format("Document with id %d not found", documentId));
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse getDocumentsNumber() {
        EntityResponse entityResponse = new EntityResponse<>();
        String email = SecurityUtils.getCurrentUserLogin();

        int documentsNo = documentRepository.findByDocumentDeleteFlag('N').size();
        int approvedNo = documentRepository.findByCreatedByAndStatusAndDocumentDeleteFlag(email, Status.APPROVED, 'N').size();
        int pendingNo = documentRepository.findByCreatedByAndStatusAndDocumentDeleteFlag(email, Status.PENDING, 'N').size();
        int rejectedNo = documentRepository.findByCreatedByAndStatusAndDocumentDeleteFlag(email, Status.REJECTED, 'N').size();
        int notStartedNo = documentRepository.findByCreatedByAndStatusAndDocumentDeleteFlag(email, null, 'N').size();

        Map<String, Object> documentsMap= new HashMap<>();

        if (documentsNo == 0){
            entityResponse.setMessage("No documents found");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            documentsMap.put("Approved", 0);
            documentsMap.put("Pending", 0);
            documentsMap.put("Rejected", 0);
            documentsMap.put("Not_started", 0);
            documentsMap.put("Total", 0);
            entityResponse.setEntity(documentsMap);
        }
        else {
            documentsMap.put("Approved", approvedNo);
            documentsMap.put("Pending", pendingNo);
            documentsMap.put("Rejected", rejectedNo);
            documentsMap.put("Not_started", notStartedNo);
            documentsMap.put("Total", documentsNo);
            entityResponse.setMessage(documentsNo + " documents found");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(documentsMap);
        }
        return entityResponse;
    }

}







//    public DocumentModel saveDocumentAndAddAttributes(MultipartFile file, String notes, String approverComments,
//                                                      String department, LocalDate dueDate) throws IOException {
//
//        // Extract file information
//        String documentName = Objects.requireNonNull(file.getOriginalFilename());
//        byte[] fileData = file.getBytes();
//
//        // Create a new Document1Model instance
//        DocumentModel document1Model = new DocumentModel();
//        document1Model.setDocumentName(documentName);
//        document1Model.setFileType(file.getContentType());
//        document1Model.setDocumentData(fileData);
//
//        // Set the additional attributes
//        document1Model.setNotes(notes);
//        document1Model.setApproverComments(approverComments);
//        document1Model.setDepartment(department);
//        document1Model.setDueDate(dueDate);
//        document1Model.setCreatedBy(SecurityUtils.getCurrentUserLogin());
//        document1Model.setCreateDate(LocalDate.now());
//
//        // Save the document with all attributes
//        return documentRepository.save(document1Model);
//    }
//    public DocumentModel processDocumentAndAddAttributes(Long id, String notes, String approverComments, String department, LocalDate dueDate, String createdBy) {
//        // Retrieve the document from the main table
//        DocumentModel documentModel = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
//        if(documentModel != null){
//
//        }
//        // Add additional attributes
//
//        documentModel.setApproverComments(approverComments);
//        documentModel.setDepartment(department);
//        documentModel.setDueDate(dueDate);
//        documentModel.setCreatedby();
//        documentModel.setNotes(notes);
//
//
//        return documentRepository.save(documentModel);
//    }
//    public DocumentModel saveDocument(MultipartFile file) throws IOException {
//        String documentName = Objects.requireNonNull(file.getOriginalFilename());
//        byte[] fileData = file.getBytes();
//        log.info("Document converted to bytes");
//
//        DocumentModel documentModel = new DocumentModel();
//        documentModel.setDocumentName(documentName);
//        documentModel.setFileType(file.getContentType());
//        documentModel.setDocumentData(fileData);
//        LocalDateTime currentDate = LocalDateTime.now();
//        documentModel.setCreateDate(currentDate);
//        documentModel.setCreatedBy(SecurityUtils.getCurrentUserLogin());
//        log.info("user created by has worked");
//        log.info("Document has been added sucessfully");
//
//
//        return documentRepository.save(documentModel);
//    }

//    public DocumentModel processDocumentAndAddAttributes(
//            String documentIdentifier, // Accepts either document name or document ID
//            String newDocumentName,
//            String notes,
//            String approverComments,
//            String department,
//            LocalDate dueDate,
//            String createdBy
//    ) {
//        // Fetch the document based on the provided document identifier (name or ID)
//        DocumentModel documentToUpdate = getDocumentByIdentifier(documentIdentifier);
//
//        // Update the document name if a new document name is provided
//        if (newDocumentName != null && !newDocumentName.isEmpty()) {
//            documentToUpdate.setDocumentName(newDocumentName);
//        }
//
//        // Update other attributes
//        documentToUpdate.setNotes(notes);
//        documentToUpdate.setApproverComments(approverComments);
//        documentToUpdate.setDepartment(department);
//        documentToUpdate.setDueDate(dueDate);
//        documentToUpdate.setCreatedBy(SecurityUtils.getCurrentUserLogin());
//        log.info("user crested by has worked");
//        // Save the updated document
//        return documentRepository.save(documentToUpdate);
//    }
//public DocumentModel saveDocumentAndAddAttributes(MultipartFile file, String notes, String approverComments,
//                                                  String department, LocalDate dueDate) throws IOException {
//
//    // Extract file information
//    String documentName = Objects.requireNonNull(file.getOriginalFilename());
//    byte[] fileData = file.getBytes();
//
//    // Create a new Document1Model instance
//    DocumentModel document1Model = new DocumentModel();
//    document1Model.setDocumentName(documentName);
//    document1Model.setFileType(file.getContentType());
//    document1Model.setDocumentData(fileData);
//
//    // Set the additional attributes
//    document1Model.setNotes(notes);
//    document1Model.setApproverComments(approverComments);
//    document1Model.setDepartment(department);
//    document1Model.setDueDate(dueDate);
//    document1Model.setCreatedBy(SecurityUtils.getCurrentUserLogin());
//    document1Model.setCreateDate(LocalDate.now());
//
//    // Save the document with all attributes
//    return documentRepository.save(document1Model);

//    public DocumentModel processDocumentAndAddAttributes(
//            String documentIdentifier, // Accepts either document name or document ID
//            String newDocumentName,
//            String notes,
//            String approverComments,
//            String department,
//            LocalDate dueDate,
//            String createdBy
//    ) {
//        // Fetch the document based on the provided document identifier (name or ID)
//        DocumentModel documentToUpdate = getDocumentByIdentifier(documentIdentifier);
//
//        // Update the document name if a new document name is provided
//        if (newDocumentName != null && !newDocumentName.isEmpty()) {
//            documentToUpdate.setDocumentName(newDocumentName);
//        }
//
//        // Update other attributes
//        documentToUpdate.setNotes(notes);
//        documentToUpdate.setApproverComments(approverComments);
//        documentToUpdate.setDepartment(department);
//        documentToUpdate.setDueDate(dueDate);
//        documentToUpdate.setCreatedBy(SecurityUtils.getCurrentUserLogin());
//        log.info("user crested by has worked");
//        // Save the updated document
//        return documentRepository.save(documentToUpdate);
//    }

//    private DocumentModel getDocumentByIdentifier(String documentIdentifier) {
//        // Try to fetch by document ID
//        try {
//            Long documentId = Long.parseLong(documentIdentifier);
//            return documentRepository.findById(documentId)
//                    .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
//        } catch (NumberFormatException e) {
//            // If not a valid ID, try to fetch by document name
//            return (DocumentModel) documentRepository.findByDocumentName(documentIdentifier)
//                    .orElseThrow(() -> new RuntimeException("Document not found with name: " + documentIdentifier));
//        }
//    }

