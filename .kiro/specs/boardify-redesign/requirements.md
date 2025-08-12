# Requirements Document

## Introduction

Boardify is a modern web-based onboarding application designed to transform and streamline the employee onboarding process for small to medium-sized businesses. The redesign focuses on creating a robust, role-based onboarding platform that provides centralized template management, automated task generation, progress tracking, and document management with e-signature capabilities.

The core workflow involves HR managers creating reusable onboarding templates scoped to departments, with each template containing ordered tasks (Events, Documents, Resources). When templates are assigned to new hires, the system generates individual todos with progress tracking and automated notifications.

## Requirements

### Requirement 1: HR Manager Registration and Company Setup

**User Story:** As an HR manager, I want to register on the platform and register my company alongside myself, so that I can start using the onboarding system without needing admin approval.

#### Acceptance Criteria

1. WHEN an HR manager visits the registration page THEN the system SHALL display fields for personal information (first name, last name, email, password) and company information (company name)
2. WHEN an HR manager submits valid registration data THEN the system SHALL create both the HR user account and company record with a default free subscription
3. WHEN registration is successful THEN the system SHALL automatically log in the HR manager and redirect to the dashboard
4. IF a company name already exists THEN the system SHALL display an error message and prevent registration
5. WHEN an HR manager logs in THEN the system SHALL authenticate using JWT tokens and maintain session state(store user details and token in local storage)

### Requirement 2: Template Creation and Management

**User Story:** As an HR manager, I want to create reusable onboarding templates for each role or department, so that I can quickly assign tailored onboarding journeys to new hires.

#### Acceptance Criteria

1. WHEN an HR manager creates a template THEN the system SHALL require a title, description, and department association
2. WHEN a template is created THEN the system SHALL initialize it with PENDING status and allow task addition(once assigned status must change to 'in-progrss' once all tasks have been complemnted by hires then status goes to 'completed')
3. WHEN an HR manager views templates THEN the system SHALL display all templates created by their company with status indicators
4. WHEN an HR manager edits a template THEN the system SHALL update the template and maintain task ordering
5. IF a template has assigned hires THEN the system SHALL show warning message foe attempted delete.

### Requirement 3: Task Definition and Management

**User Story:** As an HR manager, I want to define tasks to add to templates with different types (Event, Document, Resource), so that I can create comprehensive onboarding workflows.

#### Acceptance Criteria

1. WHEN an HR manager adds a task to a template THEN the system SHALL require task type selection (EVENT, DOCUMENT, RESOURCE)
2. WHEN creating an EVENT task THEN the system SHALL require title, description, and event date/time
3. WHEN creating a DOCUMENT task THEN the system SHALL require title, description, and signature requirement flag
4. WHEN creating a RESOURCE task THEN the system SHALL require title, description, and resource URL or file upload
5. WHEN tasks are added THEN the system SHALL maintain order index for sequential completion
6. WHEN an HR manager uploads a document to a task THEN the system SHALL store the file and associate it with the task

### Requirement 4: Template Assignment to New Hires

**User Story:** As an HR manager, I want to assign a template to a new hire(or an multiple hires through a department ), so that they receive only the tasks and documents relevant to their department and role.

#### Acceptance Criteria

1. WHEN an HR manager assigns a template to a new hire THEN the system SHALL create individual Todo records for each task in the template
2. WHEN todos are generated THEN the system SHALL copy task details and set initial status to PENDING
3. WHEN a template is assigned THEN the system SHALL initialize the hire's progress at 0%
4. WHEN assignment is complete THEN the system SHALL send notification to the new hire


### Requirement 5: New Hire Registration and Dashboard

**User Story:** As a new hire, I want a personalized onboarding checklist, so that I know exactly what tasks I need to complete.

#### Acceptance Criteria

1. WHEN a new hire logs in THEN the system SHALL display their personalized dashboard with assigned todos
2. WHEN viewing todos THEN the system SHALL show task details, due dates, and completion status
3. WHEN a new hire completes a non-signature task THEN the system SHALL update status to COMPLETED and recalculate progress
4. WHEN progress is updated THEN the system SHALL display current completion percentage
5. WHEN all todos are completed THEN the system SHALL show completion celebration and notify HR

### Requirement 6: Document Management and E-Signatures

**User Story:** As an HR manager, I want to upload documents and track e-signatures on key documents, so that I don't have to manually follow up on document completion.

#### Acceptance Criteria

1. WHEN an HR manager uploads a document to a task THEN the system SHALL store the file with metadata
2. WHEN a new hire encounters a signature-required document THEN the system SHALL display embedded signing interface
3. WHEN a new hire completes document signing THEN the system SHALL notify the HR manager with a notification badge
4. WHEN an HR manager clicks the notification THEN the system SHALL show document status and completion details
5. WHEN HR marks a signed document as complete THEN the system SHALL update todo status and clear notification

### Requirement 7: Progress Tracking and Analytics

**User Story:** As an HR manager, I want a dashboard showing the progress of all new hires, so that I can intervene if someone is falling behind.

#### Acceptance Criteria

1. WHEN an HR manager views the dashboard THEN the system SHALL display progress for all active hires
2. WHEN displaying progress THEN the system SHALL show completion percentage, overdue tasks, and recent activity
3. WHEN a hire falls behind schedule THEN the system SHALL highlight overdue items with visual indicators
4. WHEN progress is updated THEN the system SHALL recalculate percentages based on completed todos vs total todos
5. WHEN viewing individual hire details THEN the system SHALL show detailed task breakdown and timeline

### Requirement 8: Notification and Reminder System

**User Story:** As an HR manager, I want to set up reminders and notifications, so that new hires don't forget critical tasks.

#### Acceptance Criteria

1. WHEN a new hire has overdue tasks THEN the system SHALL automatically send reminder notifications
2. WHEN an HR manager sends manual reminders THEN the system SHALL deliver notifications to the hire
3. WHEN signature documents are completed THEN the system SHALL create notification badges for HR managers
4. WHEN notifications are created THEN the system SHALL support both in-app and email delivery
5. WHEN reminders are sent THEN the system SHALL track reminder timestamps to prevent spam

### Requirement 9: Department Management

**User Story:** As an HR manager, I want to create and manage departments, so that I can organize templates and hires by organizational structure.

#### Acceptance Criteria

1. WHEN an HR manager creates a department THEN the system SHALL require department name and associate with company
2. WHEN departments are created THEN the system SHALL allow template assignment to specific departments
3. WHEN hiring new employees THEN the system SHALL allow department assignment during hire creation
4. WHEN viewing departments THEN the system SHALL show associated templates and hire counts
5. IF a department has active hires THEN the system SHALL prevent deletion and show warning

### Requirement 10: Calendar and Event Management

**User Story:** As an HR manager, I want to manage onboarding events in a calendar, so that scheduling is transparent and coordinated.

#### Acceptance Criteria

1. WHEN EVENT tasks are created THEN the system SHALL display them in a calendar view
2. WHEN viewing calendar THEN the system SHALL show events for all hires with date/time details
3. WHEN events are approaching THEN the system SHALL send reminder notifications to relevant parties
4. WHEN an HR manager reschedules an event THEN the system SHALL update all related todos and notify affected hires
5. WHEN new hires view their calendar THEN the system SHALL show only their assigned events

### Requirement 11: File Upload and Resource Management

**User Story:** As an HR manager, I want to upload and share documents with new hires, so they can easily access the resources they need.

#### Acceptance Criteria

1. WHEN uploading files THEN the system SHALL support common document formats (PDF, DOC, images)
2. WHEN files are uploaded THEN the system SHALL store them securely with access controls
3. WHEN new hires access resources THEN the system SHALL provide download links for assigned documents
4. WHEN resources are accessed THEN the system SHALL track access for completion verification
5. WHEN files are large THEN the system SHALL implement proper file size limits and error handling

### Requirement 12: Team Introduction and Employee Directory

**User Story:** As a new hire, I want to view information about my team and manager, so that I can feel connected before my first day.

#### Acceptance Criteria

1. WHEN new hires access team introductions THEN the system SHALL display team member profiles with photos and roles
2. WHEN team information is displayed THEN the system SHALL show manager details and reporting structure
3. WHEN available THEN the system SHALL include video introductions from team members
4. WHEN viewing team directory THEN the system SHALL organize members by department and role
5. WHEN team information is updated THEN the system SHALL reflect changes for all relevant hires