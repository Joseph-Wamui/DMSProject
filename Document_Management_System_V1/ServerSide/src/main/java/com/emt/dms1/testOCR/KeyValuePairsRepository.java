package com.emt.dms1.testOCR;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KeyValuePairsRepository extends JpaRepository<KeyValuePairs, Long> {

    List<KeyValuePairs> findByDocumentId(Long documentId);
}
