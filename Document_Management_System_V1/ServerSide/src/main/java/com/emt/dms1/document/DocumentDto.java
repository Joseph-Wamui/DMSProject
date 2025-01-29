package com.emt.dms1.document;


import com.emt.dms1.workFlow.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentDto {

    private Long documentId;
    private String documentName;
    private String notes;
    private String department;
    private LocalDate dueDate;
    private String createdBy;
    private String fileType;
    private String fileSize;
    private String tags;
    private LocalDate createDate;
    private Status status;
    private boolean backedUp;

}
