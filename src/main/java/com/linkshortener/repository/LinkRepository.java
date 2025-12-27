package com.linkshortener.repository;

import com.linkshortener.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortCode(String shortCode);

    Optional<Link> findByShortCodeAndActiveTrue(String shortCode);

    boolean existsByShortCode(String shortCode);

    @Query("SELECT l FROM Link l WHERE l.expiresAt < :now AND l.active = true")
    List<Link> findExpiredLinks(LocalDateTime now);

    @Modifying
    @Query("UPDATE Link l SET l.active = false WHERE l.expiresAt < :now AND l.active = true")
    int deactivateExpiredLinks(LocalDateTime now);

    @Query("SELECT COUNT(l) FROM Link l")
    long countTotalLinks();

    @Query("SELECT SUM(l.clickCount) FROM Link l")
    Long sumTotalClicks();

    @Query("SELECT COUNT(l) FROM Link l WHERE l.active = true AND l.expiresAt > :now")
    long countActiveLinks(LocalDateTime now);
}
