package com.emt.dms1.customReports;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class CustomReportRepository {

    @Autowired
    private EntityManager entityManager;

    public List<String> getColumnNames(String tableName) {
        String sql = "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = :tableName " +
                "AND TABLE_SCHEMA = 'dmsproject' " +
                "AND COLUMN_NAME NOT IN ('password', 'document_data')";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("tableName", tableName);

        return query.getResultList();
    }

    public List<Object[]> customReportData(String tableName, String dateName, List<String> columnNames, LocalDate startDate, LocalDate endDate) {

        String columns = String.join(", ", columnNames);
        String sql = "SELECT " + columns + " FROM " + tableName;

        // If dateName is provided, add the WHERE clause
        if (dateName != null && !dateName.isEmpty()) {
            sql += " WHERE " + dateName + " BETWEEN :startDate AND :endDate";
        }

        Query query = entityManager.createNativeQuery(sql);

        // Only set date parameters if they are part of the query
        if (dateName != null && !dateName.isEmpty()) {
            query.setParameter("startDate", startDate);
            query.setParameter("endDate", endDate);
        }

        try {
            return query.getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to execute query", e);
        }
    }


}