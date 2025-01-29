package com.emt.dms1.archiving;


import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.workFlow.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;


@Entity
@Table(name = "Archiving")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Archiving{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long archivingid;

    @Column(name = "document_name")
    private String documentName;
    @Column(name = "file_type")
    private String fileType;

    @Lob
    @Column(name = "document_data", length = 1000000)
    private byte[] documentData;



    private String notes;
    private Character DocumentDeleteFlag;
    private String FilePath;


    @Enumerated(EnumType.STRING)
    private Status status;

    private String department;

    @Column(name = "created_by")
    private String createdBy;
    @Version
    private Long version;

    @Column(name = "date_uploaded")

    private LocalDate dateUploaded;

    @Column(name = "retention_period_in_minutes")
    private Long retentionPeriodInMinutes;
    private boolean deleted = false;


    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="document_id")
    private DocumentModel documentModel;

    @Column
    private String FileSize;
    @Column
    private String FileLocation;
    @Column
    private String ArchiveLocation;

    @Column(
            nullable = false,
            updatable = false)
    private LocalDate ArchiveDate;






}
