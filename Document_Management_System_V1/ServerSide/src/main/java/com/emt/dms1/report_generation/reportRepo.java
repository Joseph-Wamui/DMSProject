package com.emt.dms1.report_generation;

import com.emt.dms1.document.DocumentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface reportRepo extends JpaRepository<DocumentModel,Long> {

//    @Query(value = "select * from documents",nativeQuery = true)
//
//    List<Object[]> getAll();

//    List<Object[]> getAllUsers();
    @Query(value = "select * from versions",nativeQuery = true)

    List<Object[]> getVersionsByDateRange(LocalDate startDate, LocalDate endDate);

    //List<Object[]> getAllVersions();
    @Query(value = "select * from documents",nativeQuery = true)


    List<Object[]> getDocumentsByDateRange(LocalDate startDate, LocalDate endDate);
    //@Query(value = "SELECT u FROM UserModel u WHERE u.createdOn BETWEEN :startDate AND :endDate",nativeQuery = true)
@Query(value = "select * from users", nativeQuery = true)
    List<Object[]> getUsersByDateRange(LocalDate startDate, LocalDate endDate);

@Query(value = "SELECT * FROM users WHERE deleted_flag = 'Y'", nativeQuery = true)
List<Object[]> getDeletedUsersByDateRange(LocalDate startDate, LocalDate endDate);

@Query(value = " SELECT document_id, document_log_type, user_name, timestamp\n" +
        "FROM document_log\n" +
        "WHERE timestamp BETWEEN :startDate AND :endDate\n" +
        "ORDER BY document_id;\n",nativeQuery = true)

    List<Object[]> getDocumentLogsByDateRange(LocalDate startDate, LocalDate endDate);
@Query(value = "select * from documents",nativeQuery = true)

    List<Object[]> getStorageByDateRange(LocalDate startDate, LocalDate endDate);
@Query(value = "select * from search_metrics",nativeQuery = true)

    List<Object[]> getSearchMetricsByDateRange(LocalDate startDate, LocalDate endDate);
@Query(value = "select * from user_log",nativeQuery = true)

List<Object[]> getUserLogsByDateRange(LocalDate startDate, LocalDate endDate);
    @Query(value = "select * from workflows",nativeQuery = true)
    List<Object[]> getWorkflowsByDateRange(LocalDate startDate,LocalDate endDate);
    @Query(value = "select * from archiving", nativeQuery = true)

    List<Object[]> getArchivingByDateRange(LocalDate startDate, LocalDate endDate);
    @Query(value = "select * from backup",nativeQuery = true)

    List<Object[]> getBackUpByDateRange(LocalDate startDate, LocalDate endDate);
}
