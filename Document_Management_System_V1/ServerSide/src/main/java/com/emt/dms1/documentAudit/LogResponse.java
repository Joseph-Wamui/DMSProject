package com.emt.dms1.documentAudit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogResponse {
    private LocalDateTime timestamp;
    private DocumentLogType documentLogType;
}