# Ecommerce Service

Spring Boot microservice for e-commerce product, order, and cart management.

## Prerequisites

- **Java 17** or later
- **Maven 3.8+**
- **MySQL 8.0+** (should be running on localhost:3306)

## Quick Start

### 1. Verify MySQL Connection

Ensure MySQL is running. The database `ecommercedb` will be created automatically by Hibernate.

### 2. Build Project

```powershell
cd C:\Users\YASSINE JOMNI\Documents\trae_projects\TEST\demo

mvn clean package
```

### 3. Run the Service

```powershell
mvn spring-boot:run
```

You should see:
```
Started EcommerceServiceApplication in X.XXX seconds
```

### 4. Verify Service is Running

```powershell
curl http://localhost:8081/actuator/health
```

Expected response:
```json
{
  "status": "UP"
}
```

## Database Configuration

| Property | Value |
|----------|-------|
| Database | ecommerce_db |
| Username | ecommerce_user |
| Password | ecommerce_password |
| Port | 5432 |
| Host | localhost |

## Project Structure

```
ecommerce-service/
├── src/main/
│   ├── java/com/ecommerce/
│   │   ├── EcommerceServiceApplication.java
│   │   ├── config/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── exception/
│   └── resources/
│       ├── application.properties
│       └── db/migration/
│           └── V1__init_schema.sql
└── pom.xml
```

## API Endpoints (TODO)

All endpoints are prefixed with `/api/ecommerce/`

- `GET /api/ecommerce/products` - Get all products
- `GET /api/ecommerce/products/{id}` - Get product by ID
- `POST /api/ecommerce/products` - Create new product
- `PUT /api/ecommerce/products/{id}` - Update product
- `DELETE /api/ecommerce/products/{id}` - Delete product

(More endpoints coming as you implement them)

## Database Tables

Tables created by Flyway migration `V1__init_schema.sql`:

- **categories** - Product categories
- **products** - Product information
- **orders** - Customer orders
- **order_items** - Items in each order
- **carts** - Shopping carts
- **cart_items** - Items in each cart

## Development Mode

The service runs on **port 8081** by default.

### Health Check Endpoint

```
GET http://localhost:8081/actuator/health
```

### Database Logs

To see SQL queries in the console, logs are set to DEBUG level for:
- `com.ecommerce` - Your code
- `org.hibernate.SQL` - Hibernate queries
- `org.springframework.web` - Spring Web requests

## Maven Commands

```powershell
# Clean build
mvn clean package

# Run tests
mvn test

# Run the service
mvn spring-boot:run

# Run specific class
mvn exec:java -Dexec.mainClass="com.ecommerce.EcommerceServiceApplication"

# Install dependencies only
mvn dependency:resolve
```

## Troubleshooting

### Problem: "PostgreSQL connection refused"
- Verify PostgreSQL is running: `psql -U postgres -c "SELECT 1"`
- Check credentials in `application.properties`
- Ensure database and user were created

### Problem: "Flyway migration failed"
- Check if tables already exist: `psql -U ecommerce_user -d ecommerce_db -c "\dt"`
- If tables exist but you want fresh start: Drop and recreate database
  ```powershell
  psql -U postgres -c "DROP DATABASE ecommerce_db;"
  psql -U postgres -c "CREATE DATABASE ecommerce_db;"
  psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;"
  ```

### Problem: "Port 8081 already in use"
- Kill the process using port 8081 or change `server.port` in `application.properties`

### Problem: "Cannot find com.ecommerce package"
- Run: `mvn clean compile`
- Restart your IDE
- Check folder structure matches package naming

## Next Steps

1. ✅ Database configured
2. ✅ PostgreSQL connected
3. ✅ Flyway migrations ready
4. → Create entities (Product, Category, Order, etc.)
5. → Create repositories
6. → Create services
7. → Create controllers
8. → Write tests

## Configuration Files

- **application.properties** - Main configuration
- **pom.xml** - Maven dependencies and build config

All settings are environment-specific. For production, create `application-prod.properties`.

---

**Author:** Ecommerce Team  
**Created:** February 16, 2026  
**Last Updated:** February 16, 2026
