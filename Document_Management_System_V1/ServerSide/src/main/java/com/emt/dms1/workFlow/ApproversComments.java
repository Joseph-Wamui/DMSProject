package com.emt.dms1.workFlow;

import com.emt.dms1.document.DocumentModel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name ="Comments")
public class ApproversComments {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "comments")
    private String comment;
    @Column(name = "user")
    private String user;
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    @ManyToOne
    @JoinColumn(name = "document_id")
    private DocumentModel document;

}