package com.emt.dms1.notifications;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notifications")
public class NotificationModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime timestamp;
    @Column(nullable = false)
    private  Long documentId;

    @Enumerated(EnumType.STRING)
    private  notificationType type;

    public NotificationModel(String message, String recipient, NotificationStatus status, LocalDateTime timestamp,Long documentId,notificationType type) {
        this.message = message;
        this.recipient = recipient;
        this.status = status;
        this.timestamp = timestamp;
        this.documentId=documentId;
        this.type=type;
    }

}
