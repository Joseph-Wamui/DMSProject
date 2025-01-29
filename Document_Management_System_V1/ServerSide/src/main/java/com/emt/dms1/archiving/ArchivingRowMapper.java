package com.emt.dms1.archiving;



import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.workFlow.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
public class ArchivingRowMapper implements RowMapper<Archiving> {

    @Override
    public Archiving mapRow(ResultSet rs, int rowNum) throws SQLException {
        Archiving archiving = new Archiving();

        // Map all fields from ResultSet to Archiving entity
        archiving.setArchivingid(rs.getLong("archiving_id"));
        archiving.setDocumentName(rs.getString("document_name"));
        archiving.setFileType(rs.getString("file_type"));
        archiving.setDocumentData(rs.getBytes("document_data"));
        archiving.setNotes(rs.getString("notes"));
        archiving.setDepartment(rs.getString("department"));
        archiving.setCreatedBy(rs.getString("created_by"));
        archiving.setArchiveDate(rs.getDate("archiving_date").toLocalDate());
        archiving.setDateUploaded(rs.getDate("date_uploaded").toLocalDate());
        archiving.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
        archiving.setDocumentDeleteFlag(rs.getString("document_delete_flag").charAt(0));
        archiving.setFileSize(rs.getString("file_size"));
        archiving.setFileLocation(rs.getString("file_location"));
        archiving.setStatus(Status.valueOf(rs.getString("status"))); // Assuming the status is stored as a string
        archiving.setArchiveLocation(rs.getString("archiving_location"));
        archiving.setFilePath(rs.getString("file_path"));

        // DocumentModel is fetched separately since it's a related entity
        Long documentId = rs.getLong("document_id");
        if (documentId != null) {
            DocumentModel documentModel = new DocumentModel();
            documentModel.setId(documentId);
            // Map other fields for documentModel if needed

            archiving.setDocumentModel(documentModel);
        } else {
            log.warn("DocumentModel is null for archiving_id: {}", archiving.getArchivingid());
        }

        return archiving;
    }
}

