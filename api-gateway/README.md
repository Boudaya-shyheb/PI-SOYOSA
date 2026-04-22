# EnglishWay API Gateway

## Overview

The API Gateway serves as the single entry point for all microservices in the EnglishWay platform. Built with Spring Cloud Gateway, it provides intelligent routing, load balancing, and centralized CORS configuration.

## Key Features

- **Unified Entry Point**: Single endpoint for all backend services
- **Intelligent Routing**: Path-based routing to appropriate microservices
- **CORS Management**: Centralized CORS configuration for all services
- **Load Balancing**: Distribute requests across service instances
- **Health Monitoring**: Actuator endpoints for system health checks
- **Scalability**: Easy addition of new microservices

## Architecture

The Gateway routes requests from the frontend to various microservices based on URL patterns:

```
Client (http://localhost:4200)
    ↓
API Gateway (http://localhost:8080)
    ↓
    ├── /api/courses/** → Course Service (8081)
    ├── /api/chapters/** → Course Service (8081)
    ├── /api/lessons/** → Course Service (8081)
    ├── /api/quizzes/** → Course Service (8081)
    └── [Future routes...]
```

## Configuration

### Server Port
```yaml
server:
  port: 8080
```

### Route Configuration

Routes are defined in `application.yml`:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: course-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/courses/**,/api/chapters/**,/api/lessons/**,...
          filters:
            - RewritePath=/api/(?<segment>.*), /${segment}
```

### CORS Configuration

Global CORS settings allow requests from the Angular frontend:

```yaml
globalcors:
  corsConfigurations:
    '[/**]':
      allowedOrigins: 
        - "http://localhost:4200"
      allowedMethods:
        - GET, POST, PUT, DELETE, PATCH, OPTIONS
      allowedHeaders: "*"
      allowCredentials: true
```

## Running the Gateway

### Prerequisites
- Java 17+
- Maven 3.8+

### Start the Service

```bash
cd Backend/api-gateway
mvn clean install
mvn spring-boot:run
```

The gateway will start on `http://localhost:8080`

### Verify Health

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Adding New Microservices

To add a new microservice route:

1. Update `application.yml`:
```yaml
routes:
  - id: new-service
    uri: http://localhost:8082
    predicates:
      - Path=/api/new-service/**
    filters:
      - RewritePath=/api/(?<segment>.*), /${segment}
```

2. Restart the gateway
3. Access via `http://localhost:8080/api/new-service/**`

## Monitoring

### Available Actuator Endpoints

- Health: `http://localhost:8080/actuator/health`
- Info: `http://localhost:8080/actuator/info`
- Gateway Routes: `http://localhost:8080/actuator/gateway/routes`

## Dependencies

- Spring Cloud Gateway
- Spring Boot Actuator
- Spring Cloud LoadBalancer
- Lombok

## Logging

Debug logging is enabled for gateway routing:

```yaml
logging:
  level:
    org.springframework.cloud.gateway: DEBUG
    reactor.netty: DEBUG
```

## Security Considerations

- CORS is configured to allow specific origins
- Add authentication/authorization filters as needed
- Consider rate limiting for production
- Implement circuit breakers for fault tolerance

## Troubleshooting

### Gateway doesn't start
- Ensure port 8080 is not in use
- Check Java version (requires 17+)

### Routes not working
- Verify downstream service is running
- Check route predicates match request path
- Review gateway logs for routing details

### CORS errors
- Verify origin is in `allowedOrigins` list
- Check that credentials are properly configured
- Ensure preflight OPTIONS requests are handled

## Future Enhancements

- [ ] JWT Authentication Filter
- [ ] Rate Limiting
- [ ] Circuit Breaker Integration
- [ ] Service Discovery (Eureka/Consul)
- [ ] Request/Response Logging
- [ ] API Versioning Support
- [ ] Distributed Tracing (Sleuth/Zipkin)

---

**Version**: 0.0.1-SNAPSHOT  
**Spring Boot**: 3.2.2  
**Spring Cloud**: 2023.0.1  
