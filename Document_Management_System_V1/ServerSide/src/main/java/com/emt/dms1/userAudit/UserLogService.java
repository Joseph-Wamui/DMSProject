package com.emt.dms1.userAudit;

import com.emt.dms1.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserLogService {
    @Autowired
    private UserLogRepository userLogRepository;

    public void logUserAction(String userName, UserLogType actionType, User user) {
        UserLog userLog = new UserLog();

        userLog.setTimestamp(LocalDateTime.now());
        userLog.setUserName(userName);
        userLog.setUserLogType(actionType);
        userLog.setUser(user);
        // Set other relevant fields

        userLogRepository.save(userLog);
    }

    public List<UserLog> getAuditTrailForUser(Long Id) {
        return userLogRepository.findByUserIdOrderByTimestampAsc(Id);
    }

    public List<UserLog> getAllAuditTrails() {
        return  userLogRepository.findAll();
    }
}

