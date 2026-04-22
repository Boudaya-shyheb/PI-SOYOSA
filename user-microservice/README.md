# User Service API

This project is a Spring Boot application for managing users and their profiles. It includes authentication with JWT and profile image management using Cloudinary.

## Project Structure

The project follows a standard Spring Boot layered architecture:

- **Config**: Contains configuration classes (Security, JWT, Cloudinary, etc.).
- **Controller**: REST Controllers handling incoming HTTP requests.
- **Domain**: Data models, including Entities (`User`, `Profile`) and DTOs (`AuthResponse`).
- **Repository**: Data access layer interfaces (Spring Data JPA).
- **Service**: Business logic implementation.

## Tech Stack
- Java 17+
- Spring Boot
- Spring Security (JWT)
- Hibernate / JPA
- Cloudinary (for image storage)
- MySQL / PostgreSQL (based on configuration)

## API Endpoints

### Authentication

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `POST` | `/user/register` | Register a new user | `User` object |
| `POST` | `/user/login` | Authenticate and get JWT | `{"username": "...", "password": "..."}` |

### Profile Management

| Method | Endpoint | Description | Notes |
|--------|----------|-------------|-------|
| `GET` | `/profiles` | List all profiles | |
| `GET` | `/profiles/{id}` | Get profile by ID | |
| `POST` | `/profiles/add-profile-image/{username}` | Create profile with image | Multipart form-data |
| `PUT` | `/profiles/{id}` | Update profile details | `Profile` object |
| `PUT` | `/profiles/{id}/image` | Update profile image | Multipart form-data |
| `DELETE` | `/profiles/{id}` | Delete a profile | |

## Data Models

### User
- `username` (Unique)
- `password` (Encoded)
- `role` (`STUDENT`, `TUTOR`, `ADMIN`)
- `status` (`PENDING`, `ACTIVE`, `BLOCKED`)

### Profile
- `firstName`, `lastName`
- `phoneNumber`
- `address`
- `mail`
- `level` (`A1`, `A2`, `B1`, `B2`, `C1`, `C2`)
- `image` (URL from Cloudinary)

## Authentication
Most endpoints require a JWT token in the `Authorization` header:
`Authorization: Bearer <your_token>`

*Note: Check `SecurityConfig.java` for current permitAll/authenticated settings.*

## Setup
1. Clone the repository.
2. Configure `src/main/resources/application.properties` with your database and Cloudinary credentials.
3. Run with `./mvnw spring-boot:run`.
