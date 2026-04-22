# EnglishWay Course Management Service

Production-ready Spring Boot 3 microservice for managing courses, learning flow, and student progress.

## Highlights
- Course, chapter, lesson, material CRUD with role-gated writes
- Enrollment flow with payment-confirmed event activation
- Lesson locking, progress tracking, milestones, XP rewards
- Kafka events for course and progress lifecycle
- Flyway migrations, validation, global error handling

## Local Run
1. Start dependencies:
   - `docker-compose up -d`
2. Run the service:
   - `mvn spring-boot:run`

## Free Cloud Storage (No Local Files)
This service can store course images and lesson file uploads in Cloudinary free tier.

1. Create a free Cloudinary account.
2. In Cloudinary settings, create an unsigned upload preset.
3. Set environment variables before starting the service:
  - `CLOUDINARY_ENABLED=true`
  - `CLOUDINARY_CLOUD_NAME=<your-cloud-name>`
  - Option A (unsigned): `CLOUDINARY_UPLOAD_PRESET=<your-unsigned-upload-preset>`
  - Option B (signed, recommended server-side):
    - `CLOUDINARY_API_KEY=<your-api-key>`
    - `CLOUDINARY_API_SECRET=<your-api-secret>`

When enabled:
- Course images sent as `data:image/...` are uploaded to Cloudinary and only URL is stored in DB.
- Lesson attachments are uploaded to Cloudinary and material file path stores the cloud URL.
- Download endpoint redirects to the cloud file URL.

## Required Request Headers
- `X-User-Id`: static user id (default: `user-0001` if omitted)
- `X-Role`: `ADMIN`, `TEACHER`, `STUDENT`, `USER` (default: `STUDENT` if omitted)

## Sample JSON Payloads
### Create Course
```json
{
  "title": "English Basics",
  "description": "Start speaking and reading with confidence.",
  "level": "A1",
  "capacity": 200,
  "active": true
}
```

### Create Chapter
```json
{
  "courseId": "b3d6a0fe-3f6f-4b4d-8b60-9a6f1c58b2c8",
  "title": "Welcome & Alphabet",
  "orderIndex": 1
}
```

### Create Lesson
```json
{
  "chapterId": "a6e6cc2b-1c8c-4a4b-8d8a-94a1c84b3a33",
  "title": "Greetings",
  "orderIndex": 1,
  "xpReward": 20
}
```

### Create Material
```json
{
  "lessonId": "c7d1c7c0-32fd-4e62-8d2a-9a1c7c94b419",
  "type": "VIDEO",
  "title": "Greeting Basics",
  "url": "https://cdn.englishway.com/greetings.mp4",
  "content": null
}
```

### Request Enrollment
```json
{
  "courseId": "b3d6a0fe-3f6f-4b4d-8b60-9a6f1c58b2c8"
}
```

### Payment Confirmed Event (Kafka)
```json
{
  "paymentId": "pay-123",
  "userId": "student-001",
  "courseId": "b3d6a0fe-3f6f-4b4d-8b60-9a6f1c58b2c8"
}
```

### Complete Lesson
```json
{
  "lessonId": "c7d1c7c0-32fd-4e62-8d2a-9a1c7c94b419"
}
```

## Quick CRUD Test (Courses)
Replace `<courseId>` with the id returned from the create call.

```bash
curl -X POST http://localhost:8081/api/courses \
  -H "Content-Type: application/json" \
  -H "X-Role: ADMIN" \
  -H "X-User-Id: admin-001" \
  -d '{"title":"English Basics","description":"Start speaking with confidence.","level":"A1","capacity":200,"active":true}'

curl http://localhost:8081/api/courses

curl http://localhost:8081/api/courses/<courseId>

curl -X PUT http://localhost:8081/api/courses/<courseId> \
  -H "Content-Type: application/json" \
  -H "X-Role: ADMIN" \
  -H "X-User-Id: admin-001" \
  -d '{"title":"English Basics Updated","description":"Updated course.","level":"A1","capacity":250,"active":true}'

curl -X DELETE http://localhost:8081/api/courses/<courseId> \
  -H "X-Role: ADMIN" \
  -H "X-User-Id: admin-001"
```
