package com.emt.dms1.archiving;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class ArchivingJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(ArchivingJob.class.getName());

    private final ArchivingService archivingService;

    @Autowired
    public ArchivingJob(ArchivingService archivingService) {
        this.archivingService = archivingService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Archiving job triggered...");

        try {
            // Retrieve parameters from the job context
            String archiveLocation = context.getJobDetail().getJobDataMap().getString("archiveLocation");
            String ipAddress = context.getJobDetail().getJobDataMap().getString("ipAddress");
            String port = context.getJobDetail().getJobDataMap().getString("port");
            String dataBaseName = context.getJobDetail().getJobDataMap().getString("dataBaseName");
            String databaseUsername = context.getJobDetail().getJobDataMap().getString("databaseUsername");
            String databasePassword = context.getJobDetail().getJobDataMap().getString("databasePassword");
            String isRemote = context.getJobDetail().getJobDataMap().getString("isRemote");

            // Perform the archiving process using the retrieved parameters
            archivingService.performArchiving(
                    archiveLocation,
                    ipAddress,
                    port,
                    dataBaseName,
                    databaseUsername,
                    databasePassword,
                    isRemote
            );

            LOGGER.info("Archiving process completed successfully.");
        } catch (Exception e) {
            LOGGER.severe("Archiving process failed: " + e.getMessage());
            throw new JobExecutionException(e);
        }
    }
}
