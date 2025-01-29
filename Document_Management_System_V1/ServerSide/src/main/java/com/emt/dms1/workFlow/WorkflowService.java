package com.emt.dms1.workFlow;



import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.document.DocumentRepository;
import com.emt.dms1.documentAudit.DocumentLogService;
import com.emt.dms1.documentAudit.DocumentLogType;
import com.emt.dms1.emails.EmailNotificationService;
import com.emt.dms1.notifications.NotificationModel;
import com.emt.dms1.notifications.NotificationService;
import com.emt.dms1.notifications.NotificationStatus;
import com.emt.dms1.notifications.notificationType;
import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WorkflowService {
    @Autowired
    private DocumentLogService documentLogService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private WorkflowStepRepository workflowStepRepository;
    @Autowired
    private EmailNotificationService emailNotificationService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private  commentsRepository commentsRepository;



    public EntityResponse assignPredefinedApprovers(Long workflowId, Long documentId, String approverComments) {
        EntityResponse<ApprovalWorkflow> entityResponse = new EntityResponse<>(); // Use diamond operator for type inference

        try {
            // Fetch workflow and document concurrently
//            CompletableFuture<Optional<ApprovalWorkflow>> workflowFuture = CompletableFuture.supplyAsync(() ->
//                    workflowStepRepository.findById(workflowId));
//            CompletableFuture<Optional<DocumentModel>> documentFuture = CompletableFuture.supplyAsync(() ->
//                    documentRepository.findById(documentId));
//
//            // Wait for both futures to complete
//            CompletableFuture.allOf(workflowFuture, documentFuture).join();
//
//            Optional<ApprovalWorkflow> workflow = workflowFuture.join();
//            Optional<DocumentModel> documentModel = documentFuture.join();
            Optional<ApprovalWorkflow> workflow= workflowStepRepository.findById(workflowId);
            Optional<DocumentModel> documentModel= documentRepository.findById(documentId);

            if (workflow.isEmpty()) {
                entityResponse.setMessage("Workflow not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                return entityResponse;
            }

            if (documentModel.isEmpty()) {
                entityResponse.setMessage("Document not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                return entityResponse;
            }

            ApprovalWorkflow aWorkflow = workflow.get();
            DocumentModel document = documentModel.get();

            // Step 2: Implement logic to determine predefined approvers based on business rules
            List<String> predefinedApprovers = aWorkflow.getApprovers();
            ApprovalWorkflow approvalWorkflow= new ApprovalWorkflow();
            approvalWorkflow.setName(aWorkflow.getName());
            approvalWorkflow.setAssigner(SecurityUtils.getCurrentUserLogin());
            approvalWorkflow.setDocumentModel(document);
            approvalWorkflow.setApprovers(predefinedApprovers);
            approvalWorkflow.setCurrentApproverIndex(0);
            approvalWorkflow.setType(Type.PREDEFINED);
            approvalWorkflow.setStartTime(LocalDateTime.now());
            approvalWorkflow.setCreatedBy(aWorkflow.getCreatedBy());
            document.setStatus(Status.PENDING);
            workflowStepRepository.save(approvalWorkflow);

            String assignerEmail=SecurityUtils.getCurrentUserLogin();
            LocalDateTime timestamp = LocalDateTime.now();

            ApproversComments approversComments1= new ApproversComments();
            approversComments1.setComment(approverComments);
            approversComments1.setDocument(document);
            approversComments1.setUser(assignerEmail);
            approversComments1.setTimestamp(timestamp);
            commentsRepository.save(approversComments1);

            String firstApprover = predefinedApprovers.get(0);

            String notificationMessage2 = "Please review the workflow awaiting your approval.'" + approvalWorkflow.getDocumentModel().getDocumentName() + "' by " + assignerEmail + ". Preview the document here: " ;
            NotificationModel notification2 = new NotificationModel(notificationMessage2, firstApprover, NotificationStatus.UNREAD, timestamp,approvalWorkflow.getDocumentModel().getId(), notificationType.NORMAL);
            notificationService.createNotification2(notification2);

            entityResponse.setMessage("Predefined approvers assigned successfully");
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(approvalWorkflow); // Return the updated document

        } catch (Exception e) {
            // Handle any exceptions that occur during the process
            entityResponse.setMessage("Failed to assign predefined approvers: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }


    public EntityResponse assignUserDefinedApprovers(Long documentId, List<String> approvers, boolean saveAsPredefined,String approversComments, String name) {
        EntityResponse<ApprovalWorkflow> entityResponse = new EntityResponse<>(); // Use diamond operator for type inference

        try {
            // Step 1: Retrieve the document from the database
            Optional<DocumentModel> optionalDocument = documentRepository.findById(documentId);
            if (optionalDocument.isPresent()) {
                DocumentModel document = optionalDocument.get();

                ApprovalWorkflow approvalWorkflow = new ApprovalWorkflow();
                approvalWorkflow.setAssigner(SecurityUtils.getCurrentUserLogin());
                approvalWorkflow.setCreatedBy(SecurityUtils.getCurrentUserLogin());
                approvalWorkflow.setName(name);
                approvalWorkflow.setApprovers(approvers);
                approvalWorkflow.setCurrentApproverIndex(0);
                approvalWorkflow.setDocumentModel(document);
                approvalWorkflow.setType(saveAsPredefined ? Type.PREDEFINED : Type.USERDEFINED);
                approvalWorkflow.setStartTime(LocalDateTime.now());
                document.setStatus(Status.PENDING);


                workflowStepRepository.save(approvalWorkflow);
                String notificationMessage = "Your Workflow has been started for:'" + approvalWorkflow.getDocumentModel().getDocumentName();
                NotificationModel notification = new NotificationModel(notificationMessage, SecurityUtils.getCurrentUserLogin(), NotificationStatus.UNREAD, LocalDateTime.now(),approvalWorkflow.getDocumentModel().getId(), notificationType.NORMAL);
                notificationService.createNotification2(notification);

                entityResponse.setMessage("Approvers assigned successfully" + (saveAsPredefined ? ", workflow has been saved" : ""));
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(approvalWorkflow); // Return the updated document
                // Assuming you want to return the first approver
                String firstApprover = approvers.get(0);
                String assignerEmail=SecurityUtils.getCurrentUserLogin();
                String notificationMessage2 = "Please review the process awaiting your approval.'" + approvalWorkflow.getDocumentModel().getDocumentName() + "' by " + assignerEmail + ". Preview here: " ;
                NotificationModel notification2 = new NotificationModel(notificationMessage2, firstApprover, NotificationStatus.UNREAD, LocalDateTime.now(),approvalWorkflow.getDocumentModel().getId(), notificationType.NORMAL);
                notificationService.createNotification2(notification2);

                String assigner = SecurityUtils.getCurrentUserLogin();

                documentLogService.logDocumentAction(assigner, DocumentLogType.STARTED_WORKFLOW, document);

                LocalDateTime timestamp = LocalDateTime.now();

                ApproversComments approversComments1= new ApproversComments();
                approversComments1.setComment(approversComments);
                approversComments1.setDocument(document);
                approversComments1.setUser(assigner);
                approversComments1.setTimestamp(timestamp);
                commentsRepository.save(approversComments1);
                log.info("Saving assigner comments for workflow");

            } else {
                // Document with the given ID not found
                entityResponse.setMessage("Document not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            }
        } catch (Exception e) {
            // Handle any exceptions that occur during the proces
            entityResponse.setMessage("Failed to assign approvers: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }


    private void deleteWorkflow2(Long workflowId) {
        workflowStepRepository.deleteById(workflowId);
    }

    public EntityResponse processApproval(Long documentId, boolean approved, boolean rejected,  String comment) {
        EntityResponse<ApprovalWorkflow> entityResponse = new EntityResponse<>();
        var currentUserEmail = SecurityUtils.getCurrentUserLogin();

        try {
            Optional<ApprovalWorkflow> optionalWorkflow = workflowStepRepository.findByDocumentModelId(documentId);
            if (optionalWorkflow.isEmpty()) {
                entityResponse.setMessage("Workflow not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                return entityResponse;
            }

            ApprovalWorkflow workflow = optionalWorkflow.get();
            List<String> approvers = workflow.getApprovers(); // Assuming approvers is a list of strings representing emails
            int currentUserIndex = approvers.indexOf(currentUserEmail);
            int workflowIndex=workflow.getCurrentApproverIndex();

            if (workflow.getStepDurations() == null) {
                workflow.setStepDurations(new ArrayList<>());
            }


            if (currentUserIndex == -1 || currentUserIndex > approvers.size() || currentUserIndex== approvers.size()){
                entityResponse.setMessage("Bad Request: User not found in approvers list");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            if(currentUserIndex<workflowIndex){
                entityResponse.setMessage("Bad Request: User cannot approve or reject as user's step has been done");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            if( currentUserIndex > workflowIndex ){
                entityResponse.setMessage("Bad Request: User cannot approve or reject as user's step has not reached");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }
            if (workflow.getDocumentModel().getStatus() == Status.APPROVED || workflow.getDocumentModel().getStatus() == Status.REJECTED){
                entityResponse.setMessage("This workflow is already finished");
                entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
                return entityResponse;
            }



            // Calculate duration for the current step if it's not the first step
            if (workflow.getCurrentApproverIndex() != 0) {
                calculateStepDuration(workflow);
            }

            if (approved) {
                handleApproval(workflow, currentUserIndex, comment);
            } else if (rejected) {
                handleRejection(workflow,comment);
            } else {
                sendToNextApprover(workflow, currentUserIndex,comment);
            }


            // Save the updated workflow
            workflowStepRepository.save(workflow);

            // Calculate total duration
            calculateTotalDuration(workflow);

            // Set response message and status code
            if (workflow.getDocumentModel().getStatus() != Status.PENDING) {
                entityResponse.setMessage("Approval processed successfully.");
            } else {
                entityResponse.setMessage("Document sent to the next approver.");
            }
            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setEntity(workflow);
        } catch (Exception e) {
            entityResponse.setMessage("Failed to process approval: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
        return entityResponse;
    }
    private void handleApproval(ApprovalWorkflow workflow, int currentUserIndex, String comments)
            throws MessagingException, GeneralSecurityException {
        if (currentUserIndex == workflow.getApprovers().size() - 1) {
            // Last approver, document is approved
            workflow.getDocumentModel().setStatus(Status.APPROVED);

             String approver = SecurityUtils.getCurrentUserLogin();
             LocalDateTime timestamp = LocalDateTime.now();

             ApproversComments approversComments = new ApproversComments();
             approversComments.setDocument(workflow.getDocumentModel());
             approversComments.setComment(comments);
             approversComments.setUser(approver);
             approversComments.setTimestamp(timestamp);
             commentsRepository.save(approversComments);
             log.info("Saving approver comments for last approver");

            // Notify the assigner via email that the document has been approved
            String assignerEmail = workflow.getAssigner();
            String currentemail = SecurityUtils.getCurrentUserLogin();

            if (assignerEmail != null) {
//                emailNotificationService.sendEmail(assignerEmail, "Document Approval", "Your document has been approved.");
                String notificationMessage = "Your workflow has been approved.'" + workflow.getDocumentModel().getDocumentName() + "' by " + currentemail + ". Preview the document here: ";
                NotificationModel notification = new NotificationModel(notificationMessage, assignerEmail, NotificationStatus.UNREAD, LocalDateTime.now(), workflow.getDocumentModel().getId(), notificationType.NORMAL);
                notificationService.createNotification2(notification);
            }
        } else {
            // Move to the next approver
            workflow.setCurrentApproverIndex(currentUserIndex + 1);
            workflow.getDocumentModel().setStatus(Status.PENDING);
            // Notify the next approver via email
            String nextApproverEmail = workflow.getApprovers().get(currentUserIndex + 1);
            String approver2 = SecurityUtils.getCurrentUserLogin();
            String assignerEmail = workflow.getAssigner();

            LocalDateTime timestamp = LocalDateTime.now();

            ApproversComments approversComments = new ApproversComments();
            approversComments.setDocument(workflow.getDocumentModel());
            approversComments.setComment(comments);
            approversComments.setUser(approver2);
            approversComments.setTimestamp(timestamp);
            commentsRepository.save(approversComments);
            log.info("Saving approver comments for other approvers");
            if (nextApproverEmail != null) {
//                emailNotificationService.sendEmail(nextApproverEmail, "Document Approval Request", "Please review the document awaiting your approval.");
//                emailNotificationService.sendEmail(assignerEmail, "Document Approval Progress", "Your Document has been sent to the next approver in the workflow");

                String notificationMessage = "Your workflow has been sent to the next approver'" + workflow.getDocumentModel().getDocumentName() + "' by " + nextApproverEmail + ". Preview the document here: ";
                NotificationModel notification = new NotificationModel(notificationMessage, assignerEmail, NotificationStatus.UNREAD, LocalDateTime.now(), workflow.getDocumentModel().getId(), notificationType.NORMAL);
                notificationService.createNotification2(notification);

                String notificationMessage2 = "Please review the workflow awaiting your approval.'" + workflow.getDocumentModel().getDocumentName() + "' by " + assignerEmail + ". Preview the document here: ";
                NotificationModel notification2 = new NotificationModel(notificationMessage2, nextApproverEmail, NotificationStatus.UNREAD, LocalDateTime.now(), workflow.getDocumentModel().getId(), notificationType.NORMAL);
                notificationService.createNotification2(notification2);

                documentLogService.logDocumentAction(nextApproverEmail, DocumentLogType.APPROVED_DOCUMENT, workflow.getDocumentModel());

            }
        }
    }
   public EntityResponse shareNotification(String userMail, long documentId) {
       EntityResponse entityResponse = new EntityResponse();
       try {
           String email = SecurityUtils.getCurrentUserLogin();
           String message = "A document has been shared with you by " +   email + " Click here to view this document";
           NotificationModel notification2 = new NotificationModel(message, userMail, NotificationStatus.UNREAD, LocalDateTime.now(), documentId,notificationType.SHARED);
           notificationService.createNotification3(notification2);

           Optional<DocumentModel> documentModel = documentRepository.findById(documentId);
           if (documentModel.isPresent()){
               DocumentModel optionalDocument =  documentModel.get();
               documentLogService.logDocumentAction(SecurityUtils.getCurrentUserLogin(), DocumentLogType.SHARED_DOCUMENT, optionalDocument);
           }

           List<String> userlist = notificationService.sharedTo(documentId);

           entityResponse.setStatusCode(HttpStatus.OK.value());
           entityResponse.setMessage("The document has been shared with " + userMail);
           entityResponse.setEntity(userlist);
       }
         catch (Exception e) {
        entityResponse.setMessage("Failed to share the Document: " + e.getMessage());
        entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

       }
       return  entityResponse;

   }



    // Method to handle rejection
    private void handleRejection(ApprovalWorkflow workflow, String comments) throws MessagingException, GeneralSecurityException {

        String approver = SecurityUtils.getCurrentUserLogin();
        LocalDateTime timestamp = LocalDateTime.now();

        // Document rejected, send it back to the assigner
        workflow.getDocumentModel().setStatus(Status.REJECTED);
        ApproversComments approversComments = new ApproversComments();
        approversComments.setDocument(workflow.getDocumentModel());
        approversComments.setComment(comments);
        approversComments.setUser(approver);
        approversComments.setTimestamp(timestamp);
        commentsRepository.save(approversComments);
        log.info("Saving rejection comments");

        // Notify the assigner via email that the document has been rejected
        String assignerEmail = workflow.getAssigner();
        String  currentemail= SecurityUtils.getCurrentUserLogin();
        if (assignerEmail != null) {

//            emailNotificationService.sendEmail(assignerEmail, "Document Rejected", "Your document has been rejected.");
            String notificationMessage = "Your workflow"+ workflow.getDocumentModel().getDocumentName() + "has been rejected by " +currentemail+ ". Preview the document here: " ;
            NotificationModel notification = new NotificationModel(notificationMessage, assignerEmail, NotificationStatus.UNREAD, LocalDateTime.now(),workflow.getDocumentModel().getId(), notificationType.NORMAL);

            notificationService.createNotification(notification);
            documentLogService.logDocumentAction(currentemail, DocumentLogType.REJECTED_DOCUMENT, workflow.getDocumentModel());

        }
    }

    // Method to send document to the next approver
    private void sendToNextApprover(ApprovalWorkflow workflow, int currentUserIndex, String comments) throws MessagingException, GeneralSecurityException {
        // Move to the next approver
        workflow.setCurrentApproverIndex(currentUserIndex + 1);
        workflow.getDocumentModel().setStatus(Status.PENDING); // Document is pending for the next approver

        String approver = SecurityUtils.getCurrentUserLogin();
        LocalDateTime timestamp = LocalDateTime.now();

        ApproversComments approversComments = new ApproversComments();
        approversComments.setDocument(workflow.getDocumentModel());
        approversComments.setTimestamp(timestamp);
        approversComments.setUser(approver);
        approversComments.setComment(comments);
        commentsRepository.save(approversComments);
        log.info("Saving comments for sending to the next step");

        // Notify the next approver via email
        String nextApproverEmail = workflow.getApprovers().get(currentUserIndex + 1);
        String assignerEmail = workflow.getAssigner();
        if (nextApproverEmail != null) {
//            emailNotificationService.sendEmail(nextApproverEmail, "Document Approval Request", "Please review the document awaiting your approval.");
//            emailNotificationService.sendEmail(assignerEmail, "Document Approval Progress", "Your Document has been sent to the next approver in the workflow");
            String notificationMessage = "Your Document has been sent to the next approver in the workflow'" + workflow.getDocumentModel().getDocumentName() + "' by " + nextApproverEmail+ ". Preview the document here: " ;
            NotificationModel notification = new NotificationModel(notificationMessage, assignerEmail, NotificationStatus.UNREAD, LocalDateTime.now(),workflow.getDocumentModel().getId(),notificationType.NORMAL);
            notificationService.createNotification2(notification);
            String notificationMessage2 = "Please review the document awaiting your approval.'" + workflow.getDocumentModel().getDocumentName() + "' by " + assignerEmail + ". Preview the document here: " ;
            NotificationModel notification2 = new NotificationModel(notificationMessage2, nextApproverEmail, NotificationStatus.UNREAD, LocalDateTime.now(),workflow.getDocumentModel().getId(),notificationType.NORMAL);
            notificationService.createNotification(notification2);

        }
    }
    private void calculateStepDuration(ApprovalWorkflow workflow) {
        LocalDateTime previousStepTime = workflow.getSetNextStepAt();
        List<String> stepDurations = workflow.getStepDurations(); // Retrieve the list of step durations

        if (previousStepTime != null) {
            LocalDateTime currentTime = LocalDateTime.now();
            Duration stepDuration = Duration.between(previousStepTime, currentTime);
            long days = stepDuration.toDays();
            long hours = stepDuration.toHours() % 24;
            long minutes = stepDuration.toMinutes() % 60;
            long seconds = stepDuration.getSeconds() % 60;

            String durationString = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);

            stepDurations.add(durationString); // Add the duration string to the list

            // Update the next step time to current time
            workflow.setSetNextStepAt(currentTime);
        } else {
            LocalDateTime startTime = workflow.getStartTime();
            LocalDateTime currentTime= LocalDateTime.now();
            System.out.println("Previous step time is null for the first approver.");
            Duration stepDuration = Duration.between(startTime, currentTime);
            long days = stepDuration.toDays();
            long hours = stepDuration.toHours() % 24;
            long minutes = stepDuration.toMinutes() % 60;
            long seconds = stepDuration.getSeconds() % 60;

            String durationString = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);
            stepDurations.add(durationString); // Add the duration string to the list
            workflow.setSetNextStepAt(currentTime);
        }
    }
    // Method to calculate total duration
    private void calculateTotalDuration(ApprovalWorkflow workflow) {
        long totalDurationSeconds = 0;
        for (String durationString : workflow.getStepDurations()) {
            String[] parts = durationString.split("[,\\s]+");
            totalDurationSeconds += Integer.parseInt(parts[0]) * 24 * 3600 + // days
                    Integer.parseInt(parts[2]) * 3600 + // hours
                    Integer.parseInt(parts[4]) * 60 + // minutes
                    Integer.parseInt(parts[6]); // seconds
        }

        long totalDays = totalDurationSeconds / (24 * 3600);
        long remainingSeconds = totalDurationSeconds % (24 * 3600);
        long totalHours = remainingSeconds / 3600;
        remainingSeconds %= 3600;
        long totalMinutes = remainingSeconds / 60;
        long totalSeconds = remainingSeconds % 60;

        String totalDurationString = String.format("%d days, %d hours, %d minutes, %d seconds", totalDays, totalHours, totalMinutes, totalSeconds);
        workflow.setTotalDuration(totalDurationString);
        workflowStepRepository.save(workflow);
    }

// Other methods remain the same



    public EntityResponse fetchAllworkflows() {
        EntityResponse entityResponse = new EntityResponse<>();

        try {
            log.info("----->LISTING WORKFLOWS<-------");
            List<ApprovalWorkflow> workflowList = workflowStepRepository.findAll().stream()
                    .distinct()
                    .collect(Collectors.toList());
            if (workflowList.isEmpty()) {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--------> DATABASE IS EMPTY <------ ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("My documents is empty");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            } else {
                //
                List<WorkflowDto> dtos = mapEntitiesToDTOs(workflowList);
                entityResponse.setMessage("Documents found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(dtos);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING MY DOCUMENTS<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }


    public EntityResponse fetchMyFlows() {
        EntityResponse entityResponse = new EntityResponse<>();
        var email = SecurityUtils.getCurrentUserLogin();
        try {
            List<ApprovalWorkflow> workflows = workflowStepRepository.findPredefinedFlows(email, String.valueOf(Type.PREDEFINED));
            log.info("----->FETCHING Workflows<-------");
            if (workflows != null) {
                List<WorkflowDto> workflowDtos = mapEntitiesToDTOs(workflows);
                log.info("----->FETCHING VERSIONS<-------");
                entityResponse.setMessage("Your workflows have been Found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(workflowDtos);
                log.info("----->FETCHING WorkFlows<-------");
            } else {
                log.warn("'''''''''''''''warning''''''''''''''");
                log.warn("--> YOU DON'T HAVE WORKFLOWS<-- ");
                log.warn(",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,");

                entityResponse.setMessage("You have not created any workflows");
                entityResponse.setStatusCode(HttpStatus.CONFLICT.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING DOCUMENT WITH THIS  ID<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse  deleteWorkflow(Long workFlowId){
        EntityResponse entityResponse = new EntityResponse();
        try{
           Optional<ApprovalWorkflow> workflow=workflowStepRepository.findById(workFlowId);
           log.info("Requested workflow has been found");


           if(workflow.isPresent()){
               workflowStepRepository.delete(workflow.get());
               entityResponse.setMessage("Workflow has been deleted");
               entityResponse.setStatusCode(HttpStatus.OK.value());
               entityResponse.setEntity(null);
           } else{
               log.error("requested workflow is missing");
               entityResponse.setMessage("Requested workflow is missing");
               entityResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
           }

        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE FETCHING WORKFLOW     WITH THIS  ID<--: " + exception.getLocalizedMessage());
            entityResponse.setMessage(HttpStatus.EXPECTATION_FAILED.getReasonPhrase() + exception.getLocalizedMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }

    public EntityResponse updateWorkflow(Long workflowId, List<String> approversToRemove, List<String> approversToAdd) {
        EntityResponse entityResponse = new EntityResponse();
        try {
            Optional<ApprovalWorkflow> optionalWorkflow = workflowStepRepository.findById(workflowId);
            if (optionalWorkflow.isPresent()) {
                ApprovalWorkflow workflow = optionalWorkflow.get();

                // Remove existing approvers
                if (approversToRemove != null && !approversToRemove.isEmpty()) {
                    workflow.getApprovers().removeAll(approversToRemove);
                }

                // Add new approvers
                if (approversToAdd != null && !approversToAdd.isEmpty()) {
                    workflow.getApprovers().addAll(approversToAdd);
                }

                // Save the updated workflow
                workflowStepRepository.save(workflow);

                // Map entity to DTO
                WorkflowDto DTO = mapEntityToDTO(workflow);

                entityResponse.setMessage("Workflow updated successfully");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(DTO);
            } else {
                entityResponse.setMessage("Workflow Not Found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                entityResponse.setEntity(null);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error("--> ERROR WHILE UPDATING WORKFLOW WITH THIS ID <--: " + exception.getMessage());
            entityResponse.setMessage(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase() + ": " + exception.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            entityResponse.setEntity(null);
        }
        return entityResponse;
    }
    public EntityResponse progressTracker(Long workflowId) {
        EntityResponse entityResponse = new EntityResponse<>();
        String currentUserEmail = SecurityUtils.getCurrentUserLogin(); // Assuming you have a method to get the current user's email

        try {
            Optional<ApprovalWorkflow> optionalWorkflow = workflowStepRepository.findById(workflowId);
            if (optionalWorkflow.isPresent()) {
                ApprovalWorkflow workflow = optionalWorkflow.get();

                // Check if the current user is the assigner
                if (!workflow.getAssigner().equals(currentUserEmail)) {
                    entityResponse.setMessage("Unauthorized access: You are not the assigner of this workflow");
                    entityResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
                    return entityResponse;
                }

                // Proceed with progress tracking logic
                List<String> approvers = workflow.getApprovers();
                int currentIndex = workflow.getCurrentApproverIndex();
                int totalApprovers = approvers.size();

                if (totalApprovers == 0) {
                    entityResponse.setMessage("Approval has not started yet");
                    entityResponse.setStatusCode(HttpStatus.PROCESSING.value());
                    return entityResponse;
                }

                double progressPercentage = (double) (currentIndex + 1) / totalApprovers * 100;
                progressPercentage = Math.round(progressPercentage * 10.0) / 10.0;
                String currentApproverEmail = approvers.get(currentIndex);

                entityResponse.setMessage(String.format("Workflow ID: %d, Progress: %.1f%%, Approver: %s", workflow.getId(), progressPercentage, currentApproverEmail));
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(progressPercentage + ", " + currentApproverEmail);

                return entityResponse;
            } else {
                entityResponse.setMessage("Workflow not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                return entityResponse;
            }
        } catch (Exception e) {
            entityResponse.setMessage("Failed to track progress: " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return entityResponse;
        }
    }
    public EntityResponse<List<Map<String, Object>>> listProgressForWorkflows() {
        EntityResponse<List<Map<String, Object>>> entityResponse = new EntityResponse<>();
        List<Map<String, Object>> workflowProgressList = new ArrayList<>();
        String currentUserEmail = SecurityUtils.getCurrentUserLogin(); // Assuming you have a method to get the current user's ema
//        log.info("email", currentUserEmail);
        // Retrieve workflows associated with the current user
        List<ApprovalWorkflow> userWorkflows = workflowStepRepository.findByAssigner(currentUserEmail);

        for (ApprovalWorkflow workflow : userWorkflows) {
            Map<String, Object> workflowProgress = new HashMap<>();
            List<String> approvers = workflow.getApprovers();
            int currentIndex = workflow.getCurrentApproverIndex();
            int totalApprovers = approvers.size();
            String documentName = workflow.getDocumentModel().getDocumentName();
            String filetype = workflow.getDocumentModel().getFileType();
            LocalDate startedOn = workflow.getDocumentModel().getCreateDate();
            Enum<Status>  statusEnum=workflow.getDocumentModel().getStatus();

            if (totalApprovers == 0) {
                // Workflow has not started yet
                workflowProgress.put("message", "Approval has not started yet");
                workflowProgress.put("statusCode", HttpStatus.PROCESSING.value());
            } else {
                double progressPercentage;
                if (currentIndex == 0){
                    progressPercentage = 0;
                }
                else {
                    progressPercentage = ((double) (currentIndex + 1) / totalApprovers) * 100; // Adding 1 to currentIndex to start from 1-based indexing
                    progressPercentage = Math.round(progressPercentage * 10.0) / 10.0;
                }
                String currentApproverEmail = approvers.get(currentIndex);

                workflowProgress.put("documentName", documentName);
                workflowProgress.put("filetype", filetype);
                workflowProgress.put("progressPercentage", progressPercentage);
                workflowProgress.put("currentApproverEmail", currentApproverEmail);
                workflowProgress.put("startedOn", startedOn);
                workflowProgress.put("Status", statusEnum);

            }
            workflowProgressList.add(workflowProgress);
        }
        entityResponse.setMessage("WORKFLOWS HAVE BEEN FOUND");
        entityResponse.setStatusCode(HttpStatus.OK.value());
        entityResponse.setEntity(workflowProgressList);
        return entityResponse;
    }

    private List<WorkflowDto> mapEntitiesToDTOs(List<ApprovalWorkflow> workflow) {
        return workflow.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());
    }

    private WorkflowDto mapEntityToDTO(ApprovalWorkflow workflow) {
        WorkflowDto dto = new WorkflowDto();
        dto.setId(workflow.getId());
        dto.setApprovers(workflow.getApprovers());
        dto.setAssigner(workflow.getAssigner());
        dto.setType(workflow.getType());
        dto.setStepDurations(workflow.getStepDurations());
        dto.setTotalDuration(workflow.getTotalDuration());
        dto.setStepDurations(workflow.getStepDurations());
        dto.setDocumentName(workflow.getDocumentModel().getDocumentName());
        dto.setName(workflow.getName());
        return dto;
    }

    public EntityResponse fetchusers(long docid) {
        EntityResponse entityResponse = new EntityResponse<>();
        try {
            Optional<ApprovalWorkflow> object = workflowStepRepository.findByDocumentModelId(docid);

            if (object.isEmpty()) {
                entityResponse.setMessage("Workflow not found");
                entityResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
            } else {
                ApprovalWorkflow workflow = object.get();
                List<String> approvers = workflow.getApprovers();

                entityResponse.setMessage("Workflow users found");
                entityResponse.setStatusCode(HttpStatus.OK.value());
                entityResponse.setEntity(approvers);
            }
        } catch (Exception e) {
            entityResponse.setMessage("Failed to fetch users " + e.getMessage());
            entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

        }
        return entityResponse;
    }

    public EntityResponse listApproversComments(long documentId) {
        EntityResponse entityResponse = new EntityResponse<>();
    try{
        List<ApproversComments> comments= commentsRepository.findByDocumentId(documentId);
        if(comments.isEmpty()){
            entityResponse.setMessage("No Comments for this  document");
            entityResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
        }else{
            List<Map<String, Object>> commentsList = new ArrayList<>();

            for(ApproversComments approversComments: comments){
                Map<String, Object> commentsMap = new HashMap<>();
                commentsMap.put("Comment", approversComments.getComment());
                commentsMap.put("User", approversComments.getUser());
                commentsMap.put("TimeStamp", approversComments.getTimestamp());

                commentsList.add(commentsMap);
            }

            entityResponse.setStatusCode(HttpStatus.OK.value());
            entityResponse.setMessage("Comments for this document found");
            entityResponse.setEntity(commentsList); // Assuming EntityResponse has a setData method

        }
    }catch (Exception e){
        entityResponse.setMessage("Failed to fetch users " + e.getMessage());
        entityResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());

    }
    return entityResponse;
    }

}















