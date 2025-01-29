package com.emt.dms1.backup;

import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

// ...
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/backup", produces = MediaType.APPLICATION_JSON_VALUE)
public class BackupController {

    private final BackupService backupService;

    @Autowired
    public BackupController(BackupService backupService) {
        this.backupService = backupService;
    }



    @PostMapping("/schedulebackup")
    public EntityResponse setBackupFrequency(@RequestParam int minutes, @RequestParam String backupLocation) {
        return backupService.scheduleBackup( minutes, backupLocation);
    }

    @PostMapping("/restore")
    public EntityResponse restoreExistingDocuments() {
        return backupService.restoreExistingDocuments();
    }

    @PostMapping("/backupnow")
    public EntityResponse performBackup(@RequestParam(required = false) String backupLocation,
                                        @RequestParam(required = false) String ipAddress,
                                        @RequestParam(required = false) String port,
                                        @RequestParam(required = false) String dataBaseName,
                                        @RequestParam(required = false) String databaseUsername,
                                        @RequestParam(required = false) String databasePassword,
                                        @RequestParam(required = false) String isRemote) throws IOException {
        log.info("backupsettings:" +backupLocation+ipAddress+port+dataBaseName+databaseUsername+databasePassword);
        return backupService.performBackup(backupLocation, ipAddress, port, dataBaseName, databaseUsername, databasePassword, isRemote);
    }
    @GetMapping("/backedupdocuments")
    public EntityResponse getAllBackedUpDocuments() throws IOException {
        return backupService.getAllBackedUpDocuments();
    }

}
