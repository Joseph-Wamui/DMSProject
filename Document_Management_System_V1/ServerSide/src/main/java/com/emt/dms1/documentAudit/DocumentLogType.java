package com.emt.dms1.documentAudit;

public enum DocumentLogType {
    Uploaded_document,
    Uploaded_multiple_documents,
    Downloaded_the_document,
    Deleted_the_document,
    Accessed_the_document,
    Added_new_version_of_the_document,
    Fetched_the_versions_of_this_document,
    SHARED_DOCUMENT,
    STARTED_WORKFLOW,
    APPROVED_DOCUMENT,
    REJECTED_DOCUMENT,
    Added_document_metadata,
    Added_tags,
    Added_notes,
    Added_approver_comments,
    Added_document_name,
    Added_duedate

}
