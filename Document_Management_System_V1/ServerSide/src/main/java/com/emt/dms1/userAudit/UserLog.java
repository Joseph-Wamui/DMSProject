package com.emt.dms1.userAudit;

import com.emt.dms1.user.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "UserLog")
public class UserLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userLogId;

    private LocalDateTime timestamp;
    private String userName;

    @Enumerated(EnumType.STRING)
    private UserLogType userLogType;

    @ManyToOne
    @JoinColumn(name = "userid")
    private User user;
}



