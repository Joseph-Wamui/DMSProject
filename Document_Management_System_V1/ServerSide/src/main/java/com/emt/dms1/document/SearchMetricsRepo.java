package com.emt.dms1.document;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface SearchMetricsRepo extends JpaRepository<SearchMetrics, Long> {
}
