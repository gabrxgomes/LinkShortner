# Link Shortener

A professional URL shortening service built with Spring Boot, featuring auto-expiring links and comprehensive analytics.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Production Deployment](#production-deployment)
- [API Documentation](#api-documentation)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Documentation](#documentation)
- [License](#license)

## Overview

This URL shortener provides a robust solution for creating short, trackable links with automatic expiration. Built with enterprise-grade Spring Boot architecture, it offers secure URL validation, click tracking, and scheduled cleanup of expired links.

## Features

- **URL Shortening**: Generate short, unique codes for long URLs
- **Auto-Expiration**: Links automatically expire after a configurable time period (default: 24 hours)
- **Click Tracking**: Monitor the number of clicks for each shortened link
- **URL Validation**: Comprehensive security checks to prevent malicious URLs
- **System Statistics**: Real-time metrics on total links, clicks, and active links
- **Automated Cleanup**: Scheduled task to remove expired links from the database
- **Dual Database Support**: H2 for development, PostgreSQL for production
- **RESTful API**: Clean, well-documented API endpoints

## Technology Stack

- **Backend**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**:
  - Development: H2 (in-memory)
  - Production: PostgreSQL
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Input validation, URL sanitization
- **Build Tool**: Maven
- **Containerization**: Docker
- **Deployment**: Render (with Docker support)

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Docker (optional, for containerized deployment)

### Local Development

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd LinkShortner
   ```

2. **Build the project**
   ```bash
   mvn clean package
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080` with an embedded H2 database.

4. **Access the H2 Console** (development only)
   ```
   URL: http://localhost:8080/h2-console
   JDBC URL: jdbc:h2:mem:linkdb
   Username: sa
   Password: (leave blank)
   ```

### Production Deployment

For production deployment instructions, refer to:
- [Deployment Guide](docs/DEPLOYMENT.md)
- [Maintenance Guide](docs/MAINTENANCE.md)

## API Documentation

### Create Short Link
```http
POST /api/links
Content-Type: application/json

{
  "url": "https://example.com/very/long/url",
  "expirationHours": 24
}
```

**Response:**
```json
{
  "shortCode": "aBc123",
  "shortUrl": "https://your-domain.com/aBc123",
  "originalUrl": "https://example.com/very/long/url",
  "clickCount": 0,
  "createdAt": "2025-12-27T10:00:00",
  "expiresAt": "2025-12-28T10:00:00",
  "active": true
}
```

### Redirect to Original URL
```http
GET /{shortCode}
```

Redirects (302) to the original URL and increments the click counter.

### Get Link Statistics
```http
GET /api/links/{shortCode}/stats
```

Returns statistics for a specific shortened link.

### Get System Statistics
```http
GET /api/stats
```

Returns overall system statistics including total links, total clicks, and active links.

## Project Structure

```
LinkShortner/
├── src/
│   ├── main/
│   │   ├── java/com/linkshortener/
│   │   │   ├── config/           # Configuration classes
│   │   │   ├── controller/       # REST controllers
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── model/            # JPA entities
│   │   │   ├── repository/       # Data access layer
│   │   │   ├── scheduler/        # Scheduled tasks
│   │   │   ├── service/          # Business logic
│   │   │   └── LinkShortenerApplication.java
│   │   └── resources/
│   │       ├── static/           # Frontend assets
│   │       ├── application.properties
│   │       └── application-production.properties
│   └── test/                     # Unit and integration tests
├── docs/                         # Additional documentation
├── Dockerfile                    # Docker configuration
├── render.yaml                   # Render deployment configuration
├── pom.xml                       # Maven configuration
└── README.md
```

## Configuration

The application can be configured through `application.properties`:

```properties
# Server Configuration
server.port=8080

# Application Configuration
app.base-url=http://localhost:8080
app.link-expiration-hours=24
app.short-code-length=6

# Security Configuration
app.max-url-length=2048
app.blocked-domains=localhost,127.0.0.1,0.0.0.0
```

For production configuration, see `application-production.properties`.

## Documentation

Comprehensive documentation is available in the `docs/` directory:

- [Architecture Documentation](docs/ARCHITECTURE.md) - System design and technical decisions
- [Deployment Guide](docs/DEPLOYMENT.md) - Step-by-step deployment instructions
- [Maintenance Guide](docs/MAINTENANCE.md) - Operational procedures and troubleshooting
- [Security Documentation](docs/SECURITY.md) - Security features and best practices

### Tutorials

- [Tutorial Completo em Português](docs/TUTORIAL_PT.md) - Guia passo a passo para criar e deployar uma aplicação Spring Boot do zero, incluindo integração com PostgreSQL e deploy no Render

## License

This project is licensed under the MIT License.
