package com.emt.dms1.workFlow;



import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name="workflows")
public class ApprovalWorkflow implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private List<String> approvers;
    private String assigner;
    private Integer currentApproverIndex;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private  LocalDateTime setNextStepAt;
    private String totalDuration;
    private List<String> stepDurations ;
    private String createdBy;


    @Enumerated
    private Type type;
    @ManyToOne
    @JoinColumn(name="document_id")
    private DocumentModel documentModel;





}