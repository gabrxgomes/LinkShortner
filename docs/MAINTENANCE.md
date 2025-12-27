# Maintenance Guide

This guide provides operational procedures, monitoring strategies, and troubleshooting steps for maintaining the Link Shortener application in production.

## Table of Contents

- [Routine Maintenance](#routine-maintenance)
- [Monitoring](#monitoring)
- [Database Management](#database-management)
- [Performance Tuning](#performance-tuning)
- [Common Issues](#common-issues)
- [Backup and Recovery](#backup-and-recovery)
- [Scaling](#scaling)
- [Security Updates](#security-updates)

## Routine Maintenance

### Daily Tasks

1. **Review Application Logs**
   ```bash
   # For Render: Check logs in dashboard
   # For traditional server:
   sudo journalctl -u linkshortener -f --since "24 hours ago"
   ```

   Look for:
   - Error messages
   - Unusual activity patterns
   - Failed database connections
   - Security warnings

2. **Check System Health**
   ```bash
   curl https://your-domain.com/api/stats
   ```

   Verify response is healthy and metrics are reasonable.

### Weekly Tasks

1. **Database Performance Review**
   ```sql
   -- Check database size
   SELECT pg_size_pretty(pg_database_size('linkshort'));

   -- Check table sizes
   SELECT
       schemaname,
       tablename,
       pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) AS size
   FROM pg_tables
   WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
   ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

   -- Check for bloated indexes
   SELECT
       schemaname,
       tablename,
       indexname,
       pg_size_pretty(pg_relation_size(indexrelid)) AS index_size
   FROM pg_indexes
   JOIN pg_class ON pg_indexes.indexname = pg_class.relname
   WHERE schemaname NOT IN ('pg_catalog', 'information_schema')
   ORDER BY pg_relation_size(indexrelid) DESC;
   ```

2. **Review Click Statistics**
   ```sql
   -- Top 10 most clicked links
   SELECT short_code, original_url, click_count, created_at
   FROM links
   ORDER BY click_count DESC
   LIMIT 10;

   -- Links created in last 7 days
   SELECT COUNT(*) AS new_links
   FROM links
   WHERE created_at >= NOW() - INTERVAL '7 days';
   ```

3. **Check Expired Links Cleanup**
   ```sql
   -- Verify cleanup is running
   SELECT COUNT(*) AS expired_inactive_links
   FROM links
   WHERE expires_at < NOW() AND active = false;

   -- This count should be low if cleanup is working
   ```

### Monthly Tasks

1. **Database Backup Verification**
   - Test restore procedure
   - Verify backup integrity
   - Update backup retention policy

2. **Security Audit**
   - Review access logs for suspicious activity
   - Update dependencies for security patches
   - Rotate database credentials (if policy requires)

3. **Performance Analysis**
   - Analyze response times
   - Review database query performance
   - Identify slow queries

4. **Capacity Planning**
   - Review storage usage trends
   - Analyze traffic patterns
   - Plan for scaling needs

## Monitoring

### Application Metrics

Monitor these key metrics:

1. **Request Rate**: Number of requests per minute
2. **Error Rate**: Percentage of failed requests
3. **Response Time**: Average and p95 response times
4. **Database Connection Pool**: Active and idle connections

### Database Metrics

Monitor these PostgreSQL metrics:

1. **Connection Count**
   ```sql
   SELECT count(*) FROM pg_stat_activity;
   ```

2. **Active Queries**
   ```sql
   SELECT pid, now() - pg_stat_activity.query_start AS duration, query
   FROM pg_stat_activity
   WHERE state = 'active' AND now() - pg_stat_activity.query_start > interval '1 second'
   ORDER BY duration DESC;
   ```

3. **Lock Wait**
   ```sql
   SELECT * FROM pg_stat_activity WHERE wait_event_type = 'Lock';
   ```

4. **Cache Hit Ratio**
   ```sql
   SELECT
       sum(heap_blks_read) as heap_read,
       sum(heap_blks_hit) as heap_hit,
       (sum(heap_blks_hit) - sum(heap_blks_read)) / sum(heap_blks_hit) as ratio
   FROM pg_statio_user_tables;
   ```

   Target: > 0.99 (99% cache hits)

### Alert Thresholds

Configure alerts for:

| Metric | Warning | Critical |
|--------|---------|----------|
| Response Time (p95) | > 500ms | > 1000ms |
| Error Rate | > 1% | > 5% |
| Database Connections | > 80% | > 95% |
| Disk Space | > 80% | > 90% |
| Memory Usage | > 85% | > 95% |

## Database Management

### Cleanup Operations

The application includes automatic cleanup via scheduled tasks. To manually trigger cleanup:

```sql
-- Delete expired and inactive links
DELETE FROM links
WHERE expires_at < NOW() AND active = false;

-- Analyze tables after cleanup
ANALYZE links;
```

### Index Maintenance

```sql
-- Rebuild indexes if needed
REINDEX TABLE links;

-- Update statistics
ANALYZE VERBOSE links;
```

### Database Vacuum

```sql
-- Regular vacuum (can run online)
VACUUM ANALYZE links;

-- Full vacuum (requires table lock, schedule during maintenance window)
VACUUM FULL ANALYZE links;
```

### Connection Pooling

Monitor and adjust HikariCP settings in `application-production.properties`:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

Guidelines:
- `maximum-pool-size`: Start with 10, adjust based on load
- `minimum-idle`: 2-5 for quick response to traffic spikes
- Monitor with: `SELECT count(*) FROM pg_stat_activity WHERE datname = 'linkshort';`

## Performance Tuning

### Application-Level Optimizations

1. **JVM Tuning**
   ```bash
   # Set appropriate heap size
   java -Xms256m -Xmx512m -jar app.jar

   # Enable G1GC for better pause times
   java -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -jar app.jar

   # Monitor GC
   java -Xlog:gc*:file=gc.log -jar app.jar
   ```

2. **Database Query Optimization**
   ```sql
   -- Enable query logging
   ALTER SYSTEM SET log_min_duration_statement = 100; -- Log queries > 100ms
   SELECT pg_reload_conf();

   -- Review slow queries
   SELECT query, calls, total_time, mean_time
   FROM pg_stat_statements
   ORDER BY mean_time DESC
   LIMIT 10;
   ```

3. **Connection Pool Sizing**

   Formula: `connections = ((core_count * 2) + effective_spindle_count)`

   For a 2-core system with SSD: `connections = (2 * 2) + 1 = 5`

   Adjust `spring.datasource.hikari.maximum-pool-size` accordingly.

### Database-Level Optimizations

1. **PostgreSQL Configuration**

   Edit `/etc/postgresql/*/main/postgresql.conf`:

   ```ini
   # For 2GB RAM server
   shared_buffers = 512MB
   effective_cache_size = 1536MB
   maintenance_work_mem = 128MB
   work_mem = 16MB
   ```

2. **Enable Query Planning Cache**
   ```sql
   ALTER DATABASE linkshort SET plan_cache_mode = 'auto';
   ```

## Common Issues

### Issue: High Memory Usage

**Symptoms:**
- Application becomes unresponsive
- Out of memory errors in logs

**Diagnosis:**
```bash
# Check memory usage
free -h

# Check Java process memory
ps aux | grep java

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <pid>
```

**Solutions:**
1. Reduce JVM heap size if over-allocated
2. Check for memory leaks in logs
3. Restart application to clear memory
4. Consider upgrading server resources

### Issue: Slow Database Queries

**Symptoms:**
- High response times
- Database connection pool exhaustion

**Diagnosis:**
```sql
-- Find slow queries
SELECT query, calls, total_time, mean_time, max_time
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 20;

-- Check for missing indexes
SELECT schemaname, tablename, attname, n_distinct, correlation
FROM pg_stats
WHERE schemaname = 'public'
ORDER BY abs(correlation) DESC;
```

**Solutions:**
1. Add indexes to frequently queried columns
2. Optimize queries with EXPLAIN ANALYZE
3. Increase database resources
4. Consider read replicas for read-heavy workloads

### Issue: Application Won't Start

**Symptoms:**
- Service fails to start
- Port already in use errors

**Diagnosis:**
```bash
# Check if port is in use
sudo netstat -tlnp | grep 8080

# Check application logs
sudo journalctl -u linkshortener -n 100

# Verify database connectivity
psql -h localhost -U linkshort_user -d linkshort -c "SELECT 1;"
```

**Solutions:**
1. Ensure port 8080 is available or change `server.port`
2. Verify database credentials
3. Check database is running and accessible
4. Review environment variables

### Issue: Render Free Tier Database Expiration

**Symptoms:**
- Database connection errors after 90 days
- "database does not exist" errors

**Diagnosis:**
- Check Render dashboard for database status
- Verify database age

**Solutions:**
1. Create new PostgreSQL database on Render
2. Update environment variables with new credentials:
   - `POSTGRES_URL`: New database JDBC URL
   - `POSTGRES_USER`: New username
   - `POSTGRES_PASSWORD`: New password
3. Trigger manual deployment to apply changes
4. Consider paid tier for persistent databases

## Backup and Recovery

### Backup Strategy

1. **Automated Database Backups** (Render)
   - Render Free tier: No automatic backups
   - Render Paid tier: Automatic daily backups

2. **Manual Database Backup**
   ```bash
   # Create backup
   pg_dump -h hostname -U username -d linkshort > backup_$(date +%Y%m%d_%H%M%S).sql

   # Create compressed backup
   pg_dump -h hostname -U username -d linkshort | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
   ```

3. **Backup Schedule**
   - Daily: Full database backup
   - Retention: 30 days
   - Storage: Off-site (S3, Google Cloud Storage, etc.)

### Recovery Procedure

1. **Restore from Backup**
   ```bash
   # Restore from SQL file
   psql -h hostname -U username -d linkshort < backup.sql

   # Restore from compressed file
   gunzip -c backup.sql.gz | psql -h hostname -U username -d linkshort
   ```

2. **Verify Restoration**
   ```sql
   -- Check record count
   SELECT COUNT(*) FROM links;

   -- Verify recent data
   SELECT * FROM links ORDER BY created_at DESC LIMIT 10;
   ```

## Scaling

### Vertical Scaling (Increase Resources)

1. **Render**: Upgrade to higher tier plan
2. **Traditional Server**: Increase RAM/CPU

### Horizontal Scaling (Add Instances)

Current limitations:
- Scheduled cleanup task runs on all instances (acceptable, idempotent)
- No session state (application is stateless, supports scaling)

Recommended setup:
1. Deploy multiple application instances
2. Use load balancer (Render provides this automatically)
3. Single PostgreSQL primary for writes
4. Read replicas for read operations (future enhancement)

### Caching Layer (Future Enhancement)

For high-traffic deployments, consider adding Redis:

```properties
# Redis configuration (example)
spring.cache.type=redis
spring.redis.host=redis-host
spring.redis.port=6379
spring.cache.redis.time-to-live=3600000
```

Cache frequently accessed short codes to reduce database load.

## Security Updates

### Dependency Updates

1. **Check for updates**
   ```bash
   mvn versions:display-dependency-updates
   ```

2. **Update Spring Boot**
   Edit `pom.xml`:
   ```xml
   <parent>
       <groupId>org.springframework.boot</groupId>
       <artifactId>spring-boot-starter-parent</artifactId>
       <version>3.2.x</version> <!-- Update to latest patch version -->
   </parent>
   ```

3. **Test updates**
   ```bash
   mvn clean test
   mvn spring-boot:run
   ```

4. **Deploy updates**
   - Test in staging environment first
   - Schedule deployment during low-traffic period
   - Monitor for issues after deployment

### Security Best Practices

1. **Environment Variables**
   - Never commit credentials to Git
   - Rotate passwords quarterly
   - Use strong, unique passwords

2. **Database Access**
   - Limit connections to application server only
   - Use firewall rules to restrict access
   - Keep PostgreSQL updated

3. **Application Updates**
   - Subscribe to Spring Boot security announcements
   - Apply security patches promptly
   - Monitor CVE databases for used dependencies

## Conclusion

Regular maintenance ensures the Link Shortener application remains secure, performant, and reliable. Follow the schedules outlined above and adjust based on your specific operational requirements.

For deployment procedures, refer to the [Deployment Guide](DEPLOYMENT.md).
For technical architecture details, refer to the [Architecture Documentation](ARCHITECTURE.md).
