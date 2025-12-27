# Deployment Guide

This guide provides comprehensive instructions for deploying the Link Shortener application to production environments.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Deployment Platforms](#deployment-platforms)
  - [Render (Recommended)](#render-recommended)
  - [Docker-based Deployment](#docker-based-deployment)
  - [Traditional Server Deployment](#traditional-server-deployment)
- [Database Setup](#database-setup)
- [Environment Configuration](#environment-configuration)
- [Post-Deployment Verification](#post-deployment-verification)
- [Troubleshooting](#troubleshooting)

## Prerequisites

Before deploying, ensure you have:

1. Source code pushed to a Git repository (GitHub, GitLab, or Bitbucket)
2. Java 17 runtime environment
3. PostgreSQL database (version 12 or higher)
4. Domain name (optional, but recommended for production)

## Deployment Platforms

### Render (Recommended)

Render provides a straightforward deployment experience with Docker support.

#### Step 1: Create PostgreSQL Database

1. Log in to [Render Dashboard](https://dashboard.render.com)
2. Click **"New +"** and select **"PostgreSQL"**
3. Configure the database:
   - **Name**: `linkshortener-db` (or your preferred name)
   - **Database**: `linkshort_db` (note the actual database name)
   - **User**: Auto-generated (e.g., `linkshort_user`)
   - **Region**: Select closest to your users (e.g., Oregon US West)
   - **Plan**: Free (for testing) or paid (for production)
4. Click **"Create Database"**
5. After creation, note the following from the **Connections** tab:
   - **Internal Database URL** (for Render services)
   - **Hostname** (e.g., `dpg-xxxxx.oregon-postgres.render.com`)
   - **Port** (typically `5432`)
   - **Database name** (the actual database name created)
   - **Username**
   - **Password**

#### Step 2: Create Web Service

1. Click **"New +"** and select **"Web Service"**
2. Connect your Git repository
3. Configure the service:
   - **Name**: `linkshortener`
   - **Region**: Same as your database
   - **Branch**: `main`
   - **Runtime**: Docker
   - **Plan**: Free (for testing) or paid (for production)

#### Step 3: Configure Environment Variables

In the **Environment** tab, add the following variables:

```plaintext
BASE_URL=https://your-service-name.onrender.com
POSTGRES_URL=jdbc:postgresql://[hostname]:[port]/[database_name]
POSTGRES_USER=[username]
POSTGRES_PASSWORD=[password]
SPRING_PROFILES_ACTIVE=production
```

**Example:**
```plaintext
BASE_URL=https://linkshortener-abc.onrender.com
POSTGRES_URL=jdbc:postgresql://dpg-abc123.oregon-postgres.render.com:5432/linkshort_ngu2
POSTGRES_USER=linkshort_user
POSTGRES_PASSWORD=your_secure_password
SPRING_PROFILES_ACTIVE=production
```

**Important Notes:**
- Ensure the database name in `POSTGRES_URL` matches the actual database name created (not just "linkshort")
- Use the **Internal Hostname** from your PostgreSQL dashboard
- Do not include `postgresql://` prefix in `POSTGRES_URL`, start with `jdbc:postgresql://`

#### Step 4: Deploy

1. Render will automatically detect the `Dockerfile` and `render.yaml`
2. Click **"Create Web Service"**
3. Monitor the deployment logs for any errors
4. Once deployed, access your service at `https://your-service-name.onrender.com`

#### Render Configuration Files

The repository includes pre-configured files for Render:

**render.yaml:**
```yaml
services:
  - type: web
    name: linkshort
    env: java
    buildCommand: mvn clean package -DskipTests
    startCommand: java -Dserver.port=$PORT -jar target/link-shortener-1.0.0.jar
    envVars:
      - key: SPRING_PROFILES_ACTIVE
        value: production
      - key: JAVA_VERSION
        value: "17"
```

**Dockerfile:**
```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/link-shortener-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker-based Deployment

For any platform supporting Docker (AWS ECS, Google Cloud Run, Azure Container Instances):

#### Step 1: Build Docker Image

```bash
docker build -t linkshortener:latest .
```

#### Step 2: Test Locally

```bash
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e BASE_URL=http://localhost:8080 \
  -e POSTGRES_URL=jdbc:postgresql://your-db-host:5432/linkshort \
  -e POSTGRES_USER=your_user \
  -e POSTGRES_PASSWORD=your_password \
  linkshortener:latest
```

#### Step 3: Push to Container Registry

```bash
# Example for Docker Hub
docker tag linkshortener:latest yourusername/linkshortener:latest
docker push yourusername/linkshortener:latest

# Example for AWS ECR
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.us-east-1.amazonaws.com
docker tag linkshortener:latest 123456789012.dkr.ecr.us-east-1.amazonaws.com/linkshortener:latest
docker push 123456789012.dkr.ecr.us-east-1.amazonaws.com/linkshortener:latest
```

#### Step 4: Deploy to Platform

Follow your platform's specific instructions for deploying from a container registry.

### Traditional Server Deployment

For deployment on a VPS or dedicated server:

#### Step 1: Prepare the Server

```bash
# Install Java 17
sudo apt update
sudo apt install openjdk-17-jdk

# Install PostgreSQL
sudo apt install postgresql postgresql-contrib

# Create application user
sudo useradd -m -s /bin/bash linkshortener
```

#### Step 2: Setup Database

```bash
# Access PostgreSQL
sudo -u postgres psql

# Create database and user
CREATE DATABASE linkshort;
CREATE USER linkshort_user WITH ENCRYPTED PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE linkshort TO linkshort_user;
\q
```

#### Step 3: Build Application

```bash
# Clone repository
git clone <repository-url>
cd LinkShortner

# Build with Maven
./mvnw clean package -DskipTests

# Copy JAR to deployment directory
sudo mkdir -p /opt/linkshortener
sudo cp target/link-shortener-1.0.0.jar /opt/linkshortener/
sudo chown -R linkshortener:linkshortener /opt/linkshortener
```

#### Step 4: Create Application Properties

```bash
sudo nano /opt/linkshortener/application-production.properties
```

Add configuration:
```properties
server.port=8080

spring.datasource.url=jdbc:postgresql://localhost:5432/linkshort
spring.datasource.username=linkshort_user
spring.datasource.password=secure_password
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.base-url=https://your-domain.com
app.link-expiration-hours=24
app.short-code-length=6
```

#### Step 5: Create Systemd Service

```bash
sudo nano /etc/systemd/system/linkshortener.service
```

Add service configuration:
```ini
[Unit]
Description=Link Shortener Service
After=network.target postgresql.service

[Service]
Type=simple
User=linkshortener
WorkingDirectory=/opt/linkshortener
ExecStart=/usr/bin/java -jar /opt/linkshortener/link-shortener-1.0.0.jar --spring.config.location=/opt/linkshortener/application-production.properties --spring.profiles.active=production
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

#### Step 6: Start Service

```bash
sudo systemctl daemon-reload
sudo systemctl enable linkshortener
sudo systemctl start linkshortener
sudo systemctl status linkshortener
```

#### Step 7: Configure Nginx Reverse Proxy

```bash
sudo nano /etc/nginx/sites-available/linkshortener
```

Add Nginx configuration:
```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable site and restart Nginx:
```bash
sudo ln -s /etc/nginx/sites-available/linkshortener /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Database Setup

### PostgreSQL Configuration

Recommended PostgreSQL settings for production:

```sql
-- Increase connection limit if needed
ALTER SYSTEM SET max_connections = 100;

-- Enable logging for troubleshooting
ALTER SYSTEM SET log_statement = 'all';
ALTER SYSTEM SET log_duration = on;

-- Optimize for read-heavy workload
ALTER SYSTEM SET shared_buffers = '256MB';
ALTER SYSTEM SET effective_cache_size = '1GB';

-- Reload configuration
SELECT pg_reload_conf();
```

### Database Migrations

The application uses Hibernate's `ddl-auto=update` strategy. For production environments, consider:

1. Using Flyway or Liquibase for controlled migrations
2. Backing up database before deployments
3. Testing schema changes in staging environment

## Environment Configuration

### Required Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `SPRING_PROFILES_ACTIVE` | Active Spring profile | `production` |
| `BASE_URL` | Public URL of application | `https://short.link` |
| `POSTGRES_URL` | JDBC database URL | `jdbc:postgresql://host:5432/db` |
| `POSTGRES_USER` | Database username | `linkshort_user` |
| `POSTGRES_PASSWORD` | Database password | `secure_password` |

### Optional Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SERVER_PORT` | Application port | `8080` |
| `LINK_EXPIRATION_HOURS` | Default link expiration | `24` |
| `SHORT_CODE_LENGTH` | Length of generated codes | `6` |
| `MAX_URL_LENGTH` | Maximum URL length | `2048` |

## Post-Deployment Verification

### Health Check

```bash
curl https://your-domain.com/api/stats
```

Expected response:
```json
{
  "totalLinks": 0,
  "totalClicks": 0,
  "activeLinks": 0
}
```

### Create Test Link

```bash
curl -X POST https://your-domain.com/api/links \
  -H "Content-Type: application/json" \
  -d '{
    "url": "https://example.com",
    "expirationHours": 24
  }'
```

### Test Redirection

```bash
curl -I https://your-domain.com/{shortCode}
```

Expected: HTTP 302 redirect to original URL.

## Troubleshooting

### Database Connection Issues

**Problem**: Application fails to connect to database

**Solutions**:
1. Verify database credentials in environment variables
2. Check database hostname is accessible from application server
3. Ensure database name matches exactly (case-sensitive)
4. Verify PostgreSQL is running: `sudo systemctl status postgresql`
5. Check PostgreSQL logs: `sudo tail -f /var/log/postgresql/postgresql-*.log`

### Application Won't Start

**Problem**: Service fails to start or crashes immediately

**Solutions**:
1. Check application logs
2. Verify Java 17 is installed: `java -version`
3. Ensure all required environment variables are set
4. Check port 8080 is not already in use: `sudo netstat -tlnp | grep 8080`
5. Verify JAR file exists and has correct permissions

### Render-Specific Issues

**Problem**: Deploy fails on Render

**Solutions**:
1. Check build logs in Render dashboard
2. Ensure `Dockerfile` is present in repository root
3. Verify Maven build completes successfully locally
4. Check environment variables are correctly configured
5. Ensure database region matches web service region

### High Memory Usage

**Problem**: Application consumes excessive memory

**Solutions**:
1. Adjust JVM heap size:
   ```bash
   java -Xmx512m -Xms256m -jar app.jar
   ```
2. Monitor with JVM tools:
   ```bash
   jstat -gc <pid> 1000
   ```
3. Consider enabling G1GC:
   ```bash
   java -XX:+UseG1GC -jar app.jar
   ```

## Rollback Procedure

If deployment fails or issues arise:

### Render
1. Navigate to service in Render dashboard
2. Click "Events" tab
3. Find previous successful deployment
4. Click "Rollback" button

### Docker
1. Deploy previous image tag:
   ```bash
   docker pull yourusername/linkshortener:previous-tag
   docker stop linkshortener
   docker run -d --name linkshortener yourusername/linkshortener:previous-tag
   ```

### Traditional Server
1. Stop service:
   ```bash
   sudo systemctl stop linkshortener
   ```
2. Restore previous JAR:
   ```bash
   sudo cp /opt/linkshortener/backups/link-shortener-1.0.0.jar.backup /opt/linkshortener/link-shortener-1.0.0.jar
   ```
3. Start service:
   ```bash
   sudo systemctl start linkshortener
   ```

## Security Checklist

Before going to production:

- [ ] Environment variables stored securely (not in code)
- [ ] Database password is strong and unique
- [ ] HTTPS/TLS configured (use Let's Encrypt for free certificates)
- [ ] CORS configured appropriately
- [ ] Database backups automated
- [ ] Firewall rules configured (only necessary ports open)
- [ ] Application logs monitored
- [ ] Rate limiting implemented (if needed)
- [ ] Security headers configured in reverse proxy

## Conclusion

Following this guide ensures a secure, reliable deployment of the Link Shortener application. For ongoing operational procedures, refer to the [Maintenance Guide](MAINTENANCE.md).
