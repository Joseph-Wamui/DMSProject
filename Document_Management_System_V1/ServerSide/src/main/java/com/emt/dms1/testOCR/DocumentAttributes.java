package com.emt.dms1.testOCR;


import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Document attributes")
public class DocumentAttributes  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long id;

    private String taxpayerName;
    private String idNumber;
    private String date;
    private String degreeName;
    private String issuingInstitution;
    private String issueDate;
    private String pin;
    private String email;
    private String poBox;
    private String postalCode;
    private String name;
    private String serialNumber;
    private String invoiceNumber;
    private String invoiceDate;
    private String totalAmount;
    private String permitNumber;
    private String expiryDate;

    @Column(name = "document_id") // Map the foreign key column
    private Long documentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "document_id", insertable = false, updatable = false)
    private DocumentModel documentModel;

}

