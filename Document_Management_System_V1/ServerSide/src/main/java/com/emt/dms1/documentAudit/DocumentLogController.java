package com.emt.dms1.documentAudit;

import com.emt.dms1.utils.EntityResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping ( value = "/api/v1/Audit_trail" , produces = MediaType.APPLICATION_JSON_VALUE)
public class DocumentLogController {
    @Autowired
    private DocumentLogService documentLogService;


    @GetMapping("/documentAuditLogs/documentId")
    public EntityResponse<List<Map<String, Object>>> getDocumentLogs(
            @RequestParam String documentId) {
        long parsedId = Long.parseLong(documentId);
        return documentLogService.getDocumentLogs(parsedId);
    }
}


