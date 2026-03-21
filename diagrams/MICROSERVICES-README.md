# Google Keep - Microservices Architecture

This project has been decomposed from a monolithic application into a microservices architecture using Spring Boot and Spring Cloud.

## Architecture Overview

The application consists of the following microservices:

### 1. **Eureka Server** (Port: 8761)
- **Purpose**: Service Discovery and Registration
- **Technology**: Spring Cloud Netflix Eureka
- **URL**: http://localhost:8761
- All microservices register with Eureka for dynamic service discovery

### 2. **API Gateway** (Port: 8080)
- **Purpose**: Single entry point for all client requests
- **Technology**: Spring Cloud Gateway
- **Routes**:
  - `/api/auth/**` → Auth Service
  - `/api/labels/**` → Labels Service
  - `/api/notes/**` → Notes Service
  - `/api/media/**` → Media Service
- **Features**:
  - Load balancing
  - CORS configuration
  - Centralized routing

### 3. **Auth Service** (Port: 8081)
- **Purpose**: User authentication and authorization
- **Database**: `google_keep_auth_db`
- **Endpoints**:
  - `POST /api/auth/signup` - User registration
  - `POST /api/auth/login` - User login
- **Responsibilities**:
  - User management
  - JWT token generation
  - Password encryption
  - Role management

### 4. **Labels Service** (Port: 8082)
- **Purpose**: Label/tag management for notes
- **Database**: `google_keep_labels_db`
- **Endpoints**:
  - `GET /api/labels` - Get all labels for current user
  - `POST /api/labels` - Create new label
  - `PUT /api/labels/{id}` - Update label
  - `DELETE /api/labels/{id}` - Delete label
- **Inter-Service Communication**:
  - Calls Notes Service when deleting labels to remove associations

### 5. **Notes Service** (Port: 8083)
- **Purpose**: Core note management functionality
- **Database**: `google_keep_notes_db`
- **Endpoints**:
  - `GET /api/notes` - List all notes (with optional label filter)
  - `POST /api/notes` - Create new note
  - `GET /api/notes/{id}` - Get specific note
  - `PUT /api/notes/{id}` - Update note
  - `DELETE /api/notes/{id}` - Delete note
  - `PUT /api/notes/{id}/labels` - Attach labels to note
  - `POST /api/notes/{id}/copy` - Copy/duplicate note
  - `DELETE /api/notes/internal/remove-label/{labelId}` - Internal endpoint for label cleanup
- **Inter-Service Communication**:
  - Calls Media Service for image upload, copy, and deletion
  - Receives calls from Labels Service for label cleanup

### 6. **Media Service** (Port: 8084)
- **Purpose**: Image/media storage and retrieval
- **Database**: `google_keep_media_db`
- **Endpoints**:
  - `POST /api/media/upload` - Upload single image
  - `POST /api/media/upload-batch` - Upload multiple images
  - `GET /api/media/{id}` - Get image by ID
  - `POST /api/media/{id}/copy` - Copy image
  - `DELETE /api/media/{id}` - Delete image
- **Storage**: File system at `D:/Internal/Google Keep/S3/Images`

## Technology Stack

- **Framework**: Spring Boot 3.4.1
- **Language**: Java 21
- **Service Discovery**: Spring Cloud Netflix Eureka
- **API Gateway**: Spring Cloud Gateway
- **Database**: PostgreSQL (separate DB per service)
- **Security**: JWT-based authentication
- **Build Tool**: Maven

## Database Setup

Create the following PostgreSQL databases:

```sql
CREATE DATABASE google_keep_auth_db;
CREATE DATABASE google_keep_labels_db;
CREATE DATABASE google_keep_notes_db;
CREATE DATABASE google_keep_media_db;
```

## Running the Application

### Prerequisites
- Java 21
- Maven
- PostgreSQL

### Startup Sequence

**Important**: Services must be started in this order:

1. **Start Eureka Server** (Service Discovery must be up first)
   ```bash
   cd eureka-server
   mvn spring-boot:run
   ```
   Wait for: `Eureka Server started on port 8761`

2. **Start API Gateway**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```
   Wait for: Gateway registered with Eureka

3. **Start Microservices** (Can be started in parallel)
   
   Terminal 1 - Auth Service:
   ```bash
   cd auth-service
   mvn spring-boot:run
   ```
   
   Terminal 2 - Labels Service:
   ```bash
   cd labels-service
   mvn spring-boot:run
   ```
   
   Terminal 3 - Notes Service:
   ```bash
   cd notes-service
   mvn spring-boot:run
   ```
   
   Terminal 4 - Media Service:
   ```bash
   cd media-service
   mvn spring-boot:run
   ```

### Verification

1. **Check Eureka Dashboard**: http://localhost:8761
   - All 4 services (auth-service, labels-service, notes-service, media-service) should be registered

2. **Test API Gateway**: http://localhost:8080
   - All requests should go through the gateway

## Service Communication

### Synchronous Communication (REST)
- **Notes Service → Media Service**: Image operations
- **Labels Service → Notes Service**: Label cleanup on deletion

### Communication Pattern
- Uses `RestTemplate` with `@LoadBalanced` annotation
- Service discovery through Eureka
- Example: `http://media-service/api/media/upload`

## Data Architecture

### Database Per Service Pattern
Each microservice has its own database to ensure:
- Data isolation
- Independent scaling
- Technology flexibility
- Easier maintenance

### Data Denormalization
- **Labels Service**: Stores `username` directly (not FK to User table)
- **Notes Service**: Stores `username` and references to `mediaIds` and `labelIds`
- **Media Service**: Stores `username` directly

This approach avoids cross-service database joins and maintains service independence.

## Security

### JWT Authentication
- JWT tokens generated by Auth Service
- All services validate JWT tokens using the same secret
- Token contains:
  - Username
  - Roles
  - Expiration time

### Security Filter
Each service (except Auth Service endpoints) requires:
- Valid JWT token in `Authorization: Bearer <token>` header
- User authentication through Spring Security

## API Usage Examples

### 1. User Signup
```bash
POST http://localhost:8080/api/auth/signup
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "password123"
}
```

### 2. User Login
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "john",
  "password": "password123"
}

Response:
{
  "user": { "id": "1", "username": "john", "email": "john@example.com" },
  "token": "eyJhbGc..."
}
```

### 3. Create Label
```bash
POST http://localhost:8080/api/labels
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Personal"
}
```

### 4. Create Note
```bash
POST http://localhost:8080/api/notes
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "My Note",
  "content": "<p>Note content</p>",
  "images": ["data:image/png;base64,iVBORw0KG..."]
}
```

### 5. Get Notes by Label
```bash
GET http://localhost:8080/api/notes?labelId=1
Authorization: Bearer <token>
```

## Advantages of Microservices Architecture

### 1. **Independent Deployment**
- Each service can be deployed independently
- No need to redeploy entire application for small changes

### 2. **Scalability**
- Scale individual services based on demand
- Example: Scale Media Service separately during high upload traffic

### 3. **Technology Flexibility**
- Each service can use different technologies
- Example: Media Service could use MongoDB or S3 in the future

### 4. **Fault Isolation**
- Failure in one service doesn't bring down entire application
- Example: Media Service down doesn't affect note creation (just without images)

### 5. **Team Organization**
- Different teams can work on different services
- Clear service boundaries and responsibilities

## Challenges & Solutions

### 1. **Distributed Transactions**
- **Challenge**: Deleting a label requires updating notes
- **Solution**: Labels Service calls Notes Service API for cleanup

### 2. **Data Consistency**
- **Challenge**: Multiple databases, no ACID across services
- **Solution**: Eventual consistency, compensating transactions

### 3. **Service Discovery**
- **Challenge**: Services need to find each other
- **Solution**: Eureka Server for dynamic service registration

### 4. **Network Latency**
- **Challenge**: Inter-service calls add latency
- **Solution**: Denormalized data, caching strategies

## Future Enhancements

1. **Message Queue (RabbitMQ/Kafka)**
   - Asynchronous communication for label deletion events
   - Better fault tolerance

2. **Config Server**
   - Centralized configuration management
   - Dynamic configuration updates

3. **Circuit Breaker (Resilience4j)**
   - Fault tolerance
   - Graceful degradation

4. **API Documentation (Swagger/OpenAPI)**
   - Auto-generated API documentation

5. **Distributed Tracing (Sleuth + Zipkin)**
   - Request tracking across services
   - Performance monitoring

6. **Containerization (Docker + Kubernetes)**
   - Easy deployment
   - Container orchestration

## Monitoring

### Eureka Dashboard
- URL: http://localhost:8761
- Shows all registered services
- Service health status

### Logs
Each service logs to console:
- SQL queries (when `spring.jpa.show-sql=true`)
- HTTP requests
- Errors and exceptions

## Port Reference

| Service | Port |
|---------|------|
| Eureka Server | 8761 |
| API Gateway | 8080 |
| Auth Service | 8081 |
| Labels Service | 8082 |
| Notes Service | 8083 |
| Media Service | 8084 |

## Database Reference

| Service | Database |
|---------|----------|
| Auth Service | google_keep_auth_db |
| Labels Service | google_keep_labels_db |
| Notes Service | google_keep_notes_db |
| Media Service | google_keep_media_db |

## Troubleshooting

### Service not registering with Eureka
- Check if Eureka Server is running
- Verify `eureka.client.service-url.defaultZone` in application.properties
- Check network connectivity

### 404 Not Found from Gateway
- Verify service is registered in Eureka
- Check route configuration in API Gateway
- Ensure service is running

### JWT Authentication Failed
- Verify same JWT secret across all services
- Check token expiration
- Ensure proper Authorization header format

### Inter-service communication fails
- Check if target service is registered in Eureka
- Verify `@LoadBalanced` annotation on RestTemplate
- Check service name in URL (e.g., `http://media-service/...`)

## Contributing

When adding new features:
1. Identify the appropriate service
2. If new domain, consider creating a new microservice
3. Update API Gateway routes if needed
4. Document inter-service communication
5. Update this README

## License

[Your License Here]

