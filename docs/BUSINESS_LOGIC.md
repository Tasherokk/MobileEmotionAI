# MobileEmotionAI — Business Logic Documentation

## Overview

MobileEmotionAI is an HR analytics platform delivered as an Android application. It enables organizations to collect employee emotional feedback through AI-powered photo analysis and provides HR personnel with a dashboard to monitor workforce wellbeing. The system also includes a built-in support request channel for private employee-to-HR communication.

---

## User Roles

The application supports exactly two roles, determined at login and persisted for the session.

| Role | Description |
|---|---|
| **Employee** | Regular staff member. Participates in emotion feedback events, views their own results, and communicates privately with HR via support requests. |
| **HR** | Human Resources personnel. Manages emotion feedback events, views organization-wide analytics, and handles employee support requests. |

Role determines the entire navigation structure: an Employee and an HR user see completely different screens and have access to different data and actions.

---

## Authentication

### Standard Login
Users authenticate with a username and password. On success, the server issues a JWT access token and a refresh token. The access token is used for all subsequent API calls. The refresh token is used to silently renew the access token when it expires, without requiring the user to log in again.

### Face ID (Optional Secondary Authentication)
Employees and HR users can enable Face ID in their profile settings. When enabled, every time the app starts the user must verify their identity by taking a selfie with the front camera before reaching their home screen. The photo is sent to the backend for biometric verification.

- If the server approves the photo → the user proceeds normally.
- If the server rejects the photo → the user gets up to **3 attempts** total. After 3 failures the session is terminated and the user is returned to the login screen.

### Token Lifecycle
- Tokens are stored locally in encrypted shared preferences.
- Every API request automatically carries the current access token.
- If any request returns a 401 Unauthorized response, the app silently exchanges the refresh token for a new access token and retries the original request — the user never sees an interruption.
- If the refresh itself fails (e.g., the refresh token is expired), the user is logged out and sent back to the login screen.

### Logout
Pressing Logout on the profile screen immediately clears all stored tokens and navigates to the login screen. No background activity persists after logout.

---

## Employee Flows

### 1. Emotion Feedback via Events

**Purpose:** Collect the employee's emotional state at a specific organizational event.

**Flow:**
1. The employee opens the **Events** screen, which lists all events they are part of.
2. They tap an event to open the camera.
3. The app opens the **front camera** and the employee takes a selfie.
4. The photo is uploaded to the server along with the event identifier.
5. The server analyzes the photo using an AI emotion model and returns the detected emotion.
6. The **Result** screen shows the detected emotion to the employee.
7. The employee returns to the events list.

**Business rules:**
- An employee can only submit feedback for events they are a participant in.
- Each feedback submission is tied to a specific event ID.
- If no camera hardware is available the flow cannot proceed (camera is required, not optional).

### 2. Support Requests

**Purpose:** Private, asynchronous communication between an employee and a specific HR person.

**Creating a request:**
1. The employee opens the **Requests** screen.
2. They tap the create button and fill in a form: choose a request type (from a server-provided list), select which HR person to address, and write an initial comment.
3. After creation they are taken directly to the request chat.

**Ongoing communication:**
- The request chat shows all messages exchanged with the HR person.
- The employee can send text messages and file attachments.
- New messages arrive in real time via a WebSocket connection (no manual refresh needed).
- The request has a status: **OPEN**, **IN_PROGRESS**, or **CLOSED**.
- Once CLOSED the employee cannot send further messages.

**Viewing requests:**
- The Requests screen shows all the employee's requests with their current status and time of last message.
- Requests can be filtered by status using tabs (All / Open / In Progress / Closed).

---

## HR Flows

### 1. Event Management

**Purpose:** HR creates and manages events that employees participate in for emotion feedback collection.

**Creating an event:**
1. HR opens the **Events** screen and taps the create button.
2. They fill in: event title, start date/time, optional end date/time, and select which employees are participants.
3. On save the event becomes visible to the selected employees immediately.

**Editing/deleting an event:**
- HR can edit or delete events directly from the events list.
- Events can be filtered and searched by title, date range, and activity status (Upcoming / Ongoing / Past / Editable).

**Activity states:**
| State | Meaning |
|---|---|
| Upcoming | Start date is in the future |
| Ongoing | Currently between start and end date |
| Past | End date has passed |
| Editable | Can still be modified |

### 2. Analytics Dashboard

**Purpose:** Give HR a holistic view of the organization's emotional state over time.

The dashboard aggregates all feedback submissions from the organization's employees. HR can filter the data along multiple dimensions simultaneously:

| Filter | Options |
|---|---|
| Date range | Custom start and end dates |
| Department | One or more departments (multi-select) |
| Emotion | One or more specific emotions (multi-select) |
| Event | A specific event |
| Has event | Show only feedback tied to an event (vs. ad-hoc) |

The results are visualized as charts (pie/bar) and summary cards (KPI cards showing counts per emotion). Filter changes trigger an automatic data refresh with a short debounce (250 ms) to avoid excessive API calls while the user is still adjusting filters.

### 3. Support Request Management

**Purpose:** HR handles all incoming support requests from employees in their organization.

- The **HR Requests** screen lists all requests assigned to them.
- HR can open any request and read the full message history.
- HR can reply with text or file attachments; messages are delivered in real time.
- HR can advance the status of a request: OPEN → IN_PROGRESS → CLOSED.
- Once a request is CLOSED neither party can send further messages.

---

## Organization Structure

The app is aware of organizational hierarchy:
- **Company** — top-level organizational entity.
- **Department** — belongs to a company; employees belong to departments.

When an employee registers, they can be associated with a company and department. This structure is used to filter analytics (HR can view emotion data broken down by department).

---

## Emotion Feedback — AI Analysis

The core differentiating feature is AI-powered emotion detection:

1. The employee submits a selfie (JPEG photo taken by CameraX).
2. The photo is sent to the backend as a multipart/form-data upload.
3. The backend runs a machine-learning model and returns a detected emotion label (e.g., "happy", "sad", "neutral", "angry").
4. This label is stored on the server associated with the employee, the timestamp, optionally the event, and the employee's department.
5. HR can later query these results through the analytics endpoint with rich filtering.

---

## Security and Privacy

- All API calls use HTTPS Bearer token authentication.
- Tokens are stored only in device-local SharedPreferences; they are never transmitted outside of API calls.
- Face photos are sent to the server for verification only; the app does not store them locally.
- Feedback photos used for emotion analysis are not stored locally after upload.
- Support request messages and files are only accessible to the employee who created the request and the HR person they addressed.

---

## Notifications and Real-time Behavior

The application uses WebSockets for real-time messaging in support request chats. A persistent WebSocket connection is maintained while a chat screen is open. When the user navigates away, the connection is closed. There are no push notifications in the current implementation — real-time updates are only active while the chat screen is visible.

---

## Business Rules Summary

| Rule | Details |
|---|---|
| One role per account | An account is permanently Employee or HR; no role switching after login |
| Face ID is optional | Disabled by default; enabled per-user in profile settings |
| 3 face login attempts | After 3 failures the session is terminated |
| Event participation | Employees only see events they are listed as participants of |
| Request assignment | A support request is addressed to one specific HR person |
| Closed request immutability | Neither party can message after a request is CLOSED |
| Token auto-refresh | Transparent to the user; session continues without interruption |
| Analytics scope | HR only sees data from their own company's employees |
