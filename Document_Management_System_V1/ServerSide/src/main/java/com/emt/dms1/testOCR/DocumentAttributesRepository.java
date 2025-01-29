package com.emt.dms1.testOCR;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository

public interface DocumentAttributesRepository extends JpaRepository<DocumentAttributes, Long> {
    List<DocumentAttributes> findByDocumentId(Long documentId);
}
