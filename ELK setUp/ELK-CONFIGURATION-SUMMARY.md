# ELK Stack Configuration Summary

## ✅ What Was Configured

### 1. Dependencies Added

**Parent POM** (`parent-pom.xml`):
- Added `logstash-logback-encoder` version 8.1
- Added dependency management for all child projects

**All Microservices** (eureka-server, api-gateway, auth-service, labels-service, notes-service, media-service):
- Added `logstash-logback-encoder` dependency

### 2. Logging Configuration Created

**Created `logback-spring.xml` for each service**:
- ✅ `eureka-server/src/main/resources/logback-spring.xml`
- ✅ `api-gateway/src/main/resources/logback-spring.xml`
- ✅ `auth-service/src/main/resources/logback-spring.xml`
- ✅ `labels-service/src/main/resources/logback-spring.xml`
- ✅ `notes-service/src/main/resources/logback-spring.xml`
- ✅ `media-service/src/main/resources/logback-spring.xml`

**Each logback-spring.xml includes**:
- Console Appender (JSON format)
- File Appender (Rolling, 7-day retention)
- Logstash TCP Appender (localhost:5044)
- Async wrapper for non-blocking logging
- Service-specific custom fields

### 3. Application Properties Updated

**Added logging configuration to all services**:
```properties
logging.file.path=./logs
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
```

### 4. ELK Stack Configuration

**Filebeat Configuration** (`filebeat/filebeat.yml`):
- Created configuration to read from `./backend/logs/*.log`
- Configured to send logs to Logstash
- NDJSON parser for JSON logs

**Docker Compose Updates** (`compose.yaml`):
- Updated Filebeat volume mounts
- Mounted filebeat.yml configuration
- Mounted backend/logs directory

**Logstash Configuration** (`logstash/logstash.conf`):
- Already configured to receive on port 5044
- Sends to Elasticsearch with index pattern: `springboot-logs-YYYY.MM.dd`

### 5. Directory Structure

**Created**:
- `backend/logs/` - Directory for log files

### 6. Management Scripts

**Created Management Scripts**:
- ✅ `manage-elk.ps1` - Start/stop/monitor ELK stack
- ✅ `test-elk-integration.ps1` - Test ELK integration

### 7. Documentation

**Created Documentation Files**:
- ✅ `backend/ELK-INTEGRATION-GUIDE.md` - Comprehensive guide
- ✅ `ELK-QUICKSTART.md` - Quick reference guide
- ✅ `ELK-CONFIGURATION-SUMMARY.md` - This file

## 🎯 How It Works

### Log Flow

```
┌─────────────────────────────────────────────────────────────────┐
│  Spring Boot Microservices                                      │
├─────────────────────────────────────────────────────────────────┤
│  1. Application generates logs                                  │
│  2. Logback captures logs                                       │
│  3. Three destinations:                                         │
│     a) Console (JSON format)                                    │
│     b) File (./logs/[service-name].log)                        │
│     c) Logstash TCP (localhost:5044)                           │
└────────────────┬──────────────────────────────┬─────────────────┘
                 │                              │
                 │ (Direct TCP)                 │ (File)
                 ↓                              ↓
         ┌───────────────┐              ┌──────────────┐
         │   Logstash    │←─────────────│   Filebeat   │
         │   Port 5044   │              │              │
         └───────┬───────┘              └──────────────┘
                 │
                 ↓ (HTTP)
         ┌───────────────┐
         │ Elasticsearch │
         │   Port 9200   │
         └───────┬───────┘
                 │
                 ↓ (Read)
         ┌───────────────┐
         │    Kibana     │
         │   Port 5601   │
         └───────────────┘
```

### Log Format

Each log entry is structured JSON:

```json
{
  "@timestamp": "2025-12-29T03:23:45.123Z",
  "@version": "1",
  "message": "User authenticated successfully",
  "logger_name": "com.rajan.keep.controller.AuthController",
  "thread_name": "http-nio-8081-exec-1",
  "level": "INFO",
  "level_value": 20000,
  "service": "auth-service",
  "stack_trace": "...",
  "tags": []
}
```

### Service Configuration

| Service        | Port | Log File           | Logstash Host  |
|---------------|------|--------------------|----------------|
| eureka-server | 8761 | eureka-server.log  | localhost:5044 |
| api-gateway   | 8080 | api-gateway.log    | localhost:5044 |
| auth-service  | 8081 | auth-service.log   | localhost:5044 |
| labels-service| 8082 | labels-service.log | localhost:5044 |
| notes-service | 8083 | notes-service.log  | localhost:5044 |
| media-service | 8084 | media-service.log  | localhost:5044 |

## 🚀 Usage

### Step 1: Start ELK Stack

```powershell
cd "D:\Internal\Google Keep"
.\manage-elk.ps1 -Action start
```

Or:
```powershell
docker-compose up -d
```

### Step 2: Start Spring Boot Services

```powershell
cd "D:\Internal\Google Keep\backend"
.\start-all-services.ps1
```

### Step 3: Verify Integration

```powershell
cd "D:\Internal\Google Keep"
.\test-elk-integration.ps1
```

### Step 4: View Logs in Kibana

1. Open: http://localhost:5601
2. Create index pattern: `springboot-logs-*`
3. Go to Discover
4. View real-time logs

## 📊 Features

### ✅ Implemented

- [x] JSON-formatted logs
- [x] Direct TCP streaming to Logstash
- [x] File-based logging with rotation
- [x] Async logging (non-blocking)
- [x] Service identification in logs
- [x] Centralized log aggregation
- [x] Real-time log viewing in Kibana
- [x] 7-day file retention
- [x] Filebeat backup collection
- [x] Management scripts
- [x] Testing script
- [x] Comprehensive documentation

### 🎯 Benefits

1. **Centralized Logging**: All microservices log to one place
2. **Real-time Monitoring**: View logs as they happen
3. **Powerful Search**: Full-text search across all logs
4. **Visualization**: Create charts and dashboards
5. **Debugging**: Quickly find errors and issues
6. **Performance**: Async logging doesn't block application
7. **Retention**: Automatic cleanup of old logs
8. **Resilience**: Multiple log destinations (file + Logstash)

## 🔧 Configuration Details

### Logback Features Used

- **LogstashEncoder**: Formats logs as JSON
- **RollingFileAppender**: Rotates log files daily
- **AsyncAppender**: Non-blocking log writing
- **Custom Fields**: Adds service name to every log

### Logstash Features Used

- **TCP Input**: Receives logs from applications
- **JSON Codec**: Parses JSON logs
- **Date Filter**: Ensures correct timestamps
- **Elasticsearch Output**: Stores in searchable index

### Elasticsearch Features Used

- **Daily Indices**: `springboot-logs-YYYY.MM.dd`
- **Single Node**: Development mode
- **No Security**: Easy local development

### Filebeat Features Used

- **Filestream Input**: Reads log files
- **NDJSON Parser**: Parses JSON logs
- **Logstash Output**: Sends to Logstash

## 📁 File Structure

```
D:\Internal\Google Keep\
├── compose.yaml                           # Docker Compose config
├── manage-elk.ps1                         # ELK management script
├── test-elk-integration.ps1               # Integration test script
├── ELK-QUICKSTART.md                      # Quick start guide
├── ELK-CONFIGURATION-SUMMARY.md           # This file
│
├── filebeat/
│   └── filebeat.yml                       # Filebeat config
│
├── logstash/
│   └── logstash.conf                      # Logstash pipeline
│
└── backend/
    ├── logs/                              # Log files directory
    │   ├── eureka-server.log             # (created at runtime)
    │   ├── api-gateway.log               # (created at runtime)
    │   ├── auth-service.log              # (created at runtime)
    │   ├── labels-service.log            # (created at runtime)
    │   ├── notes-service.log             # (created at runtime)
    │   └── media-service.log             # (created at runtime)
    │
    ├── ELK-INTEGRATION-GUIDE.md           # Detailed guide
    │
    ├── eureka-server/
    │   └── src/main/resources/
    │       ├── logback-spring.xml         # ✓ Created
    │       └── application.properties      # ✓ Updated
    │
    ├── api-gateway/
    │   └── src/main/resources/
    │       ├── logback-spring.xml         # ✓ Created
    │       └── application.properties      # ✓ Updated
    │
    ├── auth-service/
    │   └── src/main/resources/
    │       ├── logback-spring.xml         # ✓ Created
    │       └── application.properties      # ✓ Updated
    │
    ├── labels-service/
    │   └── src/main/resources/
    │       ├── logback-spring.xml         # ✓ Created
    │       └── application.properties      # ✓ Updated
    │
    ├── notes-service/
    │   └── src/main/resources/
    │       ├── logback-spring.xml         # ✓ Created
    │       └── application.properties      # ✓ Updated
    │
    └── media-service/
        └── src/main/resources/
            ├── logback-spring.xml         # ✓ Created
            └── application.properties      # ✓ Updated
```

## 🎓 Key Concepts

### Logback
- **Open-source logging framework** for Java
- **Successor to Log4j**
- **Fast and small footprint**
- **Extensive configuration options**

### Logstash
- **Data processing pipeline**
- **Ingests data from multiple sources**
- **Transforms and sends to Elasticsearch**

### Elasticsearch
- **Distributed search engine**
- **RESTful API**
- **Near real-time search**
- **Scalable and highly available**

### Kibana
- **Visualization tool**
- **Data exploration**
- **Dashboard creation**
- **User-friendly interface**

### Filebeat
- **Lightweight log shipper**
- **Monitors log files**
- **Forwards to Logstash/Elasticsearch**
- **Low resource usage**

## 📋 Checklist

Before using the ELK integration, ensure:

- [ ] Docker is installed and running
- [ ] Docker Compose is available
- [ ] All logback-spring.xml files are created
- [ ] All application.properties are updated
- [ ] Logs directory exists and is writable
- [ ] Filebeat configuration is in place
- [ ] Logstash configuration is in place
- [ ] Port 5044 is available (Logstash)
- [ ] Port 9200 is available (Elasticsearch)
- [ ] Port 5601 is available (Kibana)

## 🐛 Common Issues

### Issue 1: Port Already in Use

**Solution**: Stop conflicting service or change port in compose.yaml

```powershell
# Find process using port 5044
Get-NetTCPConnection -LocalPort 5044
# Kill process if needed
Stop-Process -Id <PID>
```

### Issue 2: Logs Not Appearing

**Causes**:
- ELK stack not running
- Spring Boot service not started
- Logstash connection failed
- Elasticsearch out of memory

**Solution**: Run test script
```powershell
.\test-elk-integration.ps1
```

### Issue 3: Elasticsearch Out of Memory

**Solution**: Increase heap size in compose.yaml
```yaml
environment:
  - ES_JAVA_OPTS=-Xms2g -Xmx2g
```

## 📈 Monitoring

### Metrics to Watch

1. **Log Ingestion Rate**: Logs/second
2. **Elasticsearch Disk**: % used
3. **Logstash Performance**: Processing time
4. **Error Count**: By service

### Alerts to Configure

1. Error rate > threshold
2. Disk space < 10%
3. Service not logging for > 5 minutes
4. Response time > threshold

## 🔐 Security (Production)

For production, implement:

1. **Elasticsearch Security**: Enable X-Pack
2. **TLS/SSL**: Encrypt traffic
3. **Authentication**: Username/password
4. **Authorization**: Role-based access
5. **Network Security**: Firewall rules
6. **Audit Logging**: Track access

## 📚 Next Steps

1. **Start ELK Stack**: `.\manage-elk.ps1 -Action start`
2. **Test Integration**: `.\test-elk-integration.ps1`
3. **Start Services**: `.\start-all-services.ps1`
4. **Create Kibana Dashboard**: Visualize key metrics
5. **Set Up Alerts**: Monitor critical errors
6. **Regular Maintenance**: Clean old logs

## 🎉 Summary

Your Spring Boot microservices are now fully integrated with the ELK stack for centralized logging and monitoring. All logs are:

- ✅ Formatted as JSON
- ✅ Streamed to Logstash in real-time
- ✅ Stored in Elasticsearch
- ✅ Viewable in Kibana
- ✅ Backed up to files
- ✅ Automatically rotated
- ✅ Tagged with service names

**Happy Logging! 🚀**

---

For more information:
- **Quick Start**: See `ELK-QUICKSTART.md`
- **Detailed Guide**: See `backend/ELK-INTEGRATION-GUIDE.md`
- **Scripts**: Use `manage-elk.ps1` and `test-elk-integration.ps1`

