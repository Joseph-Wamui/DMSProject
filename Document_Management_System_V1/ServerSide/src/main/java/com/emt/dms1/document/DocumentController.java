package com.emt.dms1.document;


import com.emt.dms1.documentAudit.DocumentLogService;
import com.emt.dms1.documentAudit.DocumentLogType;
import com.emt.dms1.testOCR.QRCodeGenerator;
import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(value = "/api/v1/Documents" ,produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentLogService documentLogService;
    private static final String FILE_STORAGE_PATH = "D:\\DmsDocs\\";

    public DocumentController(DocumentService documentService, DocumentLogService documentLogService) {
        this.documentService = documentService;
        this.documentLogService = documentLogService;
    }


    @GetMapping("/{fileName}")
    public ResponseEntity getFile(@PathVariable String fileName) {
        try {
            // Construct the file path
            Path filePath = Paths.get(FILE_STORAGE_PATH + fileName);

            // Check if the file exists and is not a directory
            if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
                return ResponseEntity.notFound().build();
            }

            // Read the file content
            byte[] fileContent = Files.readAllBytes(filePath);

            // Set content disposition to attachment
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDisposition(ContentDisposition.builder("attachment").filename(fileName).build());

            // Return the file content with appropriate headers
            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } catch (IOException e) {
            // Handle file read errors
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to read file: " + e.getMessage());
        }
    }

    @GetMapping("/last-five-accessed")
    public EntityResponse getLastFiveAccessedDocuments() {
        return  documentLogService.getLastFiveAccessedDocuments();
    }



    @PostMapping(value = "/upload_and_save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)  // Specific endpoint for upload with attributes
    public EntityResponse<DocumentModel> saveDocumentAndAddAttributes(
            @RequestParam(value = "documentName", required = false) String documentName,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "dueDate", required = false) LocalDate dueDate,
            @RequestParam(value = "location", required = false) String fileLocation
    ) throws Exception {
        return documentService.uploadDocument(documentName, file, notes, tags,
                dueDate, fileLocation);
    }

//    @PostMapping(value = "/upload_and_save2", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public EntityResponse saveDocumentAndAddAttributes(
//            @RequestPart("file") MultipartFile file,
//            @RequestBody DocumentUploadRequest request
//    ) throws IOException {
//        return ResponseEntity.ok(documentService.saveDocumentAndAddAttributes2(request, file));
//
//    }



    @PostMapping(value = "/uploadMultiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse<List<DocumentModel>> saveDocumentsAndAddAttributes(
            @RequestParam("files") List<MultipartFile> files
    ) {
        log.info(String.format("Uploading multiple %d", files.size()));
        return documentService.uploadMultipleDocuments(files);
    }


    @PutMapping(value = "/updateattributes")
    public EntityResponse updateAttributes(
            @RequestParam("documentId") String documentId,
            @RequestParam(value = "notes", required = false) String notes,
            @RequestParam(value = "tags", required = false) List<String> tags,
            @RequestParam(value = "approverComments", required = false)List<String> approverComments,
            @RequestParam(value = "dueDate", required = false) LocalDate dueDate
    ) {
        Long parsedDocumentId = Long.parseLong(documentId);

        return documentService.updateAttributes(
                parsedDocumentId,
                notes,
                tags,
                approverComments,
                dueDate
        );
    }


    @GetMapping("/downloadDocument")
    public ResponseEntity<ByteArrayResource> downloadDocument(@RequestParam String id) throws Exception {

        long documentId = Long.parseLong(id);

        // Fetch the document by ID
        DocumentModel documentToDownload = documentService.findDocumentById(documentId);

        try {
            // Decrypt the document data
            byte[] decryptedDocumentData = documentService.decryptDocumentData(documentToDownload.getDocumentData());

            // Prepare the response
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//            headers.setContentDispositionFormData("attachment", documentToDownload.getDocumentName());

            // Convert document data to ByteArrayResource

            // Log the user's download action
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();
            log.info("User '" + loggedInUsername + "' downloaded document '" + documentToDownload.getDocumentName() + "'");

            // Log the audit trail for document download
            documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Downloaded_the_document, documentToDownload);

            ByteArrayResource resource = new ByteArrayResource(decryptedDocumentData);


            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + documentToDownload.getDocumentName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            // Handle decryption errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/FetchAllDocuments")
    public EntityResponse getAllDocuments() {
        return documentService.fetchDocuments();
    }

    @GetMapping("/getMyDocuments")
    public EntityResponse getMYDocuments() {
        return documentService.getMyDocuments();
    }

    @GetMapping("/FetchDocumentNamesList")
    public EntityResponse getAllDocumentsNames() {
        return documentService.fetchDocumentNames();
    }



    @GetMapping("/getDocumentById")
    public EntityResponse getDocumentById(@RequestParam String id) {
        long parsedId = Long.parseLong(id);

        return documentService.getDocumentById2(parsedId);

    }

//    @PutMapping("/uploadNewVersion")
//    public EntityResponse uploadNewVersion(DocumentModel documentModel) {
//        return documentService.uploadNewVersion(documentModel);
//    }

    @DeleteMapping("/deleteDocumentById")
    public EntityResponse<DocumentModel> deleteDocument(@RequestParam String documentId) {
        Long parsedId = Long.parseLong(documentId);
        return documentService.deleteDocument(parsedId);
    }


    @GetMapping("/getStatus")
    public  EntityResponse getDocstatus(){
        return documentService.getStatusList();
    }


    @GetMapping("/generateQRCode")
    public EntityResponse generateQRCodeForDocument(@RequestParam String documentId) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            Long parsedId = Long.parseLong(documentId);
            DocumentModel documentModel = documentService.findDocumentById(parsedId);
            if (documentModel != null) {
                byte[] qrData = QRCodeGenerator.generateQRCode(documentModel);

                String contentType;
                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(qrData)) {
                    contentType = URLConnection.guessContentTypeFromStream(byteArrayInputStream);
                }

                // Create a Map to hold both QR data and content type
                Map<String, Object> qrResponse = new HashMap<>();
                qrResponse.put("qrData", qrData);
                qrResponse.put("contentType", contentType);

                entityResponse.setMessage("QR code generated and stored successfully.");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(qrResponse);

            } else {
                entityResponse.setMessage("Document not found.");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            entityResponse.setMessage("Error generating QR code: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    @GetMapping("/total")
    public EntityResponse documentsNumber(){
        return documentService.getDocumentsNumber();
    }

}

//
//@Operation(summary = "Save document")
//@PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//@ApiResponse(responseCode = "201", description = "Document saved successfully",
//        content = @Content(schema = @Schema(implementation = DocumentModel.class)))
//public ResponseEntity<DocumentModel> saveDocument(@RequestPart("file") MultipartFile file )
//        throws IOException {
//    DocumentModel savedDocument = documentService.saveDocument(file);
//
//    return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
//}
//
//@GetMapping("/download/{documentId}")
//public ResponseEntity<ByteArrayResource> downloadDocument(@PathVariable Long documentId) {
//    // Fetch the document by ID
//    DocumentModel documentToDownload = documentService.findDocumentById(documentId);
//
//    // Prepare the response
//    HttpHeaders headers = new HttpHeaders();
//    headers.setContentType(MediaType.parseMediaType(documentToDownload.getFileType()));
//    headers.setContentDispositionFormData("attachment", documentToDownload.getDocumentName());
//
//    // Convert document data to ByteArrayResource
//    ByteArrayResource resource = new ByteArrayResource(documentToDownload.getDocumentData());
//
//    return ResponseEntity.ok()
//            .headers(headers)
//            .contentLength(documentToDownload.getDocumentData().length)
//            .body(resource);
//}
//@Operation(summary = "Process document and add attributes")
//@PatchMapping("/process/{documentId}")
//@ApiResponse(responseCode = "200", description = "Document processed and attributes added successfully",
//        content = @Content(schema = @Schema(implementation = DocumentModel.class)))
//public ResponseEntity<DocumentModel> processDocumentAndAddAttributes(
//        @PathVariable Long documentId,
//        @RequestParam String notes,
//        @RequestParam String approverComments,
//        @RequestParam String department,
//        @RequestParam String dueDate,
//        @RequestParam String createdBy
//) {
//    DocumentModel processedDocument = documentService.processDocumentAndAddAttributes(
//            documentId,
//            notes,
//            approverComments,
//            department,
//            LocalDate.parse(dueDate), // Parse dueDate String to LocalDate
//            createdBy
//    );
//    return ResponseEntity.ok(processedDocument);
//}

//    @ApiResponse(responseCode = "200", description = "OCR result")
//    @ApiResponse(responseCode = "500", description = "Internal server error")
//    @PutMapping(value = "/capturePdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> performOCRPDF(@RequestPart("file") MultipartFile file,
//                                                @RequestParam Long id,
//                                                @RequestParam String notes,
//                                                @RequestParam String approverComments,
//                                                @RequestParam String department,
//                                                @RequestParam String dueDate,
//                                                @RequestParam String createdBy) {
//
//        DocumentModel processedDocument = documentService.processDocumentAndAddAttributes(id, notes, approverComments, department, LocalDate.parse(dueDate), createdBy); // Parse dueDate String to LocalDatecreatedBy);
//        var result=ocrService.performOCRPDFex(file, id);
//     return result;
//    }
//    @Operation(summary = "adding document metadata ")
//    @PutMapping("/editing document Metadata}")
////    @ApiResponse(responseCode = "200", description = "Document processed and attributes added successfully")
//    public ResponseEntity<DocumentModel> processDocumentAndAddAttributes(
//            @PathVariable String documentIdentifier, // Accepts either document name or document ID
//            @RequestParam(required = false) String newDocumentName,
//            @RequestParam String notes,
//            @RequestParam String approverComments,
//            @RequestParam String department,
//            @RequestParam String dueDate,
//            @RequestParam String createdBy
//    ) {
//        DocumentModel processedDocument2 = documentService.processDocumentAndAddAttributes(
//                documentIdentifier,
//                newDocumentName,
//                notes,
//                approverComments,
//                department,
//                LocalDate.parse(dueDate),
//                createdBy
//        );
//        return ResponseEntity.ok(processedDocument2);
//    }