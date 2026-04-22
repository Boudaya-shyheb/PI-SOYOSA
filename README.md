# EnglishWay Full Application

EnglishWay is a full-stack microservices application for English learning, with courses, users, blogs, training, games, messaging, tracking, events, and ecommerce features.

This repository is a monorepo. Services communicate through Spring Cloud and are exposed through one API Gateway.

## Architecture Summary

- Service discovery: Eureka Server on port 8761
- Single backend entry point: API Gateway on port 8092
- Routing style: service discovery only (lb://), no direct http:// or https:// route targets
- Frontend: Angular app in Frontend
- Optional analytics worker: Node.js service in ecommerce-insights-worker

## Repository Modules

- api-gateway: Spring Cloud Gateway
- eureka-server: Eureka registry
- courses-microservice: Course domain
- user-microservice: Users and auth
- blog-microservice: Blogs
- training-service: Training features
- messageService: Messaging
- gameService: Games
- TrackingMS: Tracking and websocket endpoints
- PI_events: Events and clubs
- ecommerce-microservice: Ecommerce features
- Frontend: Angular client
- ecommerce-insights-worker: Node.js worker

## Service Matrix

| Module | Service Name | Port | Eureka Client | Notes |
|---|---|---:|---|---|
| eureka-server | eureka-server | 8761 | No (registry only) | Dashboard and registry |
| api-gateway | englishway-api-gateway | 8092 | Yes | Public backend entry point |
| courses-microservice | englishway-course-management | 8081 | Yes | Courses, chapters, lessons, quizzes, etc. |
| user-microservice | userService | 8070 | Yes | User and auth endpoints |
| blog-microservice | Blog | 8072 | Yes | Blog endpoints |
| training-service | training-service | 8082 | Yes | Training endpoints |
| messageService | messageService | 8083 | Yes | Message and chat endpoints |
| gameService | gameService | 8084 | Yes | Game endpoints |
| ecommerce-microservice | ecommerce-service | 8085 | Yes | Product/order/cart/etc. |
| PI_events | pi-events | 8086 | Yes | Events/clubs/statistics |
| TrackingMS | tracking-service | 8087 | Yes | Tracking and websocket |

## Gateway Route Policy

Gateway routes are configured in api-gateway/src/main/resources/application.yml.

All route targets must use service discovery URIs:

- Allowed: lb://service-name
- Forbidden: uri: http://...
- Forbidden: uri: https://...

This policy is automatically enforced by:

- api-gateway/src/test/java/com/englishway/gateway/GatewayRoutingPolicyTest.java

Run the policy test:

```powershell
cd api-gateway
mvn -Dtest=GatewayRoutingPolicyTest test
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 18+
- npm 9+
- PostgreSQL running for services that require it
- MySQL running for services that require it
- Kafka optional, depending on feature flags

## Environment and Configuration

Most services support environment overrides.

Common values:

- EUREKA_ENABLED=true
- EUREKA_URL=http://localhost:8761/eureka/

Examples:

- Courses DB: DB_URL, DB_USER, DB_PASSWORD
- Kafka toggles: KAFKA_ENABLED, KAFKA_BOOTSTRAP

Recommendation:

- Use environment variables for secrets in local and CI environments.
- Do not rely on committed default secrets for production.

## Local Startup Order (Recommended)

Open separate terminals from repository root.

1) Start Eureka

```powershell
cd eureka-server
mvn spring-boot:run -DskipTests
```

2) Start backend services that Gateway routes to

```powershell
cd user-microservice
mvn spring-boot:run -DskipTests
```

```powershell
cd courses-microservice
mvn spring-boot:run -DskipTests
```

```powershell
cd training-service
mvn spring-boot:run -DskipTests
```

```powershell
cd blog-microservice
mvn spring-boot:run -DskipTests
```

```powershell
cd messageService
mvn spring-boot:run -DskipTests
```

```powershell
cd gameService
mvn spring-boot:run -DskipTests
```

```powershell
cd ecommerce-microservice
mvn spring-boot:run -DskipTests
```

```powershell
cd PI_events
mvn spring-boot:run -DskipTests
```

```powershell
cd TrackingMS
mvn spring-boot:run -DskipTests
```

3) Start API Gateway (single backend entrypoint)

```powershell
cd api-gateway
mvn spring-boot:run -DskipTests
```

4) Start frontend

```powershell
cd Frontend
npm install
npm start
```

5) Optional: start ecommerce insights worker

```powershell
cd ecommerce-insights-worker
npm install
npm start
```

## Access Points

- Eureka dashboard: http://localhost:8761
- API Gateway base URL: http://localhost:8092
- Frontend default URL: http://localhost:4200

Important:

- Treat API Gateway as the only backend endpoint for clients.
- Client calls should go to Gateway paths, not directly to microservice ports.

## Quick Gateway Smoke Test

Create a course through Gateway:

```powershell
$body = @{
  title = "Gateway README Test"
  description = "Created through API Gateway"
  level = "A1"
  capacity = 20
  active = $true
  isPaid = $false
  price = 0
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri "http://localhost:8092/api/courses" -Headers @{
  "X-User-Id" = "admin-readme"
  "X-Role" = "ADMIN"
} -ContentType "application/json" -Body $body
```

List courses through Gateway:

```powershell
Invoke-RestMethod -Uri "http://localhost:8092/api/courses?page=0&size=5"
```

## Troubleshooting

Port already in use:

- Change the conflicting service port in its config, or free the port.
- For temporary Gateway override:

```powershell
cd api-gateway
mvn spring-boot:run -DskipTests -Dspring-boot.run.arguments=--server.port=8089
```

Service not visible in Eureka:

- Confirm Eureka is running first.
- Confirm EUREKA_ENABLED is true.
- Confirm service uses same defaultZone as Eureka.

Gateway route not working:

- Verify service name in Gateway uri: lb://... matches spring.application.name.
- Re-run GatewayRoutingPolicyTest.

## Development Notes

- Keep service names stable when editing spring.application.name.
- Keep Gateway route IDs and predicates aligned with current APIs.
- Add tests when introducing new Gateway routes or policy changes.

## License

Add your project license details here.
