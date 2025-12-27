package com.linkshortener.service;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

@Service
public class UrlValidationService {

    @Value("${app.max-url-length:2048}")
    private int maxUrlLength;

    @Value("${app.blocked-domains:localhost,127.0.0.1,0.0.0.0}")
    private String blockedDomainsStr;

    private static final List<String> DANGEROUS_SCHEMES = Arrays.asList(
        "javascript", "data", "file", "vbscript"
    );

    public void validateUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        // Check length
        if (url.length() > maxUrlLength) {
            throw new IllegalArgumentException("URL exceeds maximum length of " + maxUrlLength);
        }

        // Validate URL format
        String[] schemes = {"http", "https"};
        UrlValidator apacheUrlValidator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);

        if (!apacheUrlValidator.isValid(url)) {
            throw new IllegalArgumentException("Invalid URL format");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            // Check for dangerous schemes
            if (scheme != null && DANGEROUS_SCHEMES.contains(scheme.toLowerCase())) {
                throw new IllegalArgumentException("URL scheme not allowed: " + scheme);
            }

            // Only allow HTTP and HTTPS
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new IllegalArgumentException("Only HTTP and HTTPS protocols are allowed");
            }

            // Check blocked domains
            List<String> blockedDomains = Arrays.asList(blockedDomainsStr.split(","));
            if (host != null && blockedDomains.stream().anyMatch(blocked -> host.toLowerCase().contains(blocked.toLowerCase()))) {
                throw new IllegalArgumentException("Domain is blocked: " + host);
            }

            // Prevent private IP ranges (additional security)
            if (host != null) {
                if (host.matches("^(10\\.|172\\.(1[6-9]|2[0-9]|3[01])\\.|192\\.168\\.).*")) {
                    throw new IllegalArgumentException("Private IP addresses are not allowed");
                }
            }

        } catch (Exception e) {
            if (e instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) e;
            }
            throw new IllegalArgumentException("Invalid URL: " + e.getMessage());
        }
    }

    public String sanitizeUrl(String url) {
        // Remove whitespace
        url = url.trim();

        // Basic XSS prevention - remove any HTML tags
        url = url.replaceAll("<[^>]*>", "");

        return url;
    }
}
