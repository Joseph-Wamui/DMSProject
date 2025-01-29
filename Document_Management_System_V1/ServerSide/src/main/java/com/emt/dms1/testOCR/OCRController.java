package com.emt.dms1.testOCR;


import com.emt.dms1.document.DocumentService;
import com.emt.dms1.utils.EntityResponse;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping(value = "/api/v1/Ocr" , produces = MediaType.APPLICATION_JSON_VALUE)
@NoArgsConstructor
@Slf4j
@ResponseBody
public class OCRController {
    @Autowired
    private OcrService ocrService;
    @Autowired
    private DocumentService documentService;

//    @Operation(summary = "Perform OCR on uploaded image")
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

    // Array of all available DocumentAttribute values (extracted from enum)
    private final String[] allAttributesArray = Arrays.stream(DocumentAttribute.values())
            .map(Enum::name)
            .toArray(String[]::new);



    //    @GetMapping("/uploaded Documents")
//    public ResponseEntity<List<DocumentAttributes>> getAll() {
//        try {
//            List<DocumentAttributes> allAttributes = ocrService.getAll();
//            return ResponseEntity.ok(allAttributes);
//        } catch (Exception e) {
//            log.error("Error retrieving all document attributes:", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//
//        }
//    }
//    @Operation(summary = "upload document")
//    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ApiResponse(responseCode = "201", description = "Document saved successfully",
//            content = @Content(schema = @Schema(implementation = DocumentModel.class)))
//    public ResponseEntity<DocumentModel> saveDocument(@RequestPart("file") MultipartFile file )
//            throws IOException {
//        DocumentModel savedDocument = documentService.saveDocument(file);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
//    }

    @GetMapping("/documentAttributes")
    public EntityResponse getDocumentAttributesByDocumentId(@RequestParam String documentId) {
        try {
            long parsedId = Long.parseLong(documentId);
            return ocrService.getKeyValuePairsByDocumentId(parsedId);
        } catch (NumberFormatException e) {
            EntityResponse errorResponse = new EntityResponse<>();
            errorResponse.setMessage("Invalid document ID: " + documentId);
            errorResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return errorResponse;
        }
    }

    @PostMapping(value = "/OcrExtract",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse extractAttributes(
            @RequestParam(required = false) List<String> keys,
            @RequestParam Long id,
            @RequestPart("file") MultipartFile file) throws IOException {

        return  ocrService.ocrSelector(file,id,keys);
    }

}


//    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public List<String> detectText(@RequestParam("image") MultipartFile image) throws IOException, TesseractException {
//        File tempFile = File.createTempFile("upload", ".png");
//        image.transferTo(tempFile);
//        List<String> results = textDetectionService.detectAndReadText(tempFile.getAbsolutePath());
//        tempFile.delete();
//        return results;
//    }









//        StringBuilder resultBuilder = new StringBuilder();
//        try {
//            // Check if the uploaded file is a PDF
//            if (!file.getContentType().equals("application/pdf")) {
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only PDF files are supported.");
//            }
//
//            // Create a temporary directory to store images
//            File tempDir = new File(System.getProperty("java.io.tmpdir"), "ocr_images");
//            tempDir.mkdirs();
//
//            // Load the PDF document
//            try (PDDocument document = PDDocument.load(file.getInputStream())) {
//                PDFRenderer renderer = new PDFRenderer(document);
//
//                // Iterate through each page and convert it to an image
//                for (int i = 0; i < document.getNumberOfPages(); i++) {
//                    BufferedImage image = renderer.renderImageWithDPI(i, 300);
//
//                    // Save the image to a temporary file
//                    File tempImageFile = new File(tempDir, "page_" + (i + 1) + ".jpg");
//                    javax.imageio.ImageIO.write(image, "jpg", tempImageFile);
//
//                    // Perform OCR on the image
//                    String result = ocrService.capture(tempImageFile);
//
//                    // Append OCR result
//                    resultBuilder.append("OCR RESULT FOR PAGE ").append(i + 1).append(": ").append(result).append("\n");
//                }
//            }
//
//            // Save OCR results
//            ocrService.saveResult(resultBuilder.toString(), id);
//
//            return ResponseEntity.ok(resultBuilder.toString());
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing PDF file.");
//        }
//    }


   /* @Operation(summary = "Process document and add attributes")
    @PutMapping("/process/{documentId}")
    @ApiResponse(responseCode = "200", description = "Document processed and attributes added successfully",
            content = @Content(schema = @Schema(implementation = DocumentModel.class)))
    public ResponseEntity<DocumentModel> processDocumentAndAddAttributes(
            @RequestParam Long    id,
            @RequestParam String notes,
            @RequestParam String approverComments,
            @RequestParam String department,
            @RequestParam String dueDate,
            @RequestParam String createdBy
          *//*  @RequestBody String   result*//*

    ) {
        DocumentModel processedDocument = documentService.processDocumentAndAddAttributes(
                id,
                notes,
                approverComments,
                department,
                LocalDate.parse(dueDate), // Parse dueDate String to LocalDate
                createdBy


        );
        return ResponseEntity.ok(processedDocument);
    }
*/

//    @Operation(summary = "Perform OCR on uploaded image")
//    @ApiResponse(responseCode = "200", description = "OCR result")
//    @ApiResponse(responseCode = "500", description = "Internal server error")
//    @PostMapping(value = "/capture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> performOCR(
//            @RequestPart("file") MultipartFile file) {
//        try {
//            // Create a temporary file from the MultipartFile
//            File tempfile = new File(System.getProperty("java.io.tmpdir"), "ocr_images");
//            File tempFile = File.createTempFile("tempImage", "jpg");
//            file.transferTo(tempFile);
////            String result = ocrService.capture(tempFile);
//            tempFile.delete();
//
//            return ResponseEntity.ok(result);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
//        }
//    }


//    @PostMapping(value = "/extract", produces = "application/json")
//    public ResponseEntity<DocumentAttributes> extractAttributes(
//            @RequestBody String documentContent,
//            @RequestParam(required = false) List<String> selectedAttributes) {
//        try {
//            EnumSet<DocumentAttribute> attributesSet = EnumSet.noneOf(DocumentAttribute.class);
//
//            // Validate and convert selected attributes with autocomplete support
//            if (selectedAttributes != null) {
//                for (String attribute : selectedAttributes) {
//                    if (attribute != null && !attribute.isEmpty() &&
//                            Arrays.stream(allAttributesArray).anyMatch(attr -> attr.equalsIgnoreCase(attribute))) {
//                        attributesSet.add(DocumentAttribute.valueOf(attribute.toUpperCase()));
//                    } else {
//                        // Handle invalid attribute (e.g., log warning or return bad request)
//                        log.warn("Invalid attribute provided: " + attribute);
//                        return ResponseEntity.badRequest().build();
//                    }
//                }
//            }
//
//            DocumentAttributes attributes = ocrService.extractAttributes(documentContent, attributesSet);
//            return ResponseEntity.ok(attributes);
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid arguments provided for extracting document attributes:", e);
//            return ResponseEntity.badRequest().build();
//        } catch (Exception e) { // Catch broad exception for unexpected errors
//            log.error("Error extracting document attributes:", e);
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }