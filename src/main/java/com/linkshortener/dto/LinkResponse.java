package com.linkshortener.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LinkResponse {
    private String shortCode;
    private String shortUrl;
    private String originalUrl;
    private Long clickCount;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private Boolean active;
}
