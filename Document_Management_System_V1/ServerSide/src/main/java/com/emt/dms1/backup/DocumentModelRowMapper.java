package com.emt.dms1.backup;

import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.workFlow.Status;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DocumentModelRowMapper implements RowMapper<DocumentModel> {
    @Override
    public DocumentModel mapRow(ResultSet rs, int rowNum) throws SQLException {
        DocumentModel document = new DocumentModel();
        document.setId(rs.getLong("Document_id")); // Ensure column name matches the database schema
        document.setDocumentName(rs.getString("documentName"));
        document.setNotes(rs.getString("notes"));
        document.setDepartment(rs.getString("department"));
        document.setDueDate(rs.getDate("dueDate") != null ? rs.getDate("dueDate").toLocalDate() : null);
        document.setCreatedBy(rs.getString("createdBy"));
        document.setDocumentData(rs.getBytes("documentData"));
        document.setFileType(rs.getString("fileType"));
        document.setFileLocation(rs.getString("fileLocation"));
        document.setFileSize(rs.getString("fileSize"));
        document.setResult(rs.getString("result"));
        document.setFilepath(rs.getString("filepath"));
        // Assuming tags are stored in a separate table or you handle them separately
        // document.setTags(...); // Implement tag retrieval if needed
        document.setCreateDate(rs.getDate("createDate") != null ? rs.getDate("createDate").toLocalDate() : null);
        document.setLastModifiedBy(rs.getString("lastModifiedBy"));
        document.setLastModified(rs.getTimestamp("lastModified") != null ? rs.getTimestamp("lastModified").toLocalDateTime() : null);
        document.setStatus(rs.getString("status") != null ? Status.valueOf(rs.getString("status")) : null);
        document.setBackedUpLocally(rs.getBoolean("backupstatus"));
        document.setArchivedLocally(rs.getBoolean("archivedstatus"));
        document.setDocumentDeleteFlag(rs.getString("documentDeleteFlag") != null ? rs.getString("documentDeleteFlag").charAt(0) : null);
        document.setDeletedBy(rs.getString("deletedBy"));
        document.setDeletedOn(rs.getTimestamp("deletedOn") != null ? rs.getTimestamp("deletedOn").toLocalDateTime() : null);

        return document;
    }
}
