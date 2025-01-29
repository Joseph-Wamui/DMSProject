package com.emt.dms1.testOCR;

import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="KeyValuePairs")
public class KeyValuePairs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "kvp_attributes", joinColumns = @JoinColumn(name = "kvp_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentModel document;
}

