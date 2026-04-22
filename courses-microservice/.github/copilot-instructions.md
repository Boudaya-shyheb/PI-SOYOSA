# EnglishWay E-Learning Platform - Development Guidelines

## Project Structure

### Backend (Spring Boot 3)
```
src/main/java/com/englishway/course/
├── controller/     # REST API endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/            # Request/Response DTOs
├── config/         # Configuration
├── exception/      # Custom exceptions
└── util/           # Utilities
```

### Frontend (Angular 17)
```
angular/src/app/
├── features/       # Feature modules (courses, quiz, etc.)
├── shared/         # Shared components
├── services/       # API services
├── models/         # TypeScript interfaces
└── guards/         # Route guards
```

## Architecture

- Course → Chapter → Lesson → Material/Quiz
- Enrollment required for content access
- Quiz stored in backend with auto-grading
- Files stored on disk, paths in database

## Development Patterns

### Backend
- Services handle business logic
- Controllers only handle HTTP mapping
- Use DTOs for API requests/responses
- Validate with `@Valid` annotation
- Use `@Transactional` for database operations

### Frontend
- Use observables with async pipe when possible
- Use NotificationService for user feedback
- Use LoadingService for loading states
- Lazy load feature modules

## Running the Project

### Backend
```bash
mvn spring-boot:run
```

### Frontend
```bash
cd angular
ng serve
```

## Key Files
- [application.yml](../src/main/resources/application.yml) - Backend config
- [api.config.ts](../angular/src/app/services/api.config.ts) - API base URL

Before starting a new task in the above plan, update progress in the plan.
-->
- Work through each checklist item systematically.
- Keep communication concise and focused.
- Follow development best practices.
