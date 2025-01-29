package com.emt.dms1.report_generation;


import com.emt.dms1.archiving.Archiving;
import com.emt.dms1.user.UserDTO;
import com.emt.dms1.user.UserRepository;
import com.emt.dms1.archiving.ArchivingDTO;
import com.emt.dms1.archiving.ArchivingRepository;
import com.emt.dms1.backup.Backup;
import com.emt.dms1.backup.BackupDTO;
import com.emt.dms1.backup.BackupRepository;
import com.emt.dms1.document.*;
import com.emt.dms1.documentAudit.DocumentLog;
import com.emt.dms1.documentAudit.DocumentLogDto;
import com.emt.dms1.documentAudit.DocumentLogRepository;
import com.emt.dms1.documentAudit.DocumentLogType;
import com.emt.dms1.organizationInfo.CompanyDetailsRepo;
import com.emt.dms1.user.User;
import com.emt.dms1.user.UserService;
import com.emt.dms1.userAudit.UserLog;
import com.emt.dms1.userAudit.UserLogDto;
import com.emt.dms1.userAudit.UserLogRepository;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.versioning.Version;
import com.emt.dms1.versioning.versionRepotitory;
import com.emt.dms1.workFlow.ApprovalWorkflow;
import com.emt.dms1.workFlow.WorkflowDto;
import com.emt.dms1.workFlow.WorkflowStepRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class analyticsService {
    @Autowired
    private DocumentLogRepository documentLogRepository;
    @Autowired
    private versionRepotitory versionRepository;
    @Autowired
    private BackupRepository backupRepository;
    @Autowired
    private ArchivingRepository archivingRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserLogRepository userLogRepository;
    @Autowired
    SearchMetricsRepo searchMetricsRepo;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    WorkflowStepRepository workflowStepRepository;

    private final DocumentService documentService;

    private final UserService userService;

    private final CompanyDetailsRepo companyDetailsRepo;

    public analyticsService(DocumentService documentService, UserService userService, CompanyDetailsRepo companyDetailsRepo) {
        this.documentService = documentService;
        this.userService = userService;
        this.companyDetailsRepo = companyDetailsRepo;
    }

    public List<Version> getAllVersions(Long documentModelId) {
        return versionRepository.findByDocumentModelId(documentModelId);
    }

    public Integer getLastVersionNumberByDocumentModelId(Long documentId) {
        return versionRepository.findLastVersionNumberByDocumentModelId(documentId);
    }


    public List<DocumentModel> getAllDocuments() {

        return documentRepository.findAll();
    }

    public EntityResponse getAllMetrics() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {

            List<SearchMetrics> search = searchMetricsRepo.findAll();
            if (!search.isEmpty()) {
                entityResponse.setMessage("search metrics data found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(search);

            } else {
                entityResponse.setMessage("No search metrics data found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            e.printStackTrace();
            entityResponse.setMessage("Something went wrong");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }


    public List<DocumentModel> getDocumentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return documentRepository.findByCreateDateBetween(startDate, endDate);

    }

    private List<UserLogDto> mapEntitiesToDTOs(List<UserLog> userLogs) {
        return userLogs.stream()
                .map(this::mapUserLogsToDTo)
                .collect(Collectors.toList());
    }

    private List<WorkflowDto> mapWorkflowsToDTOs(List<ApprovalWorkflow> workflow) {
        return workflow.stream()
                .map(this::mapWorkflowToDTO)
                .collect(Collectors.toList());
    }

    public EntityResponse getAllUserLogs() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {

            List<UserLog> user = userLogRepository.findAll();
            List<UserLogDto> dtos = mapEntitiesToDTOs(user);
//            entityResponse.setMessage("User logs retrieved successfully found");
//            entityResponse.setStatusCode(HttpStatus.OK.value());
//            entityResponse.setEntity(dtos);
            if (!dtos.isEmpty()){
                entityResponse.setMessage("user logs data found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos);
            }else {
                entityResponse.setMessage("user logs data not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());

            }
        } catch (Exception e) {
            e.printStackTrace();
            entityResponse.setMessage("INTERNAL SERVER ERROR");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    private WorkflowDto mapWorkflowToDTO(ApprovalWorkflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setApprovers(Collections.singletonList(workflow.getApprovers().toString()));
        dto.setAssigner(workflow.getAssigner());
        dto.setType(workflow.getType());
        //dto.setStepDurations(Collections.singletonList(workflow.getStepDurations().toString()));
        dto.setTotalDuration(workflow.getTotalDuration());
        //dto.setDocumentName(workflow.getDocumentModel().getDocumentName());
        return dto;
    }

    private UserLogDto mapUserLogsToDTo(UserLog userLog) {
        UserLogDto user = new UserLogDto();
        user.setUserName(userLog.getUserName());
        user.setId(userLog.getUserLogId());
        user.setEmailAddress(userLog.getUser().getEmailAddress());
        user.setTimestamp(userLog.getTimestamp());
        user.setUserLogType(userLog.getUserLogType());
        user.setEmployeeNumber(userLog.getUser().getEmployeeNumber());
        user.setDepartment(userLog.getUser().getDepartment());
        return user;
    }

    public EntityResponse getAllDocumentLogType() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            // Retrieve document logs from the repository
            List<Object[]> logs = documentLogRepository.findDocumentLogs();

            // Map the logs to DTOs
            List<DocumentLogDto> dtoList = mapDocumentLogsToDTOs(logs);

            // Set the successful response details
            entityResponse.setMessage("Document logs found");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(dtoList);
        } catch (Exception e) {
            // Log the exception stack trace
            e.printStackTrace();

            // Set the error response details
            entityResponse.setMessage("SOMETHING WENT WRONG");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    private List<DocumentLogDto> mapDocumentLogsToDTOs(List<Object[]> documentLogs) {
        // Map each document log to a DTO
        return documentLogs.stream()
                .map(this::mapDocumentLogToDto)
                .collect(Collectors.toList());
    }

    private DocumentLogDto mapDocumentLogToDto(Object[] documentLog) {
        DocumentLogDto dto = new DocumentLogDto();

        // Set the properties of the DTO
        dto.setDocumentLogType(DocumentLogType.valueOf((String) documentLog[2]));
        dto.setUserName((String) documentLog[1]);
        dto.setDocumentId((Long) documentLog[3]);

        // If needed, uncomment and set additional properties
        dto.setTimestamp(((Timestamp) documentLog[0]).toLocalDateTime());
        // dto.setDocument((String) documentLog[4]);

        return dto;
    }


    public EntityResponse getAllWorkflows() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            List<ApprovalWorkflow> exist = workflowStepRepository.findAll();
            List<WorkflowDto> dtos = mapWorkflowsToDTOs(exist);
            entityResponse.setMessage("Workflow data retrieved successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(dtos);
            if (!dtos.isEmpty()) {
                entityResponse.setMessage("Workflow Data Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos);
            } else {
                entityResponse.setMessage("Workflow Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            entityResponse.setMessage("SOMETHING WENT WRONG");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
        return entityResponse;

    }

    public List<Version> getAllVersions() {
        return versionRepository.findAll();
    }


    private List<ArchivingDTO> mapArchivesToDTOs(List<Archiving> archives) {
        return archives.stream()
                .map(this::MapArchivedDocumentsToDTO)
                .collect(Collectors.toList());
    }

    private ArchivingDTO MapArchivedDocumentsToDTO(Archiving archiving) {
        ArchivingDTO archive = new ArchivingDTO();
        archive.setCreatedBy(archiving.getCreatedBy());
        archive.setDepartment(archiving.getDepartment());
        archive.setRetentionPeriodInMinutes(archiving.getRetentionPeriodInMinutes());
        archive.setDocumentName(archiving.getDocumentName());
        archive.setId(archiving.getArchivingid());
        return archive;
    }

    public EntityResponse getAllArchivedDocuments() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            List<Archiving> arch = archivingRepository.findAll();
            List<ArchivingDTO> dto2 = mapArchivesToDTOs(arch);
            if (!dto2.isEmpty()) {
                entityResponse.setMessage("Archiving Data Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dto2);
            } else {
                entityResponse.setMessage("Archiving Data Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            e.printStackTrace();
            entityResponse.setMessage("SOMETHING WENT WRONG");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    private BackupDTO mapBackedupDocumentsToDTO(Backup backup) {
        BackupDTO back = new BackupDTO();
        back.setBackupId(backup.getBackup_id());
        back.setBackUpDate(backup.getBackUpDate());
        back.setDocumentName(backup.getDocumentName());
        back.setCreatedBy(backup.getCreatedBy());
        back.setDepartment(backup.getDepartment());
        return back;
    }

    private List<BackupDTO> mapBackupToDTOs(List<Backup> backups) {
        return backups.stream()
                .map(this::mapBackedupDocumentsToDTO)
                .collect(Collectors.toList());
    }

    public EntityResponse getAllBackedupDocuments() {
        EntityResponse entityResponse = new EntityResponse<>();
        try {

            List<Backup> back1 = backupRepository.findAll();
            List<BackupDTO> dtos2 = mapBackupToDTOs(back1);
           // System.out.println(dtos2);
            if (!dtos2.isEmpty()) {
                entityResponse.setMessage("Backup Data found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos2);
            } else {
                entityResponse.setMessage("No Backup Data found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e) {
            e.printStackTrace();
            entityResponse.setMessage("INTERNAL SERVER ERROR");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }
    private StorageDTO mapDocumentStorageToDTO(DocumentModel documentModel){
        StorageDTO storage= new StorageDTO();
        storage.setDepartment(documentModel.getDepartment());
        storage.setFileSize(documentModel.getFileSize());
        storage.setFileType(documentModel.getFileType());
        storage.setId(documentModel.getId());
        return storage;

}
    private List<StorageDTO> mapStorageToDTOs(List<DocumentModel> documentModels) {
        return documentModels.stream()
                .map(this::mapDocumentStorageToDTO)
                .collect(Collectors.toList());
    }

    public EntityResponse getAllDocumentsStorageSize() {
        EntityResponse entityResponse=new EntityResponse<>();
        try {
            List<DocumentModel> doc = documentRepository.findAll();
            List<StorageDTO> dtos3 = mapStorageToDTOs(doc);
            System.out.println(dtos3);
            if (!dtos3.isEmpty()) {

                entityResponse.setMessage("Documents Data Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos3);

            }else {
                entityResponse.setMessage("Documents Data Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e){
            e.printStackTrace();
            entityResponse.setMessage("INTERNAL SERVER ERROR");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public EntityResponse getAllDeletedDocuments() {
        EntityResponse entityResponse=new EntityResponse<>();
        try {
            List<DocumentModel> deletedDocuments = documentRepository.findByDocumentDeleteFlagOrderByIdDesc('Y');
            List<DocumentDto> dtos = documentService.mapEntitiesToDTOs(deletedDocuments);
            if (!dtos.isEmpty()) {
                entityResponse.setMessage("Deleted Documents Data Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos);

            }else {
                entityResponse.setMessage("Deleted Documents Data Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e){
            e.printStackTrace();
            entityResponse.setMessage("INTERNAL SERVER ERROR");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public EntityResponse getAllDeletedUsers() {
        EntityResponse entityResponse=new EntityResponse<>();
        try {
            List<User> userList = userRepository.findByDeletedFlagOrderByIdAsc('Y');
            List<UserDTO> dtos = userService.mapEntitiesToDTOs(userList);
            if (!dtos.isEmpty()) {

                entityResponse.setMessage("Documents Data Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos);

            }else {
                entityResponse.setMessage("Documents Data Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }

        } catch (Exception e){
            e.printStackTrace();
            entityResponse.setMessage("INTERNAL SERVER ERROR");
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }
}