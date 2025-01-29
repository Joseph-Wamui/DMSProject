package com.emt.dms1.document;

import lombok.Data;

@Data
public class ShareDocumentRequest {
    private Long documentId;
    private String emailAddress;
}