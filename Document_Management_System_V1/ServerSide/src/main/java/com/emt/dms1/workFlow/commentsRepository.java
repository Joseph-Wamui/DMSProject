package com.emt.dms1.workFlow;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface commentsRepository  extends JpaRepository<ApproversComments, Long> {

    List<ApproversComments> findByDocumentId(Long documentId);

}

