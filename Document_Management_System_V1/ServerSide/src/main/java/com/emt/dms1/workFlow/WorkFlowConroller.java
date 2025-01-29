package com.emt.dms1.workFlow;



import com.emt.dms1.utils.EntityResponse;
import com.emt.dms1.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/v1/workflow" , produces = MediaType.APPLICATION_JSON_VALUE)
@CrossOrigin
public class WorkFlowConroller {
    @Autowired
    private WorkflowService service;


    @PostMapping("/progressTracker")
    public EntityResponse progressTracker(@RequestParam Long workflowId){
        return  service.progressTracker(workflowId);
    }
    @GetMapping("/showMyProgress")
    public EntityResponse listTracker(){
        return  service.listProgressForWorkflows();
    }

    @PostMapping("/send-to-next-step")
    public EntityResponse sendToNextStep(@RequestParam String documentId,
                                        @RequestParam boolean approved,
                                        @RequestParam String approverComments) {
        Long parsedDocumentId = Long.parseLong(documentId);
//        boolean approved = request.isApproved();
        boolean rejected = !approved;
//        List<String>  approvecomments= request.getApproverComments();
//        Map<String, Comments> comments = generateSystemComments(approverComments);

        return service.processApproval(parsedDocumentId, approved, rejected,approverComments);
    }
    @PostMapping("/AddNewWorkflow")
    public EntityResponse createWorkflow(
            @RequestParam Long documentId,
            @RequestParam List<String> approvers,
            @RequestParam boolean saveAsPredefined,
            @RequestParam String approverComments,
            @RequestParam String name) {

        // System-defined comments generation
//        Map<String, Comments> approverComments = generateSystemComments(approverCommentKeys)
        log.info(SecurityUtils.getCurrentUserLogin()+"Starting new workflow");
        return service.assignUserDefinedApprovers(documentId, approvers, saveAsPredefined, approverComments,name);
    }
    @PostMapping("/SelectExistingWorkflow")
    public EntityResponse  selectworkflow(@RequestParam Long workflowId,@RequestParam Long documentId, @RequestParam String approverComments
                                          ){
        log.info(SecurityUtils.getCurrentUserLogin()+"Starting Saved workflow");
        return  service.assignPredefinedApprovers(workflowId,documentId, approverComments);
    }

    @PutMapping("/update-workflow-approvers")
    public EntityResponse updateWorkflowApprovers(@RequestParam Long workflowId, @RequestParam List<String> approversToAdd, @RequestParam List<String> approversToRemove) {
      return service.updateWorkflow(workflowId,approversToRemove,approversToAdd);
    }

    @GetMapping("/ViewExistingWorkflows")
    public  EntityResponse viewWorkflow(){
        return service.fetchAllworkflows();
    }
    @GetMapping("/MyWorkflows")
    public  EntityResponse  viewMyworkflows(){
        return service.fetchMyFlows();
    }
    @GetMapping("/fetchUsers")
    public  EntityResponse  viewworkflowusers(@RequestParam String documentid){
        long docid= Long.parseLong(documentid);
        return service.fetchusers(docid);
    }
  @PostMapping("/share")
  public  EntityResponse  shareDoc( @RequestParam String userMail, @RequestParam String documentID){

        Long documentId = Long.parseLong(documentID);

       return service.shareNotification(userMail,documentId);
  }

  @GetMapping("/approverscomments")
  public EntityResponse findApproversComments(@RequestParam String documentID){
        long documentId= Long.parseLong(documentID);
        return service.listApproversComments(documentId);
  }

    @DeleteMapping("/deletWorkflow")
    public EntityResponse deleteworkflow(@RequestParam Long workflowId){
        return  service.deleteWorkflow(workflowId);
    }

//    public Map<String, Comments> generateSystemComments(List<String> keys) {
//        Map<String, Comments> systemComments = new HashMap<>();
//
//        if (keys == null || keys.isEmpty()) {
//            return systemComments; // Return an empty map if keys are null or empty
//        }
//
//        String email = SecurityUtils.getCurrentUserLogin();
//        if (email == null || email.isEmpty()) {
//            throw new IllegalStateException("Current user email is not available");
//        }
//
//        for (String key : keys) {
//            Comments comment = generateSystemValueForKey();
//            systemComments.put(key, comment);
//        }
//
//        return systemComments;
//    }
//
//    private Comments generateSystemValueForKey() {
//        String email = SecurityUtils.getCurrentUserLogin();
//        LocalDateTime timestamp = LocalDateTime.now();
//        return new Comments(email, timestamp);
//    }

}
