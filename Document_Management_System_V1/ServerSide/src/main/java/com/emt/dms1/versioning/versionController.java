package com.emt.dms1.versioning;



import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
//@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/Versions" , produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
@Slf4j
public class versionController {
    private  versionService versionService;

    public versionController(versionService versionService) {
        this.versionService = versionService;
    }


    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse addVersion(@RequestParam("documentId") Long documentId,
                                     @RequestPart MultipartFile file,
                                     @RequestParam String documentName

    ) {
        try {
            return versionService.createNewVersion(file,  documentId, documentName);
        } catch (IOException exception) {
            log.error("Error while adding new version: ", exception);
            EntityResponse entityResponse = new EntityResponse();
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + exception.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return entityResponse;
        }
    }
    @PutMapping(value = "/updateExisting version", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public EntityResponse updateExistingVersion(@RequestPart MultipartFile file ,
                                                @RequestBody String department,
                                                @RequestBody long documentId,
                                                @RequestBody String fileType,
                                                @RequestBody String notes,
                                                @RequestBody Integer versionNumber,
                                                @RequestParam String fileSize,
                                                @RequestParam List<String> approverscomments
    ) throws IOException {
        return versionService.UpdateExistingVersion(file,department,documentId,approverscomments,
                fileType,notes,versionNumber,fileSize);
    }

    @GetMapping("/getAllVersionsOfADocument")
    public EntityResponse getAllversionsoFaDoc(@RequestParam Long id){
        return versionService.getVersions(id);
    }

    @GetMapping("/getAllVersions")
    public EntityResponse getAllVersions(){
        return versionService.getAllVersions();
    }

    @GetMapping("/getAVersion")
    public EntityResponse getVersionByDocument (@RequestParam Integer versionNumber,
                                                @RequestParam Long documentId){
        return  versionService.getVersionByNumber(versionNumber,documentId);
    }
    @DeleteMapping("/deleteVersion")
    public EntityResponse<DocumentModel> deleteDocument(@RequestParam Integer versionNumber,
                                                        @RequestParam String documentId) {
        Long parsedId = Long.parseLong(documentId);
        return versionService.deleteversion(versionNumber,parsedId);
    }
}
