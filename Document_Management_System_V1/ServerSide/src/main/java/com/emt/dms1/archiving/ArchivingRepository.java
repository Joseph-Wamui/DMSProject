package com.emt.dms1.archiving;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArchivingRepository extends JpaRepository <Archiving, Long> {
    List<Archiving> findByDeleted(boolean deleted);


}
//    List<Archiving> findDocumentsNeedingRetentionUpdate(LocalDate maxExpirationDate);
//
////List<Archiving> findByAttributes(DocumentModel documentAttributes);