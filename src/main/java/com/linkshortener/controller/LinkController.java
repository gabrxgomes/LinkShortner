package com.linkshortener.controller;

import com.linkshortener.dto.CreateLinkRequest;
import com.linkshortener.dto.LinkResponse;
import com.linkshortener.dto.SystemStatsResponse;
import com.linkshortener.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/api/shorten")
    public ResponseEntity<?> createShortLink(@Valid @RequestBody CreateLinkRequest request) {
        try {
            LinkResponse response = linkService.createShortLink(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{4,10}}")
    public RedirectView redirect(@PathVariable String shortCode) {
        return linkService.getOriginalUrl(shortCode)
            .map(url -> {
                RedirectView redirectView = new RedirectView();
                redirectView.setUrl(url);
                return redirectView;
            })
            .orElseGet(() -> {
                RedirectView redirectView = new RedirectView();
                redirectView.setUrl("/error.html");
                return redirectView;
            });
    }

    @GetMapping("/api/stats/{shortCode}")
    public ResponseEntity<?> getStats(@PathVariable String shortCode) {
        return linkService.getLinkStats(shortCode)
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Link not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            });
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/system-stats")
    public ResponseEntity<SystemStatsResponse> getSystemStats() {
        SystemStatsResponse stats = linkService.getSystemStats();
        return ResponseEntity.ok(stats);
    }
}
