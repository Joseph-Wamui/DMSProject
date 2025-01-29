package com.emt.dms1.customReports;

import com.emt.dms1.utils.EntityResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/custom", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
public class CustomReportController {

    @Autowired
    private CustomReportService customReportService;

    @GetMapping("/data")
    public EntityResponse dataR(@RequestParam String tableId) {
        log.info("tableid: "+ tableId);
        return customReportService.getColumnNames(tableId);
    }

    @GetMapping("/customData")
    public EntityResponse customR(@RequestParam String tableId,
                                  @RequestParam List<String> columns,
                                  @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                  @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate){
        return customReportService.customReportData(tableId, columns, startDate, endDate);
    }
}