package com.emt.dms1.userAudit;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLogDto {
    private Long Id;
    private LocalDateTime timestamp;
    private String userName;

    @Enumerated(EnumType.STRING)
    private UserLogType userLogType;
    private String emailAddress;
    private String employeeNumber;
    private String department;



}
