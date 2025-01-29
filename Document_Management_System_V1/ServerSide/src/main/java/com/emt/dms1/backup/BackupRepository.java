package com.emt.dms1.backup;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BackupRepository extends JpaRepository<Backup, Long> {

//    @Query("SELECT b FROM Backup b WHERE b.isArchived = false")
    List<Backup> findAll();

    Optional<Backup> findByDocumentModelId(Long documentModelId);
}
