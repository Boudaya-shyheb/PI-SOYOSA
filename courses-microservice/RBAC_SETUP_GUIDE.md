# RBAC System - Complete Setup & Integration Guide

## 📋 Project Structure Overview

```
back and front courses/
├── angular/
│   ├── src/app/
│   │   ├── services/
│   │   │   ├── auth.service.ts                    ✨ NEW: Enhanced with LOGIN/LOGOUT
│   │   │   ├── courses-api.service.ts             ✨ NEW: Complete RBAC implementation
│   │   │   └── [other services...]
│   │   │
│   │   ├── guards/
│   │   │   ├── auth.guard.ts                      ✨ NEW: Authentication check
│   │   │   ├── content-modification.guard.ts      ✨ NEW: Content write permission
│   │   │   ├── student.guard.ts                   ✨ NEW: Student-only access
│   │   │   ├── role.guard.ts                      ✅ Existing (enhanced support)
│   │   │   └── enrollment.guard.ts                ✅ Existing
│   │   │
│   │   ├── interceptors/
│   │   │   ├── auth.interceptor.ts                ✨ NEW: Add user context headers
│   │   │   └── http-error.interceptor.ts          ✅ Existing
│   │   │
│   │   ├── features/courses/
│   │   │   ├── tutor-course-management.component.ts      ✨ NEW
│   │   │   ├── tutor-course-management.component.html    ✨ NEW
│   │   │   ├── tutor-course-management.component.css     ✨ NEW
│   │   │   ├── course-form.component.ts                  ✨ NEW
│   │   │   ├── course-form.component.html                ✨ NEW
│   │   │   ├── course-form.component.css                 ✨ NEW
│   │   │   ├── course-list.component.ts                  ✅ Updated with RBAC info
│   │   │   ├── course-list.component.html                ✅ Existing
│   │   │   ├── courses.module.ts                         ✅ Updated (added new components)
│   │   │   └── courses.routing.ts                        ✅ Updated (added new routes)
│   │   │
│   │   ├── models/
│   │   │   └── api-models.ts                      ✅ Updated (CourseResponse enhanced)
│   │   │
│   │   ├── app.module.ts                          ✅ Updated (AuthInterceptor added)
│   │   └── [other modules...]
│   │
│   ├── RBAC_IMPLEMENTATION_GUIDE.md               ✨ NEW: Full documentation
│   └── RBAC_USAGE_EXAMPLES.md                     ✨ NEW: Practical examples
│
└── src/main/java/com/englishway/course/
    └── [Backend structure - Java implementation required]
```

## 🎯 What's New

### Services
- **`auth.service.ts`**: Enhanced authentication with USER role, login/logout methods
- **`courses-api.service.ts`**: Complete RBAC-enforced API service with validation

### Guards
- **`auth.guard.ts`**: Requires user to be logged in
- **`content-modification.guard.ts`**: Requires TEACHER or ADMIN role
- **`student.guard.ts`**: Requires STUDENT role specifically

### Interceptors
- **`auth.interceptor.ts`**: Adds X-User-Id and X-Role headers to all requests

### Components
- **`TutorCourseManagementComponent`**: Tutor dashboard for managing own courses
- **`CourseFormComponent`**: Create and edit courses (protected by guard)

### Documentation
- **`RBAC_IMPLEMENTATION_GUIDE.md`**: Complete architecture and usage guide
- **`RBAC_USAGE_EXAMPLES.md`**: Real-world scenario implementations

---

## 🚀 Quick Start (3 Steps)

### Step 1: No Additional Installation Required
All components are already integrated into the existing project structure.

### Step 2: Verify AppModule has AuthInterceptor
Check `app.module.ts` - should include:
```typescript
{
  provide: HTTP_INTERCEPTORS,
  useClass: AuthInterceptor,
  multi: true
}
```

### Step 3: Start Using RBAC
```typescript
// In any component
import { AuthService } from './services/auth.service';

constructor(private authService: AuthService) {}

loginAsStudent() {
  this.authService.login('student-001', 'STUDENT');
}

logoutUser() {
  this.authService.logout();
}
```

---

## 📍 Navigation Map

### Public Routes (All Users)
```
/                          → Home
/courses                   → Course List (all users can view)
/courses/:courseId         → Course Details (read-only)
/login                     → Login Page
```

### Student Routes (Requires STUDENT role)
```
/enrolled-courses          → My Enrolled Courses
/courses/:courseId/content → Access enrolled content
```

### Tutor Routes (Requires TEACHER role + Protected by ContentModificationGuard)
```
/courses/my-courses        → Dashboard (tutor's own courses)
/courses/create            → Create New Course Form
/courses/:courseId/edit    → Edit Course Form
/courses/:courseId/lessons → Manage Lessons
```

### Admin Routes (Requires ADMIN role)
```
/admin                     → Admin Dashboard (full access to all)
/admin/courses             → Manage All Courses
```

---

## 🔐 Role Permissions Matrix

| Feature | Guest | Student | Tutor | Admin |
|---------|-------|---------|-------|-------|
| View Courses | ✅ | ✅ | ✅ | ✅ |
| View Course Details | ✅ | ✅ | ✅ | ✅ |
| Enroll in Course | ❌ | ✅ | ❌ | ❌ |
| Access Enrolled Content | ❌ | ✅ | ❌ | ❌ |
| Create Course | ❌ | ❌ | ✅ | ✅ |
| Edit Own Course | ❌ | ❌ | ✅ | ✅ |
| Edit Any Course | ❌ | ❌ | ❌ | ✅ |
| Delete Own Course | ❌ | ❌ | ✅ | ✅ |
| Delete Any Course | ❌ | ❌ | ❌ | ✅ |
| Manage Lessons | ❌ | ❌ | ✅ | ✅ |
| Access Admin Panel | ❌ | ❌ | ❌ | ✅ |

---

## 💻 Implementation Checklist

### Phase 1: Frontend Setup ✅ COMPLETE
- [x] Auth Service with login/logout
- [x] Guards for route protection
- [x] Interceptor for headers
- [x] Courses API Service with RBAC
- [x] Components for course management
- [x] Updated routing

### Phase 2: Backend Implementation (TODO)
- [ ] Mirror RBAC rules in Java services
- [ ] Validate X-User-Id and X-Role headers
- [ ] Extract user info from JWT token
- [ ] Implement ownership validation
- [ ] Add audit logging
- [ ] Implement proper error handling

### Phase 3: Frontend Enhancements (Optional)
- [ ] Add role-based UI elements
- [ ] Implement permission-based rendering
- [ ] Add user profile dropdown
- [ ] Implement session timeout
- [ ] Add activity logging

### Phase 4: Testing
- [ ] Unit tests for Auth Service
- [ ] Integration tests for guards
- [ ] End-to-end tests for workflows
- [ ] Security penetration testing

---

## 🔧 Configuration Guide

### Customize Default Role
Edit `auth.service.ts`:
```typescript
// Change default role from USER
private roleSubject = new BehaviorSubject<Role>('USER');
```

### Customize Interceptor Headers
Edit `auth.interceptor.ts`:
```typescript
// Change header names if needed
request = request.clone({
  setHeaders: {
    'X-User-Id': userId,
    'X-Role': role,
  },
});
```

### Customize API Base URL
Edit `courses-api.service.ts`:
```typescript
private readonly apiUrl = '/api/courses'; // Modify if endpoint differs
```

---

## 🛡️ Security Considerations

### ⚠️ CRITICAL: Backend MUST Validate

The frontend RBAC is for **UX and convenience only**. 
The backend **MUST independently validate**:

1. ✅ Extract user info from JWT token (not headers)
2. ✅ Verify role from token (not X-Role header)
3. ✅ Re-validate course ownership before modifications
4. ✅ Re-validate resource permissions before access
5. ✅ Log all access attempts (especially denials)

### Backend Validation Example (Java)
```java
@PostMapping
public CourseResponse createCourse(
    @RequestHeader("Authorization") String token,
    @Valid @RequestBody CourseCreateRequest request
) {
    // 1. Extract user from JWT
    User user = jwtService.extractUser(token);
    
    // 2. Verify role from token (NOT header)
    if (user.getRole() != Role.TEACHER && user.getRole() != Role.ADMIN) {
        throw new AccessDeniedException("Only TEACHER can create courses");
    }
    
    // Set current user as course creator
    request.setTutorId(user.getId());
    
    // 3. Create course
    return courseService.createCourse(user, request);
}
```

### Headers Are For Logging Only
```typescript
// Use headers only for:
- Request logging
- Analytics
- Quick debug info
- Frontend convenience

// NEVER use headers for:
- Authentication decisions
- Authorization checks
- Security-critical operations
```

---

## 📊 Data Flow Diagram

```
┌─────────────────┐
│  User Action    │
└────────┬────────┘
         │
         ▼
┌─────────────────────────┐
│  Check Auth Service     │
│  - isLoggedIn()         │
│  - getRole()            │
└────────┬────────────────┘
         │ Not logged in?
         ├─────────────────────────┐
         │                         ▼
         │                    ┌─────────┐
         │                    │ Redirect│
         │                    │ to Login│
         │                    └─────────┘
         │
         ▼ Logged in
┌─────────────────────────┐
│  Route Guard Check      │
│  - ContentModification  │
│  - Student              │
│  - Auth                 │
└────────┬────────────────┘
         │ Access denied?
         ├─────────────────────────┐
         │                         ▼
         │                    ┌─────────┐
         │                    │ Redirect│
         │                    │ to Home │
         │                    └─────────┘
         │
         ▼ Access allowed
┌─────────────────────────┐
│  Load Component         │
│  (e.g., CourseForm)     │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  User Action            │
│  (e.g., Update Course)  │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Service Permission Check│
│  - Can modify course?   │
│  - Check ownership      │
└────────┬────────────────┘
         │ Permission denied?
         ├─────────────────────────┐
         │                         ▼
         │                    ┌──────────┐
         │                    │ Show Error
         │                    │ Message  │
         │                    └──────────┘
         │
         ▼ Permission granted
┌─────────────────────────┐
│  Add Headers            │
│  X-User-Id: user-001    │
│  X-Role: TEACHER        │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Send HTTP Request      │
│  to Backend API         │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│  Backend Validation     │
│  (CRITICAL!)            │
│  - Verify JWT           │
│  - Verify role          │
│  - Verify ownership     │
└────────┬────────────────┘
         │ Invalid?
         ├──────────────────────────┐
         │                          ▼
         │                     ┌──────────┐
         │                     │ 403 Error│
         │                     │ Response │
         │                     └──────────┘
         │
         ▼ Valid
┌─────────────────────────┐
│  Process Request        │
│  Update Database        │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Return Success Response │
└────────┬────────────────┘
         │
         ▼
┌─────────────────────────┐
│ Handle in Frontend      │
│ Update UI               │
│ Show Success Message    │
└─────────────────────────┘
```

---

## 🧪 Testing Scenarios

### Test Suite 1: Guest Workflows
```typescript
// Test: Guest can view courses
loginState = 'NOT_LOGGED_IN';
loadCourses() → SUCCESS ✅

// Test: Guest cannot create course
loginState = 'NOT_LOGGED_IN';
navigateTo('/courses/create') → REDIRECT to home ✅

// Test: Guest cannot enroll
loginState = 'NOT_LOGGED_IN';
enrollCourse() → ERROR message ✅
```

### Test Suite 2: Student Workflows
```typescript
// Test: Student can enroll
role = 'STUDENT';
enrollCourse() → SUCCESS ✅

// Test: Student cannot create course
role = 'STUDENT';
createCourse() → SERVICE error ✅

// Test: Student cannot modify courses
role = 'STUDENT';
updateCourse() → SERVICE error ✅
```

### Test Suite 3: Tutor Workflows
```typescript
// Test: Tutor can create course
role = 'TEACHER';
createCourse() → SUCCESS ✅

// Test: Tutor can edit own course
role = 'TEACHER';
courseId = 'owned-by-me';
updateCourse() → SUCCESS ✅

// Test: Tutor cannot edit others' courses
role = 'TEACHER';
courseId = 'owned-by-other-tutor';
updateCourse() → SERVICE error ✅
```

### Test Suite 4: Admin Workflows
```typescript
// Test: Admin can edit any course
role = 'ADMIN';
courseId = 'any-course';
updateCourse() → SUCCESS ✅

// Test: Admin can delete any course
role = 'ADMIN';
courseId = 'any-course';
deleteCourse() → SUCCESS ✅
```

---

## 📚 File References

| File | Type | Purpose |
|------|------|---------|
| `auth.service.ts` | Service | Authentication & role management |
| `courses-api.service.ts` | Service | RBAC-enforced course API |
| `auth.guard.ts` | Guard | Require logged-in user |
| `content-modification.guard.ts` | Guard | Require TEACHER/ADMIN |
| `student.guard.ts` | Guard | Require STUDENT role |
| `auth.interceptor.ts` | Interceptor | Add user context headers |
| `tutor-course-management.component.*` | Component | Tutor dashboard |
| `course-form.component.*` | Component | Create/edit courses |
| `courses.module.ts` | Module | Feature module setup |
| `courses.routing.ts` | Routing | Feature routing with guards |
| `app.module.ts` | Module | App-level interceptor setup |
| `RBAC_IMPLEMENTATION_GUIDE.md` | Docs | Complete architecture guide |
| `RBAC_USAGE_EXAMPLES.md` | Docs | Practical usage examples |

---

## 🐛 Troubleshooting

### Issue: "Access Denied" when creating course
```
Cause: User not logged in as TEACHER
Solution: Verify auth.login('id', 'TEACHER') called before creation
```

### Issue: Headers not sent to backend
```
Cause: AuthInterceptor not registered
Solution: Check app.module.ts has HTTP_INTERCEPTORS provider
```

### Issue: Cannot edit own course
```
Cause: Backend not validating from JWT
Solution: Backend must extract user from token, not headers
```

### Issue: Student can see tutor-only buttons
```
Cause: Frontend check missing
Solution: Add *ngIf="coursesApi.canModifyCourse(course)" to template
```

---

## 📞 Support Resources

1. **Architecture**: `RBAC_IMPLEMENTATION_GUIDE.md`
2. **Examples**: `RBAC_USAGE_EXAMPLES.md`
3. **Code Comments**: See component `.ts` files
4. **Related Docs**: Check `FILE_REFERENCE.md` and `IMPLEMENTATION_GUIDE.md`

---

## ✅ Ready to Deploy?

Before deploying to production:

- [ ] Backend RBAC implemented and tested
- [ ] JWT token validation configured
- [ ] Error handling for all scenarios
- [ ] Audit logging enabled
- [ ] Security tests passed
- [ ] Load testing completed
- [ ] Documentation reviewed with team

---

## 🎓 Next Steps

1. **Review** `RBAC_IMPLEMENTATION_GUIDE.md` for complete details
2. **Study** `RBAC_USAGE_EXAMPLES.md` for real scenarios
3. **Implement** backend RBAC to mirror frontend
4. **Test** all role scenarios thoroughly
5. **Deploy** with confidence!

---

**Last Updated**: March 4, 2026  
**Version**: 1.0  
**Status**: Production Ready (Frontend Complete, Backend TODO)
