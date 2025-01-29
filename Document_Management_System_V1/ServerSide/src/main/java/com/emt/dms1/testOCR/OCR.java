package com.emt.dms1.testOCR;


import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class OCR {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  int resultId;

     @Column(name="Result")
    private String resuLt;
     @ElementCollection
    @ManyToOne
    @JoinColumn(name = "Document_id", referencedColumnName = "Document_id")
    private DocumentModel Documentmodel;


}
