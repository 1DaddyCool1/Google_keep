# 🎯 ELK Stack Integration - Complete Setup

## ✅ Configuration Complete!

Your Spring Boot microservices are now fully configured to stream logs to the ELK stack.

---

## 🚀 Quick Start (5 Steps)

### Step 1: Rebuild Services (First Time Only)

The Logstash dependency has been added to all services. Rebuild them:

```powershell
cd "D:\Internal\Google Keep\backend"
.\rebuild-with-elk.ps1
```

Or manually with Maven:
```powershell
cd "D:\Internal\Google Keep\backend"
mvn clean install -DskipTests -f parent-pom.xml
```

### Step 2: Start ELK Stack

```powershell
cd "D:\Internal\Google Keep"
.\manage-elk.ps1 -Action start
```

Wait 10-15 seconds for all services to start.

### Step 3: Verify ELK Integration

```powershell
.\test-elk-integration.ps1
```

This will check:
- ✓ ELK services are running
- ✓ Logs directory is writable
- ✓ Logback configurations exist
- ✓ Elasticsearch is indexing
- ✓ Logstash port is listening

### Step 4: Start Spring Boot Services

```powershell
cd "D:\Internal\Google Keep\backend"
.\start-all-services.ps1
```

### Step 5: View Logs in Kibana

1. Open browser: **http://localhost:5601**
2. Go to **Management → Stack Management → Index Patterns**
3. Click **"Create index pattern"**
4. Enter: `springboot-logs-*`
5. Click **"Next step"**
6. Select **@timestamp** as time field
7. Click **"Create index pattern"**
8. Go to **Analytics → Discover** to view logs

---

## 📋 What Was Configured

### ✅ All Microservices

- Added **Logstash Logback Encoder** dependency
- Created **logback-spring.xml** with:
  - Console logging (JSON format)
  - File logging (rotating, 7-day retention)
  - Logstash TCP streaming (localhost:5044)
  - Async appenders (non-blocking)
- Updated **application.properties** with logging configuration

### ✅ Services Configured

| Service        | Port | Log File           |
|---------------|------|--------------------|
| eureka-server | 8761 | eureka-server.log  |
| api-gateway   | 8080 | api-gateway.log    |
| auth-service  | 8081 | auth-service.log   |
| labels-service| 8082 | labels-service.log |
| notes-service | 8083 | notes-service.log  |
| media-service | 8084 | media-service.log  |

### ✅ ELK Stack

- **Elasticsearch** (9200): Stores and indexes logs
- **Logstash** (5044): Receives logs from applications
- **Kibana** (5601): Visualizes and explores logs
- **Filebeat**: Backup log collection from files

---

## 📁 Files Created

```
D:\Internal\Google Keep\
│
├── ELK-QUICKSTART.md                   ← Quick start guide
├── ELK-CONFIGURATION-SUMMARY.md        ← What was configured
├── ELK-QUICK-REFERENCE.txt             ← Quick reference card
│
├── manage-elk.ps1                      ← Manage ELK stack
├── test-elk-integration.ps1            ← Test integration
├── compose.yaml                        ← Updated with filebeat
│
├── filebeat/
│   └── filebeat.yml                    ← Created
│
├── logstash/
│   └── logstash.conf                   ← Already existed
│
└── backend/
    ├── logs/                           ← Created (log files)
    ├── rebuild-with-elk.ps1            ← Rebuild script
    ├── ELK-INTEGRATION-GUIDE.md        ← Comprehensive guide
    │
    └── [each-service]/
        └── src/main/resources/
            ├── logback-spring.xml      ← Created
            └── application.properties  ← Updated
```

---

## 🛠️ Management Scripts

### manage-elk.ps1

Main script to manage the ELK stack:

```powershell
# Start ELK stack
.\manage-elk.ps1 -Action start

# Stop ELK stack
.\manage-elk.ps1 -Action stop

# Restart ELK stack
.\manage-elk.ps1 -Action restart

# Check status
.\manage-elk.ps1 -Action status

# View container logs
.\manage-elk.ps1 -Action logs

# Clean old logs
.\manage-elk.ps1 -Action clean
```

### test-elk-integration.ps1

Tests the ELK integration:

```powershell
.\test-elk-integration.ps1
```

Runs 7 tests:
1. ✓ ELK services running
2. ✓ Logs directory writable
3. ✓ Logback configurations exist
4. ✓ Filebeat configuration exists
5. ✓ Elasticsearch indexing works
6. ✓ Logstash port listening
7. ✓ Docker containers status

### rebuild-with-elk.ps1

Rebuilds all services with ELK dependencies:

```powershell
cd backend
.\rebuild-with-elk.ps1
```

Options:
1. Clean & Install (recommended first time)
2. Install only
3. Compile only

---

## 📊 Log Format

Each log entry is JSON:

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

**Benefits:**
- ✅ Structured and searchable
- ✅ Service name tagged
- ✅ Timestamp normalized
- ✅ Stack traces included
- ✅ Thread information

---

## 🔍 Using Kibana

### Create Index Pattern (First Time)

1. Open: http://localhost:5601
2. Management → Stack Management → Index Patterns
3. Create index pattern: `springboot-logs-*`
4. Select time field: `@timestamp`

### Search Logs

Go to **Analytics → Discover** and use these queries:

```
# By service
service: "notes-service"

# By log level
level: "ERROR"
level: "WARN"

# Search messages
message: *authentication*

# Combine filters
service: "auth-service" AND level: "ERROR"

# Time range
Use time picker (top-right) - Last 15 min, 1 hour, 24 hours, etc.
```

### Create Visualizations

1. **Log Count by Service**: Bar chart
2. **Error Rate**: Line chart over time
3. **Top Errors**: Data table
4. **Service Health**: Metrics

---

## 🐛 Troubleshooting

### Logs Not Appearing?

**Run the test script:**
```powershell
.\test-elk-integration.ps1
```

**Common issues:**
- ELK stack not running → `.\manage-elk.ps1 -Action start`
- Spring Boot not started → `.\backend\start-all-services.ps1`
- Port 5044 in use → `netstat -ano | Select-String "5044"`
- Firewall blocking → Check Windows Firewall

### Elasticsearch Out of Memory?

Edit `compose.yaml`:
```yaml
environment:
  - ES_JAVA_OPTS=-Xms2g -Xmx2g  # Increase from 1g to 2g
```

Then restart:
```powershell
docker-compose restart elasticsearch
```

### Filebeat Not Working?

```powershell
# Check logs
docker logs filebeat

# Verify configuration
docker exec filebeat filebeat test config

# Restart
docker-compose restart filebeat
```

### Services Can't Connect to Logstash?

```powershell
# Check if Logstash is listening
netstat -ano | Select-String "5044"

# Check Logstash logs
docker logs logstash

# Verify logback-spring.xml has correct host
# Should be: <destination>localhost:5044</destination>
```

---

## 📈 Monitoring

### Health Checks

```powershell
# Elasticsearch
curl http://localhost:9200/_cluster/health?pretty

# Kibana
curl http://localhost:5601/api/status

# Logstash (via Docker)
docker exec logstash curl -s localhost:9600/?pretty
```

### View Indices

```powershell
# List all indices
curl http://localhost:9200/_cat/indices?v

# Search logs
curl http://localhost:9200/springboot-logs-*/_search?pretty
```

---

## 🎓 Understanding the Flow

```
┌─────────────────────────────────────┐
│   Spring Boot Application           │
│   - Generates logs                  │
│   - Logback captures                │
└─────────┬───────────────────────────┘
          │
          ├─→ Console (JSON)
          ├─→ File (./logs/service.log)
          └─→ Logstash TCP (5044)
                │
                ↓
        ┌───────────────┐
        │   Logstash    │  ←─── Filebeat (backup)
        │   Port 5044   │       reads from files
        └───────┬───────┘
                │
                ↓
        ┌───────────────┐
        │ Elasticsearch │
        │   Port 9200   │
        └───────┬───────┘
                │
                ↓
        ┌───────────────┐
        │    Kibana     │
        │   Port 5601   │
        └───────────────┘
```

---

## 🎯 Daily Workflow

### Morning Startup

```powershell
# 1. Start ELK
cd "D:\Internal\Google Keep"
.\manage-elk.ps1 -Action start

# 2. Wait 10-15 seconds

# 3. Start services
cd backend
.\start-all-services.ps1

# 4. Open Kibana
start http://localhost:5601
```

### During Development

- View real-time logs in Kibana
- Search for errors: `level: "ERROR"`
- Filter by service: `service: "notes-service"`
- Check specific operations: `message: *created*`

### Evening Shutdown

```powershell
# Stop services
cd backend
.\stop-all-services.ps1

# Stop ELK (optional - can keep running)
cd ..
docker-compose down
```

---

## 📚 Documentation

| File | Description |
|------|-------------|
| `ELK-QUICK-REFERENCE.txt` | One-page reference |
| `ELK-QUICKSTART.md` | Quick start guide |
| `ELK-CONFIGURATION-SUMMARY.md` | What was configured |
| `backend/ELK-INTEGRATION-GUIDE.md` | Complete guide |

---

## 🔐 Security Note

**Current configuration is for DEVELOPMENT only.**

For production:
- ✅ Enable Elasticsearch security (X-Pack)
- ✅ Use TLS/SSL for all connections
- ✅ Add authentication (username/password)
- ✅ Restrict network access
- ✅ Use environment variables for secrets
- ✅ Enable audit logging

---

## 💡 Best Practices

1. **Monitor disk space** - Elasticsearch grows quickly
2. **Regular cleanup** - Delete old indices (configured for 7 days)
3. **Use structured logging** - Already configured with JSON
4. **Tag your logs** - Service name is already tagged
5. **Set up alerts** - For critical errors in production
6. **Create dashboards** - For common monitoring tasks
7. **Use log levels wisely**:
   - DEBUG: Development only
   - INFO: General information
   - WARN: Potential issues
   - ERROR: Actual errors

---

## 🎉 You're All Set!

Your Google Keep microservices now have:
- ✅ Centralized logging
- ✅ Real-time log streaming
- ✅ Powerful search capabilities
- ✅ Beautiful visualizations
- ✅ Easy troubleshooting
- ✅ Production-ready logging

### Next Steps

1. **Rebuild services**: `.\backend\rebuild-with-elk.ps1`
2. **Start ELK**: `.\manage-elk.ps1 -Action start`
3. **Test setup**: `.\test-elk-integration.ps1`
4. **Start services**: `.\backend\start-all-services.ps1`
5. **Open Kibana**: http://localhost:5601
6. **Create dashboards**: Monitor your applications

---

## 🆘 Need Help?

1. **Run test script**: `.\test-elk-integration.ps1`
2. **Check status**: `.\manage-elk.ps1 -Action status`
3. **View logs**: `.\manage-elk.ps1 -Action logs`
4. **Read documentation**: See `ELK-INTEGRATION-GUIDE.md`

---

**Happy Logging! 🚀**

For questions or issues, check the comprehensive guide:
`backend/ELK-INTEGRATION-GUIDE.md`

