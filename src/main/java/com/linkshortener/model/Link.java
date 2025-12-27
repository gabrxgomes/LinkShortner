package com.linkshortener.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "links")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(nullable = false, length = 2048)
    private String originalUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Long clickCount = 0L;

    @Column(nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (expiresAt == null) {
            // Default 24 hours expiration
            expiresAt = createdAt.plusHours(24);
        }
        if (clickCount == null) {
            clickCount = 0L;
        }
        if (active == null) {
            active = true;
        }
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public void incrementClickCount() {
        this.clickCount++;
    }
}
