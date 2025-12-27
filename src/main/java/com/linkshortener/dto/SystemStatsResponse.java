package com.linkshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemStatsResponse {
    private Long totalLinks;
    private Long totalClicks;
    private Long activeLinks;
}
