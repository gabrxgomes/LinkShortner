package com.linkshortener.service;

import com.linkshortener.dto.CreateLinkRequest;
import com.linkshortener.dto.LinkResponse;
import com.linkshortener.dto.SystemStatsResponse;
import com.linkshortener.model.Link;
import com.linkshortener.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {

    private final LinkRepository linkRepository;
    private final UrlValidationService urlValidator;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.link-expiration-hours:24}")
    private int defaultExpirationHours;

    @Value("${app.short-code-length:6}")
    private int shortCodeLength;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public LinkResponse createShortLink(CreateLinkRequest request) {
        // Sanitize and validate URL
        String sanitizedUrl = urlValidator.sanitizeUrl(request.getUrl());
        urlValidator.validateUrl(sanitizedUrl);

        // Generate unique short code
        String shortCode = generateUniqueShortCode();

        // Create link entity
        Link link = new Link();
        link.setShortCode(shortCode);
        link.setOriginalUrl(sanitizedUrl);
        link.setCreatedAt(LocalDateTime.now());

        // Set expiration
        int expirationHours = request.getExpirationHours() != null
            ? request.getExpirationHours()
            : defaultExpirationHours;

        link.setExpiresAt(link.getCreatedAt().plusHours(expirationHours));
        link.setClickCount(0L);
        link.setActive(true);

        // Save to database
        link = linkRepository.save(link);

        log.info("Created short link: {} -> {}", shortCode, sanitizedUrl);

        return buildLinkResponse(link);
    }

    @Transactional
    public Optional<String> getOriginalUrl(String shortCode) {
        Optional<Link> linkOpt = linkRepository.findByShortCodeAndActiveTrue(shortCode);

        if (linkOpt.isEmpty()) {
            log.warn("Short code not found or inactive: {}", shortCode);
            return Optional.empty();
        }

        Link link = linkOpt.get();

        // Check if expired
        if (link.isExpired()) {
            link.setActive(false);
            linkRepository.save(link);
            log.info("Link expired: {}", shortCode);
            return Optional.empty();
        }

        // Increment click count
        link.incrementClickCount();
        linkRepository.save(link);

        log.info("Redirecting {} to {} (clicks: {})", shortCode, link.getOriginalUrl(), link.getClickCount());

        return Optional.of(link.getOriginalUrl());
    }

    @Transactional(readOnly = true)
    public Optional<LinkResponse> getLinkStats(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
            .map(this::buildLinkResponse);
    }

    private String generateUniqueShortCode() {
        String shortCode;
        int attempts = 0;
        int maxAttempts = 10;

        do {
            shortCode = generateRandomString(shortCodeLength);
            attempts++;

            if (attempts >= maxAttempts) {
                // Increase length if too many collisions
                shortCode = generateRandomString(shortCodeLength + 1);
                break;
            }
        } while (linkRepository.existsByShortCode(shortCode));

        return shortCode;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public SystemStatsResponse getSystemStats() {
        long totalLinks = linkRepository.countTotalLinks();
        Long totalClicks = linkRepository.sumTotalClicks();
        long activeLinks = linkRepository.countActiveLinks(LocalDateTime.now());

        return SystemStatsResponse.builder()
            .totalLinks(totalLinks)
            .totalClicks(totalClicks != null ? totalClicks : 0L)
            .activeLinks(activeLinks)
            .build();
    }

    private LinkResponse buildLinkResponse(Link link) {
        return LinkResponse.builder()
            .shortCode(link.getShortCode())
            .shortUrl(baseUrl + "/" + link.getShortCode())
            .originalUrl(link.getOriginalUrl())
            .clickCount(link.getClickCount())
            .createdAt(link.getCreatedAt())
            .expiresAt(link.getExpiresAt())
            .active(link.getActive() && !link.isExpired())
            .build();
    }
}
