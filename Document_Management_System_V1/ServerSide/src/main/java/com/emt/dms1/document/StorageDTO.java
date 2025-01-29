package com.emt.dms1.document;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table
public class StorageDTO {
    private Long id;
    private String department;
    private String fileType;
    private String fileSize;
}
