# Security Documentation

This document outlines the security measures, vulnerabilities addressed, and best practices implemented in the Link Shortener application.

## Table of Contents

- [Security Features](#security-features)
- [Threat Model](#threat-model)
- [Security Measures](#security-measures)
- [Known Limitations](#known-limitations)
- [Security Best Practices](#security-best-practices)
- [Incident Response](#incident-response)
- [Compliance Considerations](#compliance-considerations)

## Security Features

### 1. URL Validation and Sanitization

**Implementation**: `UrlValidationService`

**Protections:**
- Validates URL format using Apache Commons Validator
- Enforces maximum URL length (2048 characters)
- Blocks dangerous protocols (javascript:, data:, file:)
- Prevents localhost and internal network access
- Sanitizes input by trimming whitespace

**Code Reference**: `src/main/java/com/linkshortener/service/UrlValidationService.java`

### 2. SQL Injection Prevention

**Implementation**: Spring Data JPA with Hibernate

**Protections:**
- All database queries use prepared statements via JPA
- No raw SQL concatenation
- Parameterized queries prevent injection attacks

**Example:**
```java
@Query("SELECT l FROM Link l WHERE l.shortCode = :shortCode")
Optional<Link> findByShortCode(@Param("shortCode") String shortCode);
```

### 3. Open Redirect Prevention

**Implementation**: URL validation and domain blocking

**Protections:**
- Validates destination URLs before creating short links
- Blocks internal network addresses (localhost, 127.0.0.1, 0.0.0.0)
- Configurable blocked domain list
- URL format validation prevents malformed redirects

### 4. Secure Random Code Generation

**Implementation**: `SecureRandom` for short code generation

**Protections:**
- Uses cryptographically secure random number generator
- Prevents predictable short codes
- Collision detection with retry mechanism

**Code:**
```java
private static final SecureRandom RANDOM = new SecureRandom();
private String generateRandomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
    }
    return sb.toString();
}
```

### 5. Input Validation

**Implementation**: Spring Boot Validation

**Protections:**
- DTO validation using `@Valid` and `@NotBlank` annotations
- Automatic validation before controller processing
- Structured error responses for invalid input

**Example:**
```java
@Data
public class CreateLinkRequest {
    @NotBlank(message = "URL is required")
    private String url;

    @Min(value = 1, message = "Expiration must be at least 1 hour")
    @Max(value = 8760, message = "Expiration cannot exceed 1 year")
    private Integer expirationHours;
}
```

## Threat Model

### Threats Addressed

| Threat | Mitigation | Status |
|--------|------------|--------|
| SQL Injection | Prepared statements via JPA | ✓ Implemented |
| Open Redirect | URL validation and domain blocking | ✓ Implemented |
| XSS via URL | URL sanitization and validation | ✓ Implemented |
| Predictable Short Codes | SecureRandom generation | ✓ Implemented |
| Malicious Protocol Injection | Protocol whitelist (http/https only) | ✓ Implemented |
| Internal Network Access | Localhost/internal IP blocking | ✓ Implemented |
| URL Injection | Max length enforcement and validation | ✓ Implemented |

### Threats Not Addressed (Limitations)

| Threat | Impact | Recommendation |
|--------|--------|----------------|
| Rate Limiting | DoS attacks, abuse | Implement API rate limiting |
| Authentication | Unauthorized link creation | Add user authentication |
| Link Enumeration | Short code discovery | Consider random vs sequential codes |
| CSRF | Cross-site request forgery | Add CSRF tokens for state-changing operations |
| Link Spam | Malicious content proliferation | Implement URL reputation checking |
| Click Fraud | Inflated analytics | Add IP-based click tracking |

## Security Measures

### 1. URL Validation Rules

```java
// Blocked protocols
private static final List<String> BLOCKED_PROTOCOLS = List.of(
    "javascript:", "data:", "file:", "vbscript:", "about:"
);

// Blocked domains
private List<String> getBlockedDomains() {
    String blocked = blockedDomains; // From application.properties
    return Arrays.asList(blocked.split(","));
}
```

**Configuration**: `application.properties`
```properties
app.blocked-domains=localhost,127.0.0.1,0.0.0.0,::1
```

### 2. Database Security

**Connection Security:**
- Environment-based credential management
- No hardcoded passwords in code
- Secure connection pooling with HikariCP

**Query Security:**
- JPA Entity-based queries
- No dynamic SQL construction
- Parameterized queries only

**Example secure query:**
```java
linkRepository.findByShortCodeAndActiveTrue(shortCode);
// Translates to: SELECT * FROM links WHERE short_code = ? AND active = true
```

### 3. CORS Configuration

**Implementation**: `SecurityConfig.java`

```java
@Configuration
public class SecurityConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:8080",
            "https://your-production-domain.com"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        return source;
    }
}
```

**Recommendation**: Configure `setAllowedOrigins()` to only include trusted domains.

### 4. Secure Headers

**Recommended additions** (via reverse proxy or Spring Security):

```properties
# Content Security Policy
Content-Security-Policy: default-src 'self'

# Prevent clickjacking
X-Frame-Options: DENY

# Prevent MIME sniffing
X-Content-Type-Options: nosniff

# XSS Protection
X-XSS-Protection: 1; mode=block

# Strict Transport Security (HTTPS only)
Strict-Transport-Security: max-age=31536000; includeSubDomains
```

## Known Limitations

### 1. No Rate Limiting

**Risk**: API abuse, DoS attacks

**Impact**: High traffic from malicious users can:
- Create excessive links
- Exhaust database resources
- Increase hosting costs

**Mitigation Recommendation**:
```java
// Example with Bucket4j
@RateLimiter(name = "createLink", fallbackMethod = "rateLimitFallback")
public LinkResponse createShortLink(CreateLinkRequest request) {
    // Implementation
}
```

### 2. No User Authentication

**Risk**: Uncontrolled link creation

**Impact**:
- Anyone can create links
- No ownership or management
- Potential for abuse

**Mitigation Recommendation**:
- Implement Spring Security with OAuth2
- Add user entity with link relationships
- Require authentication for link creation

### 3. Limited URL Reputation Checking

**Risk**: Malicious URL propagation

**Impact**:
- Phishing links
- Malware distribution
- Brand reputation damage

**Mitigation Recommendation**:
- Integrate with Google Safe Browsing API
- Implement URL reputation database
- Add manual review queue for suspicious links

### 4. No HTTPS Enforcement

**Risk**: Man-in-the-middle attacks

**Impact**:
- Data interception
- Session hijacking
- Credential theft (if auth added)

**Mitigation**:
- Configure HTTPS in reverse proxy
- Use Let's Encrypt for free certificates
- Redirect all HTTP traffic to HTTPS

## Security Best Practices

### For Developers

1. **Never commit secrets**
   ```bash
   # .gitignore already includes:
   application-production.properties
   *.env
   ```

2. **Use environment variables**
   ```java
   @Value("${app.secret-key}")
   private String secretKey; // Good

   // Never:
   private String secretKey = "hardcoded-secret"; // Bad
   ```

3. **Validate all inputs**
   ```java
   @PostMapping("/api/links")
   public ResponseEntity<LinkResponse> createLink(@Valid @RequestBody CreateLinkRequest request) {
       // @Valid triggers validation
   }
   ```

4. **Use parameterized queries**
   ```java
   // Good (JPA automatically parameterizes)
   linkRepository.findByShortCode(code);

   // Never use:
   entityManager.createNativeQuery("SELECT * FROM links WHERE short_code = '" + code + "'");
   ```

### For Operators

1. **Secure environment variables**
   - Use platform secrets management (Render environment variables, AWS Secrets Manager, etc.)
   - Rotate credentials quarterly
   - Use strong, unique passwords

2. **Enable HTTPS**
   ```nginx
   server {
       listen 443 ssl http2;
       ssl_certificate /path/to/cert.pem;
       ssl_certificate_key /path/to/key.pem;
       ssl_protocols TLSv1.2 TLSv1.3;
   }
   ```

3. **Monitor logs**
   ```bash
   # Watch for suspicious patterns
   grep -i "error\|exception\|hack\|inject" /var/log/application.log
   ```

4. **Regular updates**
   ```bash
   # Check for dependency updates
   mvn versions:display-dependency-updates

   # Update Spring Boot to latest patch version
   ```

## Incident Response

### Suspected Security Breach

1. **Immediate Actions**
   - Isolate affected systems
   - Preserve logs and evidence
   - Notify stakeholders

2. **Investigation**
   ```bash
   # Review recent logs
   sudo journalctl -u linkshortener --since "2 hours ago" > incident_logs.txt

   # Check database for suspicious activity
   psql -c "SELECT * FROM links WHERE created_at > NOW() - INTERVAL '2 hours' ORDER BY created_at DESC;"
   ```

3. **Remediation**
   - Rotate all credentials
   - Apply security patches
   - Update firewall rules
   - Review and update access controls

4. **Post-Incident**
   - Document incident timeline
   - Update security procedures
   - Implement additional monitoring
   - Conduct security training

### Malicious Link Reported

1. **Verification**
   ```sql
   SELECT * FROM links WHERE short_code = 'REPORTED_CODE';
   ```

2. **Deactivation**
   ```sql
   UPDATE links SET active = false WHERE short_code = 'REPORTED_CODE';
   ```

3. **Analysis**
   - Check for similar patterns
   - Identify source IP if logged
   - Block domain if pattern detected

4. **Prevention**
   - Add domain to blocked list
   - Consider URL reputation service
   - Update validation rules

## Compliance Considerations

### GDPR (if applicable)

The application currently does not collect personal data, but if user accounts are added:

- **Data Collection**: Minimal personal data (email only)
- **Data Storage**: Encrypted database
- **Data Retention**: Configurable link expiration
- **Right to Deletion**: Implement user account deletion
- **Data Portability**: Export user's links as JSON/CSV

### Accessibility

- **WCAG 2.1**: Ensure frontend meets Level AA standards
- **Keyboard Navigation**: All functions accessible via keyboard
- **Screen Reader Support**: Proper ARIA labels

### Content Security

If serving user-generated content:
- Implement Content-Security-Policy headers
- Sanitize any displayed URLs
- Consider Content-Disposition headers for downloads

## Security Checklist

Before production deployment:

- [ ] All secrets in environment variables (not in code)
- [ ] HTTPS configured with valid certificate
- [ ] Database credentials rotated from defaults
- [ ] Firewall configured (only necessary ports open)
- [ ] CORS configured for specific domains (not wildcard)
- [ ] Security headers configured in reverse proxy
- [ ] Logs monitored for security events
- [ ] Backup and recovery procedures tested
- [ ] Dependency versions up to date
- [ ] Rate limiting implemented (recommended)
- [ ] User authentication implemented (if required)

## Reporting Security Issues

If you discover a security vulnerability:

1. **Do Not** create a public GitHub issue
2. Contact the maintainers directly at: [security@your-domain.com]
3. Provide detailed information:
   - Vulnerability description
   - Steps to reproduce
   - Potential impact
   - Suggested remediation (if any)

We will respond within 48 hours and work with you to address the issue.

## Conclusion

This application implements fundamental security practices appropriate for a link shortener. However, production deployments should consider additional measures based on threat model, compliance requirements, and risk tolerance.

For operational security procedures, refer to the [Maintenance Guide](MAINTENANCE.md).
For deployment security configuration, refer to the [Deployment Guide](DEPLOYMENT.md).
