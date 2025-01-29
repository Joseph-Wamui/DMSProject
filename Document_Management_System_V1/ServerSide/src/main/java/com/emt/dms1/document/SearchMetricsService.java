//package com.emt.dms1.Document;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//@Slf4j
//public class SearchMetricsService {
//
//    @Autowired
//    private DocumentSearchService documentSearchService;
//
//    @Autowired
//    private SearchMetricsRepo searchMetricsRepo;
//    @Scheduled(fixedRate = 60000) // Schedule every minute
//    public void updateSearchMetrics() {
//        log.info("Updating search metrics...");
//        long totalResponseTime = documentSearchService.getResponseTime();
//        int totalSearches = documentSearchService.getTotalSearches();
//
//        double avgResponseTime = calculateAverageResponseTime(totalResponseTime, totalSearches);
//        double searchThroughput = calculateSearchThroughput(totalSearches);
//
//        log.debug("Total Response Time: {}", totalResponseTime);
//        log.debug("Total Searches: {}", totalSearches);
//        log.debug("Average Response Time: {}", avgResponseTime);
//        log.debug("Search Throughput: {}", searchThroughput);
//
//
//        // Create a new SearchMetrics object, set its attributes, and save it to the database
//        SearchMetrics searchMetrics = new SearchMetrics();
//        searchMetrics.setTotalResponseTime(totalResponseTime);
//        searchMetrics.setTotalSearches(totalSearches);
//        searchMetrics.setAverageResponseTime(avgResponseTime);
//        searchMetrics.setSearchThroughput((long) searchThroughput);
//        searchMetricsRepo.save(searchMetrics);
//
//        // Reset in-memory counters in SearchService
//        documentSearchService.resetResponseTime();
//        documentSearchService.resetTotalSearches();
//    }
//
//    // Method to calculate average response time
//    private double calculateAverageResponseTime(long totalResponseTime, int totalSearches) {
//        if (totalSearches == 0) {
//            return 0;
//        }
//        return (double) totalResponseTime / totalSearches;
//    }
//
//    // Method to calculate search throughput
//    private double calculateSearchThroughput(int totalSearches) {
//        return totalSearches / (60.0); // Throughput per minute
//    }
//}
