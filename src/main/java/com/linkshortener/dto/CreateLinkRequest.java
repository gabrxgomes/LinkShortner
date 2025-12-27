package com.linkshortener.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLinkRequest {

    @NotBlank(message = "URL is required")
    @Size(max = 2048, message = "URL is too long")
    private String url;

    private Integer expirationHours; // Optional, defaults to 24
}
