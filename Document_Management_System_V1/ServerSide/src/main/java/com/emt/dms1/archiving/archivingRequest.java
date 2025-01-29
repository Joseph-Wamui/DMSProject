package com.emt.dms1.archiving;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class archivingRequest {

    int days;
    int  retentionPeriod;
     String archiveLocation;
}
