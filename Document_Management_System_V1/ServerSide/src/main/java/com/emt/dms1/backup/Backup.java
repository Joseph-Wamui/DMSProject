package com.emt.dms1.backup;


import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.workFlow.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@Entity
@Table(name = "backup")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Backup   {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Backup_id;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "file_type")
    private String fileType;

    @Lob
    @Column(name = "document_data", length = 1000000)
    private byte[] documentData;


    private String notes;

    private String department;

    @Column(name = "created_by")
    private String createdBy;

    @Column(
            nullable = false,
            updatable = false)
    private LocalDate backUpDate;

    @Column(name = "date_uploaded")
    @CreationTimestamp
    private LocalDate dateUploaded;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name="document_id")
    private DocumentModel documentModel;
    @Column
    private String Tags;
    @Column
    private Character documentDeleteFlag;
    @Column
     private String FileSize;
    @Column
     private String FileLocation;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Column
    private String BackupLocation;
    private String FilePath;




    // Constructors
}