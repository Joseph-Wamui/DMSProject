package com.emt.dms1.documentAudit;

import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "DocumentLog")
public class DocumentLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;
    private String userName;

    @Enumerated(EnumType.STRING)
    private DocumentLogType documentLogType;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentModel document;
}
