package com.emt.dms1.document;


import com.emt.dms1.workFlow.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "Documents")

public class DocumentModel {

    @Id
    @Column(name="Document_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "documentName")
    private String documentName;

    @Column(name = "notes")
    private String notes;

    @Column(name = "department")
    private String department;

    @Column(name = "dueDate")
    private LocalDate dueDate;

    @Column(name = "createdBy")
    private String createdBy;
    @Lob
    @Column(name = "documentData", length = 1000000)
    private byte[] documentData;

    @Column(name="fileType")
    private String fileType;

    @Column(name="fileLocation")
    private String fileLocation;


    @Column(name="fileSize")
    private String fileSize;

    @Column(name="result")
    private String result;

    @Column(name="filepath")
    private String filepath;

    @ElementCollection
    @CollectionTable(name = "document_tags", joinColumns = @JoinColumn(name = "document_id"))
    @Column(name = "tag")
    private List<String> tags;

    @CreatedDate
    @Column(
            nullable = false,
            updatable = false)
    private LocalDate createDate;

    @LastModifiedBy
    @Column(
            insertable = false
    )
    private  String lastModifiedBy;

    @LastModifiedDate
    @Column(insertable = false)
    private LocalDateTime lastModified;

    @Enumerated(EnumType.STRING)
    private Status status;
    //
    @Column(name="localbackupstatus", nullable = false)
    private boolean backedUpLocally;
    @Column(name="remotebackupstatus", nullable = false)
    private boolean backedUpRemotely;
    @Column
    private String localBackupLocation;
    @Column
    private String remoteBackupLocation;

    @Column(name="localarchivestatus", nullable = false)
    private boolean archivedLocally;
    @Column(name="remotearchivestatus", nullable = false)
    private boolean archivedRemotely;
    @Column
    private String localArchiveLocation;
    @Column
    private String remoteArchiveLocation;

    @Column
    private Character documentDeleteFlag;

    @Column
    private String deletedBy;

    @Column
    private LocalDateTime deletedOn;


    @PrePersist
    public void prePersist() {
        this.backedUpLocally = false;
        this.backedUpRemotely = false;
        this.archivedLocally = false;
        this.archivedRemotely = false;
    }

    //    @OneToMany(mappedBy = "document")
//    private List<Version> versions;
//

    public DocumentModel(String documentName, String fileType, byte[] documentData) {
        this.documentName = documentName;
        this.fileType = fileType;
        this.documentData = documentData;
    }


    public boolean isSuccessful() {
        return false;
    }





    public LocalDateTime getLastModifiedDate() {
        return lastModified; }

}
