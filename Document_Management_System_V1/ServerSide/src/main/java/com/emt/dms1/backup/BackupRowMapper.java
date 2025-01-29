package com.emt.dms1.backup;

import com.emt.dms1.document.DocumentModel;
import com.emt.dms1.workFlow.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
@Slf4j
public class BackupRowMapper implements RowMapper<Backup> {
    @Override
    public Backup mapRow(ResultSet rs, int rowNum) throws SQLException {
        Backup backup = new Backup();

        // Map all fields from ResultSet to Backup entity
        backup.setBackup_id(rs.getLong("backup_id"));
        backup.setDocumentName(rs.getString("document_name"));
        backup.setFileType(rs.getString("file_type"));
        backup.setDocumentData(rs.getBytes("document_data"));
        backup.setNotes(rs.getString("notes"));
        backup.setDepartment(rs.getString("department"));
        backup.setCreatedBy(rs.getString("created_by"));
        backup.setBackUpDate(rs.getDate("back_up_date").toLocalDate());
        backup.setDateUploaded(rs.getDate("date_uploaded").toLocalDate());
        backup.setDueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null);
        backup.setDocumentDeleteFlag(rs.getString("document_delete_flag").charAt(0));
        backup.setFileSize(rs.getString("file_size"));
        backup.setFileLocation(rs.getString("file_location"));
        backup.setStatus(Status.valueOf(rs.getString("status"))); // Assuming the status is stored as a string
        backup.setBackupLocation(rs.getString("backup_location"));
        backup.setFilePath(rs.getString("file_path"));

        // DocumentModel is fetched separately since it's a related entity
        Long documentId = rs.getLong("document_id");
        if (documentId != null) {
            DocumentModel documentModel = new DocumentModel();
            documentModel.setId(documentId);
            // Map other fields for documentModel if needed

            backup.setDocumentModel(documentModel);
        } else {
            log.warn("DocumentModel is null for backup_id: {}", backup.getBackup_id());
        }

        return backup;
    }
}
