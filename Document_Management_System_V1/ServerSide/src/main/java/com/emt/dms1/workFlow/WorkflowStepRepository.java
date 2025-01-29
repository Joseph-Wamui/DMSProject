package com.emt.dms1.workFlow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository

public interface WorkflowStepRepository  extends JpaRepository<ApprovalWorkflow, Long > {
//    @Query("SELECT d FROM DocumentModel d WHERE d.id = :workflowId AND d. type ='Predefined'")
//    Optional<ApprovalWorkflow> findByIdAndStatus(Long workflowId);
    List<ApprovalWorkflow> findByAssigner(String assigner);
    @Query(value = "select * FROM workflows WHERE created_by= :createdBy  AND type= :Predefined", nativeQuery = true)
     List<ApprovalWorkflow> findPredefinedFlows(@Param("createdBy")String createdBy, @Param("Predefined") String type);


    Optional<ApprovalWorkflow> findByDocumentModelId(Long documentId);
}
