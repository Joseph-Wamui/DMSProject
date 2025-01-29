package com.emt.dms1.versioning;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class versionDTO {
    private String documentName;
    private String fileType;
    private int versionNumber;
    private LocalDate dateUploaded;
    private String createdBy;
}
