package com.crs.iamservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsResponse {

    private long totalUsers;
    private long totalCustomers;
    private long newCustomers;
    private long totalDrivers;
}
