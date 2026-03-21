# ELK Stack Configuration - Quick Start Guide

## 🚀 Quick Start

### 1. Start ELK Stack

```powershell
cd "D:\Internal\Google Keep"
.\manage-elk.ps1 -Action start
```

Or manually:
```powershell
docker-compose up -d
```

### 2. Start Spring Boot Services

```powershell
cd "D:\Internal\Google Keep\backend"
.\start-all-services.ps1
```

### 3. Access Kibana

Open browser: http://localhost:5601

#### First Time Setup in Kibana:
1. Go to **Management → Stack Management → Index Patterns**
2. Click **Create index pattern**
3. Enter: `springboot-logs-*`
4. Click **Next step**
5. Select **@timestamp** as the time field
6. Click **Create index pattern**

### 4. View Logs in Kibana

1. Go to **Analytics → Discover**
2. Select `springboot-logs-*` index pattern
3. View real-time logs from all microservices

## 📊 Service URLs

| Service        | URL                          | Purpose                    |
|---------------|------------------------------|----------------------------|
| Elasticsearch | http://localhost:9200        | Search and storage         |
| Kibana        | http://localhost:5601        | Visualization & UI         |
| Logstash      | tcp://localhost:5044         | Log ingestion              |

## 📁 Directory Structure

```
D:\Internal\Google Keep\
├── compose.yaml                    # Docker Compose configuration
├── manage-elk.ps1                  # ELK management script
├── filebeat/
│   └── filebeat.yml               # Filebeat configuration
├── logstash/
│   └── logstash.conf              # Logstash pipeline
└── backend/
    ├── logs/                      # Log files directory
    │   ├── eureka-server.log
    │   ├── api-gateway.log
    │   ├── auth-service.log
    │   ├── labels-service.log
    │   ├── notes-service.log
    │   └── media-service.log
    └── [service-name]/
        └── src/main/resources/
            ├── logback-spring.xml # Logging configuration
            └── application.properties
```

## 🛠️ Management Commands

### Using Management Script

```powershell
# Start ELK stack
.\manage-elk.ps1 -Action start

# Stop ELK stack
.\manage-elk.ps1 -Action stop

# Restart ELK stack
.\manage-elk.ps1 -Action restart

# Check status
.\manage-elk.ps1 -Action status

# View logs
.\manage-elk.ps1 -Action logs

# Clean old logs
.\manage-elk.ps1 -Action clean
```

### Using Docker Compose

```powershell
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f [service-name]

# Restart specific service
docker-compose restart [service-name]

# Check status
docker-compose ps
```

## 🔍 Verification Steps

### 1. Check Elasticsearch

```powershell
# Health check
curl http://localhost:9200/_cluster/health?pretty

# List indices
curl http://localhost:9200/_cat/indices?v

# Search logs
curl http://localhost:9200/springboot-logs-*/_search?pretty
```

### 2. Check Logstash

```powershell
# View Logstash logs
docker logs -f logstash

# Check Logstash stats
docker exec logstash curl -s localhost:9600/?pretty
```

### 3. Check Log Files

```powershell
# List log files
Get-ChildItem "D:\Internal\Google Keep\backend\logs"

# View log file
Get-Content "D:\Internal\Google Keep\backend\logs\notes-service.log" -Tail 50
```

## 📈 Kibana Queries

### Filter by Service
```
service: "notes-service"
```

### Filter by Log Level
```
level: "ERROR"
level: "WARN"
level: "INFO"
level: "DEBUG"
```

### Search Messages
```
message: *authentication*
message: *database*
message: *exception*
```

### Combine Filters
```
service: "auth-service" AND level: "ERROR"
service: "notes-service" AND message: *created*
level: "ERROR" OR level: "WARN"
```

### Time Range
- Use the time picker in Kibana (top-right)
- Last 15 minutes, Last 1 hour, Last 24 hours, etc.

## 🎨 Recommended Kibana Visualizations

### 1. Log Count by Service
- **Type**: Vertical Bar Chart
- **Y-axis**: Count
- **X-axis**: Terms aggregation on `service`

### 2. Error Rate Over Time
- **Type**: Line Chart
- **Y-axis**: Count
- **X-axis**: Date Histogram on `@timestamp`
- **Filter**: level: "ERROR"

### 3. Top Error Messages
- **Type**: Data Table
- **Metrics**: Count
- **Buckets**: Terms on `message`
- **Filter**: level: "ERROR"

### 4. Response Time Distribution
- **Type**: Histogram
- **Field**: response_time (if logged)

## 🐛 Troubleshooting

### Logs Not Appearing in Kibana

1. **Check if Spring Boot services are running**:
   ```powershell
   jps -l  # Java processes
   ```

2. **Check Logstash is receiving logs**:
   ```powershell
   docker logs logstash | Select-String "springboot"
   ```

3. **Verify log files are being created**:
   ```powershell
   Get-ChildItem "D:\Internal\Google Keep\backend\logs"
   ```

4. **Check Elasticsearch indices**:
   ```powershell
   curl http://localhost:9200/_cat/indices?v
   ```

### Filebeat Not Working

1. **Check Filebeat logs**:
   ```powershell
   docker logs filebeat
   ```

2. **Verify file permissions**:
   ```powershell
   Get-Acl "D:\Internal\Google Keep\backend\logs"
   ```

3. **Restart Filebeat**:
   ```powershell
   docker-compose restart filebeat
   ```

### Elasticsearch Out of Memory

1. **Increase heap size** in compose.yaml:
   ```yaml
   environment:
     - ES_JAVA_OPTS=-Xms2g -Xmx2g
   ```

2. **Restart Elasticsearch**:
   ```powershell
   docker-compose restart elasticsearch
   ```

### Connection Refused to Logstash

1. **Check Logstash is running**:
   ```powershell
   docker ps | Select-String logstash
   ```

2. **Check port binding**:
   ```powershell
   netstat -ano | Select-String "5044"
   ```

3. **Verify logback-spring.xml** has correct host:
   ```xml
   <destination>localhost:5044</destination>
   ```

## 📝 Log Levels

Configure in `logback-spring.xml` for each service:

```xml
<!-- Application logs -->
<logger name="com.rajan.keep" level="DEBUG"/>

<!-- Spring Framework -->
<logger name="org.springframework.web" level="DEBUG"/>

<!-- SQL queries -->
<logger name="org.hibernate.SQL" level="DEBUG"/>

<!-- Root level (all other logs) -->
<root level="INFO">
```

**Levels** (most to least verbose):
- `TRACE` - Very detailed information
- `DEBUG` - Debugging information
- `INFO` - General information
- `WARN` - Warning messages
- `ERROR` - Error messages

## 🔐 Security Notes

**Current Setup** (Development Only):
- Elasticsearch security is DISABLED
- No authentication required
- All ports exposed to localhost

**For Production**:
- Enable Elasticsearch security
- Configure authentication
- Use TLS/SSL
- Restrict network access
- Set up role-based access control

## 📚 Additional Resources

- Full documentation: `backend/ELK-INTEGRATION-GUIDE.md`
- Elasticsearch docs: https://www.elastic.co/guide/en/elasticsearch/reference/current/
- Kibana docs: https://www.elastic.co/guide/en/kibana/current/
- Logstash docs: https://www.elastic.co/guide/en/logstash/current/
- Filebeat docs: https://www.elastic.co/guide/en/beats/filebeat/current/

## 🎯 Common Use Cases

### Find All Errors in Last Hour
1. Go to Kibana Discover
2. Set time range: "Last 1 hour"
3. Query: `level: "ERROR"`

### Track Specific User Activity
1. Query: `message: *user_id:12345*`
2. Or: `user_id: 12345` (if user_id is a separate field)

### Monitor Service Performance
1. Create visualization: Average response time by service
2. Set up alert when response time > threshold

### Debug Failed Requests
1. Query: `level: "ERROR" AND message: *HTTP*`
2. Expand log entry to see stack trace

### Monitor Database Queries
1. Query: `logger_name: "org.hibernate.SQL"`
2. View all SQL queries executed

## 💡 Tips

1. **Use field filters** in Kibana for faster searching
2. **Create saved searches** for frequently used queries
3. **Set up alerts** for critical errors (requires X-Pack)
4. **Use tags** in your logs for better categorization
5. **Monitor disk space** - Elasticsearch can grow quickly
6. **Regular cleanup** - Delete old indices to save space
7. **Use async logging** - Already configured to prevent blocking

## 🔄 Daily Workflow

1. **Start ELK Stack**: `.\manage-elk.ps1 -Action start`
2. **Start Services**: `.\start-all-services.ps1`
3. **Monitor in Kibana**: http://localhost:5601
4. **Stop Everything**:
   ```powershell
   .\stop-all-services.ps1
   docker-compose down
   ```

---

**For detailed information**, see: `backend/ELK-INTEGRATION-GUIDE.md`

