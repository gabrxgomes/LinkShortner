package com.linkshortener.scheduler;

import com.linkshortener.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LinkCleanupScheduler {

    private final LinkRepository linkRepository;

    // Run every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredLinks() {
        LocalDateTime now = LocalDateTime.now();
        int deactivatedCount = linkRepository.deactivateExpiredLinks(now);

        if (deactivatedCount > 0) {
            log.info("Deactivated {} expired links", deactivatedCount);
        }
    }
}
