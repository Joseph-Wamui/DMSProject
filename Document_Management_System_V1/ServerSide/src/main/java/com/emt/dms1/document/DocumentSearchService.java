package com.emt.dms1.document;


import com.emt.dms1.utils.EntityResponse;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service

public class DocumentSearchService {
    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private SearchMetricsRepo searchMetricsRepo;

    private final Logger log = LoggerFactory.getLogger(DocumentSearchService.class);


    @Autowired
    public DocumentSearchService(DocumentRepository documentRepository, SearchMetricsRepo searchMetricsRepo) {
        this.documentRepository = documentRepository;
        this.searchMetricsRepo = searchMetricsRepo;
    }

    private final AtomicLong totalSearches = new AtomicLong(0);
    private final AtomicLong totalSearchTime = new AtomicLong(0);
    private final List<Long> responseTimes = new ArrayList<>();
    private final AtomicLong errorCount = new AtomicLong(0);
    //private final AtomicLong ZeroErrorCount= new AtomicLong(0);
    private final AtomicLong zeroErrorCount = new AtomicLong(0);
    private final AtomicLong zeroRateResultCount = new AtomicLong(0);
    private AtomicInteger totalSearchCounter = new AtomicInteger(0);
    private AtomicLong responseTime = new AtomicLong(0);

    public EntityResponse<Map<LocalDate, Long>> countDocumentsByCreatedDate() {
        EntityResponse<Map<LocalDate, Long>> entityResponse = new EntityResponse<>();
        List<Object[]> results = documentRepository.countDocumentsByCreateDate();

        if (results.isEmpty()) {
            log.warn("--> no found documents<--");
            entityResponse.setMessage("No documents found ");
            entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
        } else {
            Map<LocalDate, Long> documentCountMap = new HashMap<>();
            for (Object[] result : results) {
                LocalDate date = (LocalDate) result[0];
                Long count = (Long) result[1];
                documentCountMap.put(date, count);
            }
            log.warn("-->DOCUMENTS FOUND <--");
            entityResponse.setMessage("Documents found ");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            //System.out.println(documentCountMap);
            entityResponse.setEntity(documentCountMap);
        }
        return entityResponse;
    }


    public ResponseEntity<?> searchDocumentsWithMetrics(String searchTerm) {
        long startTime = System.currentTimeMillis();
        try {
            List<DocumentModel> results = documentRepository.findByDocumentNameIgnoreCase(searchTerm);
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

            // Update metrics
            totalSearches.incrementAndGet();
            totalSearchTime.addAndGet(duration);
            responseTimes.add(duration);

            if (!results.isEmpty()) {
                return ResponseEntity.ok(results);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception exception) {
            // Log and handle errors
            exception.printStackTrace();
            errorCount.incrementAndGet();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred."));
        }
    }

    // Additional methods for calculating metrics...

    // Method to calculate average response time
    public double calculateAverageResponseTime() {
        if (totalSearches.get() == 0) {
            return 0;
        }
        return (double) totalSearchTime.get() / totalSearches.get();
    }

    // Method to calculate search throughput
    public double calculateSearchThroughput(long timeIntervalInSeconds) {
        if (timeIntervalInSeconds <= 0) {
            return 0;
        }
        return (double) totalSearches.get() / timeIntervalInSeconds;
    }

    // Method to calculate query latency distribution (percentiles)
    public double calculatePercentile(int percentile) {
        if (responseTimes.isEmpty()) {
            return 0;
        }
        Collections.sort(responseTimes);
        int index = (int) Math.ceil((double) percentile / 100 * responseTimes.size());
        return responseTimes.get(index - 1);
    }

    // Method to calculate error rate
    public double calculateErrorRate() {
        return (double) errorCount.get() / totalSearches.get();
    }



    public EntityResponse findByCreatedBy(String createdBy) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->FETCHING DOCUMENTS<-------");
            List<DocumentModel> exists = documentRepository.findByCreatedBy(createdBy);
            if (exists.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->DOCUMENT CREATED BY: NAME: DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("DOCUMENT CREATED BY: NAME: DOES NOT EXIST");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                entityResponse.setMessage("Document Created By:" + "Name" + "is found ");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(exists);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENT WITH THIS CREATOR'S NAME<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse findByDepartment(String department) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->RETRIEVING DOCUMENTS<-------");
            List<DocumentModel> documents = documentRepository.findByDepartment(department);
            if (documents.isEmpty()) {

                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->DOCUMENTS FROM THIS DEPARTMENT DO NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("DOCUMENTS FROM THIS DEPARTMENT DO NOT EXIST");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);

            } else {
                entityResponse.setMessage("Documents from Department: " + department + " are found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(documents);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENTS WITH THIS DEPARTMENT<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse; // Return the list of documents
    }

    //List<DocumentEntity>
    public EntityResponse findDocumentsByDueDateRange(LocalDate startDate, LocalDate endDate) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->RETRIEVING DOCUMENTS<-------");
            // Adjusting the upper limit of endDate to include all records for that day
            endDate = endDate.plusDays(1).atStartOfDay().toLocalDate();

            List<DocumentModel> exists = documentRepository.findByDueDateBetween(startDate, endDate);
            if (!exists.isEmpty()) {
                entityResponse.setMessage("Documents with due dates within the specified range are found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(exists);
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->NO DOCUMENTS FOUND WITHIN THE SPECIFIED DATE RANGE<-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("No documents found with due dates within the specified range");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENTS WITH THIS DUE DATE RANGE --: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse findDocumentsByCreateDateRange(LocalDate startDate, LocalDate endDate) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            log.info("----->RETRIEVING DOCUMENTS<-------");

            // Adjusting the upper limit of endDate to include all records for that day
            endDate = endDate.plusDays(1).atStartOfDay().toLocalDate();

            List<DocumentModel> exists = documentRepository.findByCreateDateBetween(startDate, endDate);

            if (exists.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->NO DOCUMENTS FOUND WITHIN THE SPECIFIED UPLOAD DATE RANGE<-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("No documents found within the specified upload date range");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            } else {
                entityResponse.setMessage("Documents uploaded within the specified date range are found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(exists);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENTS WITH THIS UPLOADED DATE RANGE --: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

//    public List<DocumentModel> findDocumentsByApproverComments(List<String> approverComments) {
//        try {
//            log.info("----->SEARCHING DOCUMENTS BY APPROVER COMMENTS<-------");
//
//            // Check if approverComments is null or empty
//            if (approverComments == null || approverComments.isEmpty()) {
//                log.warn("Empty or null approver comments provided for search.");
//                return null; // Or return an empty list if preferred
//            }
//
//            // Perform the query using the repository method to find documents with the specified approver comments
//            List<DocumentModel> documents = documentRepository.findByApproverCommentsContainingIgnoreCase(approverComments);
//
//            if (documents.isEmpty()) {
//                log.warn("'''''''''''''''warning''''''''''''''");
//                log.warn("-->NO DOCUMENTS FOUND WITH THE SPECIFIED APPROVER COMMENTS<-- ");
//                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");
//
//                return null; // Or return an empty list if preferred
//            } else {
//                log.info("Documents found with the specified approver comments: " + documents.size());
//                return documents;
//            }
//        } catch (Exception exception) {
//            log.error("Error searching documents by approver comments: " + exception.getMessage(), exception);
//            return null; // Or handle the error as required (e.g., throw an exception)
//        }
//
//
//    }

    public ResponseEntity<EntityResponse<List<DocumentModel>>> searchDocuments(String documentName,
                                                                               String notes,
                                                                               LocalDate startDate,
                                                                               LocalDate endDate,
                                                                               String fileType,
                                                                               String createdBy,
                                                                               String department,
                                                                               String status,
                                                                               List<String> approverComments) {
        log.info("----->FETCHING DOCUMENTS<-------");
        long startTime = System.currentTimeMillis();


        try {
            totalSearchCounter.incrementAndGet();
            List<DocumentModel> documents = documentRepository.findAll((root, query, builder) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (documentName != null && !documentName.isEmpty()) {
                    predicates.add(builder.like(builder.lower(root.get("documentName")), "%" + documentName.toLowerCase() + "%"));
                }
                if (notes != null && !notes.isEmpty()) {
                    predicates.add(builder.like(builder.lower(root.get("notes")), "%" + notes.toLowerCase() + "%"));
                }
                if (startDate != null && endDate != null) {
                    predicates.add(builder.between(root.get("createDate"), startDate, endDate));
                } else if (startDate != null) {
                    predicates.add(builder.greaterThanOrEqualTo(root.get("createDate"), startDate));
                } else if (endDate != null) {
                    predicates.add(builder.lessThanOrEqualTo(root.get("createDate"), endDate));
                }
                if (fileType != null && !fileType.isEmpty()) {
                    predicates.add(builder.equal(builder.lower(root.get("fileType")), fileType.toLowerCase()));
                }
                if (status!= null && !status.isEmpty()) {
                    predicates.add(builder.equal(builder.lower(root.get("status")), status.toLowerCase()));
                }
                if (createdBy != null && !createdBy.isEmpty()) {
                    predicates.add(builder.equal(builder.lower(root.get("createdBy")), createdBy.toLowerCase()));
                }
                if (department != null && !department.isEmpty()) {
                    predicates.add(builder.equal(builder.lower(root.get("department")), department.toLowerCase()));
                }
                if (approverComments != null && !approverComments.isEmpty()) {
                    predicates.add(builder.like(builder.lower(root.get("approverComments")), "%" + approverComments.toArray() + "%"));
                }
                return builder.and(predicates.toArray(new Predicate[0]));
            });
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;

//            System.out.println("startTime:" +" "+ startTime);
//            System.out.println("endTime:" +" "+ endTime);
//            System.out.println("duration:" +" "+ duration);

            totalSearches.incrementAndGet();
            totalSearchTime.addAndGet(duration);
            responseTime.addAndGet(duration);


            updateSearchMetrics();
            //System.out.println("called metrics service");
            //enter zero result rate

            if (documents.isEmpty()) {
                String errorMessage = "No document found with the provided search criteria";
                log.warn(errorMessage);
                zeroRateResultCount.incrementAndGet();


                EntityResponse<List<DocumentModel>> entityResponse = new EntityResponse<>();
                entityResponse.setMessage(HttpStatus.NOT_FOUND.getReasonPhrase() + ": " + errorMessage);
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(Collections.emptyList());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(entityResponse);

            } else {

                EntityResponse<List<DocumentModel>> entityResponse = new EntityResponse<>();
                entityResponse.setMessage(HttpStatus.OK.getReasonPhrase() + ": Documents found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(documents);
                return ResponseEntity.ok(entityResponse);
            }
        } catch (Exception ex) {
            String errorMessage = HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + ex.getLocalizedMessage();
            log.error("Error occurred while fetching documents: {}", errorMessage);
            EntityResponse<List<DocumentModel>> entityResponse = new EntityResponse<>();
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + errorMessage);
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(Collections.emptyList());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(entityResponse);
        } finally {
            long endTime = System.currentTimeMillis();
            responseTime.addAndGet(endTime - startTime);
        }
    }

    @Scheduled(fixedRate = 60000) // Schedule every minute
    public void updateSearchMetrics() {
        log.info("Updating search metrics...");
        long totalResponseTime = getResponseTime();
        int totalSearches = getTotalSearches();

        double avgResponseTime = calculateAverageResponseTime(totalResponseTime, totalSearches);
        double searchThroughput = calculateSearchThroughput(totalSearches);
        long zeroRateResultsCount = calculateZeroRateResultCount(); // Calculate zero-rate result count
        double zeroRateResultsPercentage = calculateZeroRateResultPercentage(); // Calculate zero-rate result percentage

// zeroRateResultCount
        log.debug("Total Response Time: {}", totalResponseTime);
        log.debug("Total Searches: {}", totalSearches);
        log.debug("Average Response Time: {}", avgResponseTime);
        log.debug("Search Throughput: {}", searchThroughput);


        // Create a new SearchMetrics object, set its attributes, and save it to the database
        SearchMetrics searchMetrics = new SearchMetrics();
        searchMetrics.setZeroRateResults(zeroRateResultsCount);
        searchMetrics.setZeroRateResultPercentage(zeroRateResultsPercentage);
        searchMetrics.setTotalResponseTime(totalResponseTime);
        searchMetrics.setTotalSearches(totalSearches);
        searchMetrics.setAverageResponseTime(avgResponseTime);
        searchMetrics.setSearchThroughput(searchThroughput);
        searchMetrics.setTimestamp(LocalDateTime.now());

        log.info("Search metrics before saving: {}", searchMetrics);
        searchMetricsRepo.save(searchMetrics);


        // Reset in-memory counters in SearchService
        resetResponseTime();
        resetTotalSearches();
    }

    // Method to calculate average response time
    private double calculateAverageResponseTime(long totalResponseTime, int totalSearches) {
        if (totalSearches == 0) {
            return 0;
        }
        return (double) totalResponseTime / totalSearches;
    }

    // Method to calculate search throughput

    private double calculateSearchThroughput(int totalSearches) {
        return (double) totalSearches / (60.0); // Throughput per minute
    }
    public double calculateZeroRateResultPercentage() {
        if (totalSearches.get() == 0) {
            return 0.0; // Return 0.0 if totalSearches is zero to avoid division by zero
        }
        long zeroRateResults = zeroRateResultCount.get();
        long totalSearchesValue = totalSearches.get();
        return ((double) zeroRateResults / totalSearchesValue) * 100;
    }
    public long calculateZeroRateResultCount() {
        return zeroRateResultCount.get();
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

        return dto;
    }

    public long getResponseTime() {
        return responseTime.get();
    }
    public int getTotalSearches() {
        return totalSearchCounter.get();
    }

    public void resetResponseTime() {
        responseTime.set(0);

    }

    public void resetTotalSearches() {
        totalSearchCounter.set(0);
    }

    public EntityResponse<List<String>> findDistinctFileTypes() {
        EntityResponse<List<String>> entityResponse = new EntityResponse<>();


        List<String> distinctFiletypes = documentRepository.findDistinctFileTypes();
        if (distinctFiletypes.isEmpty()){
            entityResponse.setMessage("Not found");
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        }
        entityResponse.setMessage("found");
        entityResponse.setStatusCode(HttpStatus.OK.value());
        entityResponse.setEntity(distinctFiletypes);
        return entityResponse;
    }

    public EntityResponse<List<String>> findDistinctDepartments() {
        EntityResponse<List<String>> entityResponse = new EntityResponse<>();
        List<String> distinctDepartments = documentRepository.findDistinctDepartments();
        if (distinctDepartments.isEmpty()) {
            entityResponse.setMessage("No Departments were found");
            entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
        }
        entityResponse.setMessage("Departments found");
        entityResponse.setStatusCode(HttpStatus.OK.value());
        entityResponse.setEntity(distinctDepartments);
        return entityResponse;
    }
}

