package com.emt.dms1.workFlow;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WorkflowRequest {
    private Long documentId;
    private boolean approved;
    private List<String> approverComments;

}
