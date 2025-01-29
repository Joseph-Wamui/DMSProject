package com.emt.dms1.document;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "search_metrics")


public class SearchMetrics {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(
            nullable = false,
            updatable = false)

   private LocalDateTime timestamp;
    private Long totalResponseTime;
    private int totalSearches;
    private double averageResponseTime;
    private double searchThroughput;
    //private Long QueryParameters;
    private   double zeroRateResults;
    private double zeroRateResultsPercentage;

    public void setZeroRateResultPercentage(double zeroRateResultsPercentage) {
    }
    // private String sessionId; // Optional
   // private String userFeedback; // Optional



    }



