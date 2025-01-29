package com.emt.dms1.userAudit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLogRepository extends JpaRepository<UserLog, Long> {
    List<UserLog> findByUserIdOrderByTimestampAsc(Long userId);
}

