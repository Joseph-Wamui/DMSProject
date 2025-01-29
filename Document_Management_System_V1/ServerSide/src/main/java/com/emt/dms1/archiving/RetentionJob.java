package com.emt.dms1.archiving;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class RetentionJob implements Job {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetentionJob.class);

    @Autowired
    private ArchivingService archivingService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("Starting document retention process...");

        try {
            // Call the service method to mark documents for logical deletion
            archivingService.markDocumentsForDeletion();

            LOGGER.info("Document retention process completed successfully.");
        } catch (Exception e) {
            LOGGER.error("Failed to perform document retention: {}", e.getMessage());
            throw new JobExecutionException("Failed to perform document retention", e);
        }
    }
}
