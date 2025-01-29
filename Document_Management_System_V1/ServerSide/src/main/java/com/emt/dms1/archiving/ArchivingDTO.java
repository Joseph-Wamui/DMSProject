package com.emt.dms1.archiving;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArchivingDTO {
    private Long id;
    private String documentName;
    private String department;
    private String createdBy;
    private Long retentionPeriodInMinutes;
    private String FileType;
    private byte[] DocumentData;

}
