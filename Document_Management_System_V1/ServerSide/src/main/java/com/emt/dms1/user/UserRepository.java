package com.emt.dms1.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository <User, Integer> {
    Optional<User> findByEmailAddress(String emailAddress);
    Optional<User> getUserByEmployeeNumber(String employeeNumber);

    void deleteByEmployeeNumber(String employeeNumber);

    List<User> findByEmployeeNumber(String employeeNumber);

    @Query("SELECT u FROM User u WHERE u.deletedFlag = 'N'")
    List<User> findAllActiveUsers();

    List<User> findByDeletedFlagOrderByIdDesc(char n);

    List<User> findByDeletedFlagOrderByIdAsc(char n);
}