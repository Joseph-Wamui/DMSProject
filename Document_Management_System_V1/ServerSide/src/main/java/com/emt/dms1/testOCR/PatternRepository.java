package com.emt.dms1.testOCR;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatternRepository  extends JpaRepository <PatternsRegex,Long> {

    @Query(value = "SELECT * from pattern where name=?1", nativeQuery = true)
    PatternsRegex findByName(String attributeName);
}
