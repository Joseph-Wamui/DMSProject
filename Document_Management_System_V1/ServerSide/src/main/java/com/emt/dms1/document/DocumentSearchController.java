package com.emt.dms1.document;



import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(value = "api/v1/document-search", produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentSearchController {

    @Autowired
    private DocumentSearchService documentSearchService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(@RequestParam(value = "searchTerm", required = true) String searchTerm) {
        try {
            return documentSearchService.searchDocumentsWithMetrics(searchTerm);
        } catch (Exception e) {
            // Log and handle errors
            log.error("An unexpected error occurred while searching for documents: " + searchTerm + ". Error message: " + e.getMessage(), e);
            // Return an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }
    @GetMapping("/documents/search")
    public ResponseEntity<EntityResponse<List<DocumentModel>>> searchDocuments(
            @RequestParam(required = false) String documentName,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String fileType,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) List<String>  approverComments) {

        return documentSearchService.searchDocuments(
                documentName,
                notes,
                startDate,
                endDate,
                fileType,
                createdBy,
                department,
                status,
                approverComments
        );
    }



    @GetMapping("/documents/dueDate")
    public ResponseEntity<EntityResponse> getDocumentsByDueDate(
            @RequestParam(name = "startDate", required = true) LocalDate startDate,
            @RequestParam(name = "endDate", required = true) LocalDate endDate) {
        return ResponseEntity.ok(documentSearchService.findDocumentsByDueDateRange(startDate, endDate));
    }

    @GetMapping("/documents/createDate")
    public ResponseEntity<EntityResponse> getDocumentsByCreateDate(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {
        EntityResponse response = documentSearchService.findDocumentsByCreateDateRange(startDate, endDate);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }




    @GetMapping("/distinct-file-types")
    public EntityResponse<List<String>> getDistinctFileTypes() {
        return documentSearchService.findDistinctFileTypes();
    }




    @GetMapping("/documents/createdBy")
    public EntityResponse getDocumentsByCreatedBy(@RequestParam String createdBy) {
        return documentSearchService.findByCreatedBy(createdBy);
    }

    @GetMapping("/distinct-departments")
    public EntityResponse<List<String>> getDistinctDepartments(){
        return documentSearchService.findDistinctDepartments();
    }

    @GetMapping("/documents/count-date")
    public ResponseEntity<EntityResponse<Map<LocalDate, Long>>> getDocumentsCountByCreateDate() {
        EntityResponse<Map<LocalDate, Long>> response = documentSearchService.countDocumentsByCreatedDate();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


//    public EntityResponse getDocumentsByDepartment(@RequestParam String department) {
//        return documentSearchService.findByDepartment(department);
//    }


//    @GetMapping("/documents/comments")
//    public ResponseEntity<?> getDocumentsByApproverComments(@RequestParam List<String>  approverComments) {
//        return ResponseEntity.ok(documentSearchService. findDocumentsByApproverComments(approverComments));
//    }
}

