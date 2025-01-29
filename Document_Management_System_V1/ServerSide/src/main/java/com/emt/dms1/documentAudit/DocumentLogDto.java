package com.emt.dms1.documentAudit;

import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentLogDto {

    private LocalDateTime timestamp;
    private String userName;
    @Enumerated(EnumType.STRING)
    private DocumentLogType documentLogType;
    private Long documentId;
    //private String documentName;
    //private DocumentModel document;

    public void setDocument(String documentName) {
    }
}
