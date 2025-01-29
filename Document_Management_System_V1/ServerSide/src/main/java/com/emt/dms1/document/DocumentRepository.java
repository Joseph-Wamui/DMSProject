package com.emt.dms1.document;

import com.emt.dms1.workFlow.Status;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<DocumentModel, Long> {
    List<DocumentModel> findByDocumentName(String documentName);

    //    Optional<Backup> findByDocumentModelId(Long documentId);
    @Query("SELECT d FROM DocumentModel d WHERE LOWER(d.documentName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<DocumentModel> findByDocumentNameIgnoreCase(@Param("searchTerm") String searchTerm);

    List<DocumentModel> findByNotesContainingIgnoreCase(String notes);

    List<DocumentModel> findByCreateDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT DISTINCT d.fileType FROM DocumentModel d")
    List<String> findDistinctFileTypes();


    List<DocumentModel> findByCreatedBy(String createdBy);

    //    List<DocumentModel> findByBackedUp((false);
    List<DocumentModel> findByDepartment(String department);

//    @Query("SELECT d FROM DocumentModel d WHERE LOWER(d.approverComments) LIKE LOWER(concat('%', :approverComments, '%'))")
//    List<DocumentModel> findByApproverCommentsContainingIgnoreCase(@Param("approverComments") String approverComments);

    List<DocumentModel> findByDueDateBetween(LocalDate startDate, LocalDate endDate);

    List<DocumentModel> findAll(Specification<DocumentModel> spec);

    List<DocumentModel> findByCreatedByOrderByIdDesc(String email);

    @Query("SELECT DISTINCT d.department FROM DocumentModel d")
    List<String> findDistinctDepartments();

    List<DocumentModel> findByFileTypeIgnoreCase(String fileType);
    @Query("SELECT d.createDate, COUNT(d) FROM DocumentModel d GROUP BY d.createDate")
    List<Object[]> countDocumentsByCreateDate();

    List<DocumentModel> findByDocumentNameContainingIgnoreCase(String documentNamePattern);

    List<DocumentModel> findByCreatedByAndDocumentDeleteFlagOrderByIdDesc(String email, char n);

    List<DocumentModel> findByDocumentDeleteFlagOrderByIdDesc(char y);

    Collection<Object> findByCreatedByAndDocumentDeleteFlag(String email, char n);

    Collection<Object> findByCreatedByAndStatusAndDocumentDeleteFlag(String email, Status status, char n);

    Collection<Object> findByDocumentDeleteFlag(char n);
}


