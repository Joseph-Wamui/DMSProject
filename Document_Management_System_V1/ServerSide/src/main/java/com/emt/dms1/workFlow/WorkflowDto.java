package com.emt.dms1.workFlow;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkflowDto {

    private Long id;
    private String name;
    private List<String> approvers;
    private String assigner;
    private Type type;
    private String documentName;
    private List<String> stepDurations ;
    private String totalDuration;
}
