package com.emt.dms1.versioning;




import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "versions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "document_name")
    private String documentName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name="fileSize")
    private String fileSize;

    @Lob
    @Column(name = "document_data", length = 1000000)
    private byte[] documentData;

    @Column(name = "version_number")
    private int versionNumber;

    private String notes;

    private String department;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "date_uploaded")
    @CreationTimestamp
    private LocalDate dateUploaded;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "document_id")
    private DocumentModel documentModel;

    // Constructors
    public Version(String documentName, String fileType, byte[] documentData) {
        this.documentName = documentName;
        this.fileType = fileType;
        this.documentData = documentData;
    }

//    public Version(String documentName, String notes, String approverComments, String department,
//                   LocalDateTime dateUploaded, LocalDate dueDate, String createdBy) {
//        this.documentName = documentName;
//        this.notes = notes;
//        this.approverComments = approverComments;
//        this.department = department;
//        this.dateUploaded = dateUploaded;
//        this.dueDate = dueDate;
//        this.createdBy = createdBy;
//    }
}