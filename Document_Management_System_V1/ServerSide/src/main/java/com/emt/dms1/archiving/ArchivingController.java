package com.emt.dms1.archiving;

import com.emt.dms1.utils.EntityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "/api/v1/archiving", produces = MediaType.APPLICATION_JSON_VALUE)
public class ArchivingController {

    private final ArchivingService archivingService;

    public ArchivingController(ArchivingService archivingService) {
        this.archivingService = archivingService;
    }



    // Endpoint to schedule archiving
    @PostMapping("/schedule-archiving")
    public EntityResponse scheduleArchiving(@RequestParam int days) {
        return archivingService.scheduleArchiving(days);
    }
    @PostMapping("/schedule-RETENTION")
    public EntityResponse scheduleDocumentRetention(@RequestParam int Days) {
        return archivingService.scheduleDocumentRetention(Days);


    }
    // Endpoint to retrieve all archived documents
        @GetMapping("/all-archives")
        public EntityResponse getAllArchivedDocuments()  {
        return archivingService.getAllArchivedDocuments();}
        @PostMapping("/archive-now")
         public EntityResponse performArchiving(
                    @RequestParam (required = false) String archiveLocation,
                    @RequestParam (required = false) String ipAddress,
                    @RequestParam (required = false) String port,
                    @RequestParam (required = false) String dataBaseName,
                    @RequestParam (required = false) String databaseUsername,
                    @RequestParam (required = false) String databasePassword,
                    @RequestParam (required = false) String isRemote) {

                try {
                    return archivingService.performArchiving(
                            archiveLocation,
                            ipAddress,
                            port,
                            dataBaseName,
                            databaseUsername,
                            databasePassword,
                            isRemote);
                } catch (IOException e) {
                    EntityResponse entityResponse = new EntityResponse<>();
                    entityResponse.setMessage("Archiving failed: " + e.getMessage());
                    entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    return entityResponse;
                }
            }
        }


