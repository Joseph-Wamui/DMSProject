package com.emt.dms1.backup;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BackupJob implements Job {

    @Autowired
    private BackupService backupService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Define the backup location, possibly from configuration or environment
        String backupLocation = "/path/to/backup/location";
//        backupService.performBackup(backupLocation);
    }
}
