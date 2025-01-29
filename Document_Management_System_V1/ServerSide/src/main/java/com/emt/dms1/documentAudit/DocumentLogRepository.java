package com.emt.dms1.documentAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DocumentLogRepository extends JpaRepository<DocumentLog, Long> {
    List<DocumentLog> findByDocumentId(Long documentId);

    @Query("SELECT dl FROM DocumentLog dl WHERE dl.timestamp BETWEEN :startDate AND :endDate")
    List<DocumentLog> findAuditTrailsByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    @Query(value = "SELECT * FROM document_log WHERE user_name = :userName ORDER BY timestamp DESC;", nativeQuery = true)
    List<DocumentLog> findByUserName(@Param("userName") String userName);
    List<DocumentLog> findByDocumentIdOrderByTimestampDesc(Long documentId);
    @Query(value = "SELECT timestamp, user_name ,document_log_type ,document_id FROM document_log", nativeQuery = true)
    List<Object[]> findDocumentLogs();
}