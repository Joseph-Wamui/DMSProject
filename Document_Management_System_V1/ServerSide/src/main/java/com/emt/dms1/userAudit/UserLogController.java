package com.emt.dms1.userAudit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping (value = "/api/v1/User_Audit_trail" , produces = MediaType.APPLICATION_JSON_VALUE)

public class UserLogController {
    @Autowired
    private UserLogService userLogService;

    @GetMapping("/{Id}/audit-trail")
    public ResponseEntity<List<UserLog>> getAuditTrail(@PathVariable Long Id) {
        List<UserLog> auditTrail = userLogService.getAuditTrailForUser(Id);
        return ResponseEntity.ok(auditTrail);
    }
    @GetMapping("/audit-trails")
    public ResponseEntity<List<UserLog>> getAllAuditTrails() {
        List<UserLog> auditTrails = userLogService.getAllAuditTrails();
        return ResponseEntity.ok(auditTrails);
    }
}
