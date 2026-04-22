# Interview Module

This module handles the recruitment and interview process for **Tutors**. 

## Components

### 1. Interview Form (`InterviewFormComponent`)
- **Purpose**: Allows tutors to apply for a position.
- **Features**:
  - Personal details form (Name, Email, Bio).
  - CV Upload: Tutors must upload their CV as a **PDF file**.
  - Validation: Ensure all fields are filled and the file format is correct.

### 2. Interview Pending (`InterviewPendingComponent`)
- **Purpose**: Shown when the tutor's application has been submitted but not yet reviewed.
- **Features**:
  - Displays a large **PENDING** status title.
  - Informs the tutor that their application is under review.

### 3. Interview Meeting (`InterviewMeetingComponent`)
- **Purpose**: The final stage of the interview process.
- **Features**:
  - Tutors access this component via a link sent to them by email.
  - Integrates with **Google Meet** to start the job interview with an administrator.
  - Provides a direct "Join Meeting" button.

### 4. Interview Dashboard (`InterviewDashboardComponent`)
- **Purpose**: Acts as the main container for the interview process flow.
- **Features**:
  - Provides a structured layout for the multi-step application process.
  - Navigation between Apply, Pending, and Interview stages (for demo purposes).

## Access
- This module is primarily targeted at users with the **TUTOR** (or TEACHER) role.
- Tutors can navigate to `/interview` to begin or track their application.
