# Architecture Documentation

## System Overview

This document describes the technical architecture, design decisions, and implementation details of the Link Shortener application.

## Table of Contents

- [System Architecture](#system-architecture)
- [Design Patterns](#design-patterns)
- [Data Model](#data-model)
- [Core Components](#core-components)
- [Security Considerations](#security-considerations)
- [Performance Optimizations](#performance-optimizations)
- [Scalability Considerations](#scalability-considerations)

## System Architecture

### High-Level Architecture

```
┌─────────────┐
│   Client    │
│  (Browser)  │
└──────┬──────┘
       │ HTTP/HTTPS
       ▼
┌─────────────────────────────┐
│    Spring Boot Application  │
│  ┌─────────────────────┐   │
│  │  Controller Layer   │   │ ◄── REST API Endpoints
│  └──────────┬──────────┘   │
│             │              │
│  ┌──────────▼──────────┐   │
│  │   Service Layer     │   │ ◄── Business Logic
│  └──────────┬──────────┘   │
│             │              │
│  ┌──────────▼──────────┐   │
│  │  Repository Layer   │   │ ◄── Data Access
│  └──────────┬──────────┘   │
└─────────────┼──────────────┘
              │ JPA/Hibernate
              ▼
       ┌─────────────┐
       │  PostgreSQL │
       │  Database   │
       └─────────────┘
```

### Layer Responsibilities

1. **Controller Layer**: Handles HTTP requests, input validation, and response formatting
2. **Service Layer**: Contains business logic, URL validation, and link generation
3. **Repository Layer**: Manages database operations through Spring Data JPA
4. **Model Layer**: Defines data entities and their relationships

## Design Patterns

### 1. Repository Pattern
Used for data access abstraction:
```java
@Repository
public interface LinkRepository extends JpaRepository<Link, Long>
```

Benefits:
- Separation of concerns
- Testability
- Database independence

### 2. Service Pattern
Encapsulates business logic:
```java
@Service
@RequiredArgsConstructor
public class LinkService
```

Benefits:
- Business logic centralization
- Transaction management
- Reusability

### 3. DTO Pattern
Data Transfer Objects for API communication:
```java
@Data
@Builder
public class LinkResponse
```

Benefits:
- API contract stability
- Prevents over/under-fetching
- Security (prevents sensitive data exposure)

### 4. Builder Pattern
Used with Lombok for object construction:
```java
LinkResponse.builder()
    .shortCode(link.getShortCode())
    .build();
```

## Data Model

### Entity: Link

```java
@Entity
@Table(name = "links", indexes = {
    @Index(name = "idx_short_code", columnList = "short_code", unique = true),
    @Index(name = "idx_expires_at", columnList = "expires_at"),
    @Index(name = "idx_active", columnList = "active")
})
public class Link {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
```

### Database Indexes

1. **idx_short_code**: Unique index for fast lookups during redirection
2. **idx_expires_at**: Supports efficient cleanup of expired links
3. **idx_active**: Optimizes queries filtering by active status

## Core Components

### 1. LinkService

**Responsibilities:**
- Generate unique short codes
- Validate and sanitize URLs
- Manage link lifecycle
- Track click statistics

**Key Methods:**
- `createShortLink()`: Creates a new shortened link
- `getOriginalUrl()`: Retrieves original URL and increments click count
- `getLinkStats()`: Returns statistics for a specific link
- `getSystemStats()`: Provides system-wide metrics

**Short Code Generation Algorithm:**
```
1. Generate random alphanumeric string (default 6 characters)
2. Check database for uniqueness
3. If collision detected, retry up to 10 times
4. If still colliding, increase length by 1 character
5. Return unique short code
```

### 2. UrlValidationService

**Responsibilities:**
- Validate URL format and structure
- Sanitize URLs to prevent injection attacks
- Block malicious or restricted domains

**Validation Rules:**
- Must be a valid URL format
- Maximum length: 2048 characters
- Blocked protocols: javascript:, data:, file:
- Blocked domains: localhost, 127.0.0.1, 0.0.0.0

### 3. LinkCleanupScheduler

**Responsibilities:**
- Automatically remove expired links
- Maintain database performance

**Configuration:**
- Runs daily at 2 AM
- Uses cron expression: `0 0 2 * * *`
- Deletes links where `expires_at < current_time AND active = false`

## Security Considerations

### 1. URL Validation
- Apache Commons Validator for URL format verification
- Custom validation for protocol and domain restrictions
- Maximum URL length enforcement (2048 characters)

### 2. Input Sanitization
- URL trimming and normalization
- Removal of potentially dangerous characters
- Prevention of open redirect vulnerabilities

### 3. Database Security
- Prepared statements (via JPA) prevent SQL injection
- Connection pooling with HikariCP
- Environment-based credential management

### 4. CORS Configuration
- Configurable allowed origins
- Supports production and development environments
- Credentials support when needed

### 5. Rate Limiting Considerations
Currently not implemented, but recommended additions:
- IP-based rate limiting (e.g., 100 requests per hour)
- Short code creation limits per IP
- Consider using Spring Cloud Gateway or Redis for distributed rate limiting

## Performance Optimizations

### 1. Database Optimizations
- Strategic indexing on frequently queried columns
- Connection pooling (HikariCP)
- Lazy loading for relationships
- Query optimization through Spring Data JPA

### 2. Caching Opportunities
Future improvements:
- Redis cache for frequently accessed short codes
- Cache-aside pattern for read-heavy operations
- TTL-based cache expiration aligned with link expiration

### 3. Transaction Management
- Read-only transactions for query operations
- Minimal transaction scope
- Optimistic locking for concurrent updates

## Scalability Considerations

### Current Limitations
- Single database instance
- In-memory scheduled tasks (not distributed)
- No horizontal scaling support for schedulers

### Recommended Improvements

#### 1. Database Scaling
- Read replicas for query operations
- Write operations to primary database
- Connection pool sizing based on load

#### 2. Application Scaling
- Stateless application design (already implemented)
- Horizontal pod autoscaling in Kubernetes
- Load balancer distribution

#### 3. Distributed Scheduling
- Replace in-memory scheduler with distributed solution
- Options: Quartz with JDBC store, ShedLock, or AWS EventBridge
- Ensures only one instance runs cleanup tasks

#### 4. Caching Layer
- Redis for distributed caching
- Reduces database load
- Improves response times for frequently accessed links

## Technical Decisions

### Why Spring Boot?
- Rapid development with convention over configuration
- Extensive ecosystem and community support
- Production-ready features (actuator, metrics, health checks)
- Excellent database integration with Spring Data JPA

### Why PostgreSQL?
- ACID compliance for data integrity
- Excellent indexing support
- Wide deployment support across cloud platforms
- Strong community and tooling

### Why H2 for Development?
- Zero configuration required
- Fast startup times
- Perfect for testing and local development
- Embedded database simplifies setup

### Why Docker?
- Consistent deployment across environments
- Simplified dependency management
- Cloud platform compatibility
- Easy rollback and versioning

## Future Enhancements

### Short-term
1. Add user authentication and authorization
2. Implement per-user link management
3. Add custom short code support
4. Provide detailed analytics (geography, devices, referrers)

### Long-term
1. Multi-tenancy support
2. API rate limiting and quotas
3. QR code generation for links
4. Webhooks for link events
5. Link expiration policies (views-based, date-based)
6. A/B testing support for multiple destination URLs

## Monitoring and Observability

### Current Capabilities
- Application logging with SLF4J
- Structured logging for key operations
- Error tracking in logs

### Recommended Additions
1. Spring Boot Actuator for metrics endpoints
2. Prometheus for metrics collection
3. Grafana for visualization
4. Distributed tracing with Zipkin or Jaeger
5. Centralized logging with ELK stack or CloudWatch

## Conclusion

This architecture provides a solid foundation for a production-ready URL shortener service. The modular design, security considerations, and performance optimizations ensure the system can handle real-world usage while remaining maintainable and extensible.
