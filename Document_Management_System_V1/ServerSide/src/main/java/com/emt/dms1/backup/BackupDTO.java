package com.emt.dms1.backup;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class BackupDTO {
    private String documentName;
   private Long BackupId;
    private LocalDate backUpDate;
    private String department;
    private String createdBy;
    private String fileType;
    private byte[] BackupData;
}