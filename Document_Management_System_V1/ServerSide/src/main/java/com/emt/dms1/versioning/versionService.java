package com.emt.dms1.versioning;


import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.documentAudit.DocumentLogService;
import com.emt.dms1.documentAudit.DocumentLogType;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class versionService {

    @Autowired
    private versionRepotitory  versionRepo;
    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
   private DocumentLogService documentLogService;


    public versionService(List<Version> versions, versionRepotitory versionRepo, DocumentRepository documentRepository, versionDTO versionDTO) {
        this.versionRepo = versionRepo;
        this.documentRepository = documentRepository;

    }


    public EntityResponse createNewVersion(MultipartFile file ,  long documentId, String documentName) throws IOException {
        EntityResponse entityResponse= new EntityResponse<>();
        byte[] fileData = file.getBytes();

        try {
            Optional<DocumentModel> documentModelOptional = documentRepository.findById(documentId);
            if (documentModelOptional.isPresent()) {
                DocumentModel documentModel = documentModelOptional.get();
                Integer lastVersionNumber = versionRepo.findLastVersionNumberByDocumentModelId(documentId);
                int newVersionNumber = (lastVersionNumber != null) ? lastVersionNumber + 1 : 1;

                Version version= new Version();

                // Set attributes of the version
                version.setDepartment(documentModel.getDepartment());
                version.setDocumentName(documentName);
                version.setDocumentData(fileData);
                version.setDocumentModel(documentModel);
                version.setFileType(file.getContentType());
                version.setNotes(documentModel.getNotes());
                version.setDateUploaded(LocalDate.now());
                version.setCreatedBy(SecurityUtils.getCurrentUserLogin());
                version.setVersionNumber(newVersionNumber);
                version.setFileSize(documentModel.getFileSize());

                // Save the version
                Version savedVersion = versionRepo.save(version);

                // Log the creation of a new version
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String loggedInUsername = authentication.getName();
                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Added_new_version_of_the_document, savedVersion.getDocumentModel());



                // Update entityResponse
                entityResponse.setEntity(savedVersion);
                entityResponse.setMessage("New version uploaded successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                log.info("Version has been saved successfully");
            } else {
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setMessage("Document with ID " + documentId + " not found");
                log.error("Document with ID " + documentId + " not found");
            }
        } catch (Exception exception) {
            log.error("Error while creating new version: ", exception);
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }

    public  EntityResponse UpdateExistingVersion(MultipartFile file , String department, long documentId, List<String> approverscomments, String fileType, String notes, Integer versionNumber, String fileSize) throws IOException {
        EntityResponse entityResponse= new EntityResponse<>();
        String documentName = Objects.requireNonNull(file.getOriginalFilename());
        byte[] fileData = file.getBytes();
        try {
            log.info("----->Updating User<-------");
            Optional<Version> exists = versionRepo.findById(versionNumber);
            if(exists.isPresent()){
                entityResponse.setMessage("Document with ID "+ versionNumber + "found");
                Version version= new Version();

                version.setNotes(notes);
                version.setFileType(fileType);
                version.setDocumentName(documentName);
                version.setDepartment(department);
                version.setDocumentData(fileData);
                entityResponse.setMessage("New version Uploaded successfully");
                entityResponse.setEntity(versionRepo.save(version));
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(exists);
            }else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->DOCUMENT WITH THIS ID DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("DOCUMENT WITH THIS  ID DOES NOT EXIST");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        }catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENT BY ID <--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }
    public  EntityResponse getVersions( Long id){
        EntityResponse entityResponse = new EntityResponse<>();
        try {

            List<Version> exists = versionRepo.findByDocumentModelIdOrderByIdDesc(id);
            log.info("----->FETCHING VERSIONS<-------");
            if(exists != null){
                List<versionDTO> versionDTOs = mapEntitiesToDTOs(exists);
                log.info("----->FETCHING VERSIONS<-------");
                entityResponse.setMessage("Document with the Document ID : "+ id + "found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(versionDTOs);
                log.info("----->FETCHING VERSIONS<-------");

                // Log the action of fetching document versions
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String loggedInUsername = authentication.getName();
                documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Fetched_the_versions_of_this_document, null);
            }else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("-->DOCUMENT WITH THIS  ID DOES NOT EXIST <-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("DOCUMENT WITH THIS  ID DOES NOT EXIST");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        }catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENT WITH THIS  ID<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }
    public EntityResponse  getAllVersions(){
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING VERSIONS<-------");
            List<Version> documentList = versionRepo.findAll();
            if (documentList.isEmpty()) {
                log.warn("Database is empty");
                entityResponse.setMessage("There are no versions to display");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                // Map entities to DTOs
                List<versionDTO> versionDTOs = mapEntitiesToDTOs(documentList);

                log.info("Versions found");
                entityResponse.setMessage("Versions found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(versionDTOs);
            }
        } catch (Exception exception) {
            log.error("Error while fetching versions", exception);
            entityResponse.setMessage("Failed to fetch versions: " + exception.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        return entityResponse;
    }
    private List<versionDTO> mapEntitiesToDTOs(List<Version> versions) {
        return versions.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }
    private versionDTO mapEntityToDTO(Version version) {
        versionDTO dto = new versionDTO();
        dto.setDocumentName(version.getDocumentName());
        dto.setFileType(version.getFileType());
        dto.setVersionNumber(version.getVersionNumber());
        dto.setDateUploaded(version.getDateUploaded());
        dto.setCreatedBy(version.getCreatedBy());
        return dto;
    }
    public EntityResponse getVersionByNumber(int versionNumber, Long documentId){
        EntityResponse entityResponse= new EntityResponse<>();
         try{
               Version version= versionRepo.findByVersionNumber(versionNumber,documentId);
             log.info("Document with version number found");
                if(version != null){

                    log.info("Version successfully converted to DTO");
                    entityResponse.setMessage("Version found");
                    entityResponse.setStatusCode(HttpStatus.OK.value());
                    entityResponse.setEntity(version);
                }
                else {
                    log.info("Document not found");
                    entityResponse.setMessage("Version not found");
                    entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                }
         } catch (Exception exception) {
             log.error("Error while fetching version", exception);
             entityResponse.setMessage("Failed to fetch versions: " + exception.getMessage());
         }   entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return entityResponse;
    }
    public EntityResponse  deleteversion(int versionNumber, Long documentId) {
        EntityResponse<DocumentModel> entityResponse = new EntityResponse<>();

        log.info("Deleting version with ID: {}",versionNumber );
        Version version= versionRepo.findByVersionNumber(versionNumber,documentId);



            String documentName = version.getDocumentName();

            // Get the email address of the currently logged-in user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();

            // Log the action for deleting the document
            documentLogService.logDocumentAction(loggedInUsername, DocumentLogType.Deleted_the_document, version.getDocumentModel());
            log.info("Document '{}' (ID: {}) deleted successfully", version.getDocumentName(), version.getId());
            versionRepo.delete(version);

            entityResponse.setMessage(String.format("%s deleted %s successfully", loggedInUsername, documentName));
            entityResponse.setStatusCode(HttpStatus.OK.value());

        return entityResponse;
    }



}