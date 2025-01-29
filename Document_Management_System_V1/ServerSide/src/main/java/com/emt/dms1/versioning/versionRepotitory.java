package com.emt.dms1.versioning;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface versionRepotitory extends JpaRepository <Version,Integer> {
    List<Version> findByDocumentModelId(Long documentModelId);
    @Query("SELECT MAX(v.versionNumber) FROM Version v WHERE v.documentModel.id = :documentId")
    Integer findLastVersionNumberByDocumentModelId(Long documentId);
    @Query(value = "SELECT * FROM versions WHERE version_number = :versionNumber AND document_id = :documentId", nativeQuery = true)
    Version findByVersionNumber(@Param("versionNumber") int versionNumber, @Param("documentId") long documentId);

    List<Version> findByDocumentModelIdOrderByIdDesc(Long id);
}
