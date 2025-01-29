package com.emt.dms1.documentAudit;

import com.emt.dms1.document.DocumentDto;
import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocumentLogService {

    @Autowired
    private DocumentLogRepository documentLogRepository;

    public void logDocumentAction(String userName, DocumentLogType actionType, DocumentModel document) {
        DocumentLog documentLog = new DocumentLog();

        documentLog.setTimestamp(LocalDateTime.now());
        documentLog.setUserName(userName);
        documentLog.setDocumentLogType(actionType);
        documentLog.setDocument(document);
        // Set other relevant fields

        documentLogRepository.save(documentLog);
    }

    public EntityResponse<List<Map<String, Object>>> getDocumentLogs(Long documentId) {
        EntityResponse<List<Map<String, Object>>> entityResponse = new EntityResponse<>();

        if (documentId == null) {
            entityResponse.setMessage("Document ID cannot be null");
            entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
            return entityResponse;
        }

        // Retrieve document logs
        log.info("Retrieving document logs for document ID: " + documentId);
        List<DocumentLog> documentLogs = documentLogRepository.findByDocumentIdOrderByTimestampDesc(documentId);
        List<Map<String, Object>> logMaps = new ArrayList<>();

        for (DocumentLog documentLog : documentLogs) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("timestamp", documentLog.getTimestamp());
            logMap.put("documentLogType", documentLog.getDocumentLogType());
            logMap.put("userEmail", documentLog.getUserName());
            logMaps.add(logMap);
        }
        log.info("Document logs retrieved successfully");
        entityResponse.setMessage("Document logs retrieved successfully");
        entityResponse.setEntity(logMaps);
            entityResponse.setStatusCode(HttpStatus.OK.value());

        return entityResponse;
    }

    public EntityResponse getLastFiveAccessedDocuments() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            List<DocumentLog> documentLogs = documentLogRepository.findByUserName(SecurityUtils.getCurrentUserLogin());
            if (documentLogs.isEmpty()){
                entityResponse.setMessage("Unable fetched documents accessed by the user.");
                entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
            }
            else {
                List<DocumentModel> documents = documentLogs.stream()
                        .map(DocumentLog::getDocument)
                        .filter(Objects::nonNull)
                        .distinct()
                        .limit(5)
                        .toList();

                entityResponse.setMessage("Successfully fetched the last " +documents.size()+ " documents accessed by the user.");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(documents);
            }
        } catch (Exception e) {
            e.printStackTrace();
            entityResponse.setMessage("Error occurred while retrieving last accessed documents.");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
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
        dto.setDocumentName(Document.getDocumentName());
        dto.setCreateDate(Document.getCreateDate());
        dto.setCreatedBy(Document.getCreatedBy());
        dto.setDueDate(Document.getDueDate());
        dto.setFileType(Document.getFileType());

        return dto;
    }


    public List<DocumentLog> getAllAuditTrails() {

        return documentLogRepository.findAll();
    }
}
