package com.emt.dms1.notifications;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationModel, Long> {
    List<NotificationModel> findByRecipientOrderByIdDesc(String recipient);

//    @Query(value = "SELECT DISTINCT recipient FROM notifications WHERE document_id = :documentId AND type = :type", nativeQuery = true)
//    List<String> findByDocumentIdAndType(@Param("documentId") long documentId, @Param("type") String type);
    @Query(value = "SELECT DISTINCT n.recipient, CONCAT(u.first_name, ' ', u.last_name) AS full_name " +
            "FROM notifications n " +
            "JOIN users u ON n.recipient = u.email_address " +
            "WHERE n.document_id = :documentId AND n.type = :type", nativeQuery = true)
    List<String> findByDocumentIdAndType(@Param("documentId") long documentId, @Param("type") String type);

//    @Query("SELECT n FROM NotificationModel n WHERE n.recipient = :recipient AND n.status = :status AND n.timestamp &gt;= :startDate AND n.timestamp &lt; :endDate ORDER BY n.id DESC")
//    List<NotificationModel> findByRecipientAndStatusWithinPeriod(String recipient, NotificationStatus status, LocalDateTime startDate, LocalDateTime endDate);

}
