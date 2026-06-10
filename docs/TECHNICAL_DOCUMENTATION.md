# MobileEmotionAI — Technical Documentation

## Project Configuration

| Property | Value |
|---|---|
| Language | Kotlin 2.2.21 |
| Android Gradle Plugin | 8.13.2 |
| Min SDK | 28 (Android 9 Pie) |
| Target / Compile SDK | 36 |
| JVM Target | Java 17 |
| Package | `com.example.emotionsai` |
| Build System | Gradle with version catalog (`gradle/libs.versions.toml`) |

---

## Architecture Overview

The app follows **MVVM** (Model-View-ViewModel) with a **Repository** pattern and **manual dependency injection** via a `ServiceLocator` singleton.

```
UI Layer       Fragment / Activity / Composable
               ↕ observe LiveData / Compose State
ViewModel      Holds UI state, coordinates repos, no Android references
               ↕ suspend calls (Coroutines)
Repository     Wraps API/WebSocket; returns Result<T>
               ↕ Retrofit / OkHttp / WebSocket
Remote         ApiService (Retrofit), ChatWebSocket
Local          TokenStorage, SettingsStorage (SharedPreferences)
DI             ServiceLocator — all wiring in one place
```

### Key design decisions
- **No Hilt/Dagger** — all dependencies are wired in `ServiceLocator.kt` using `@Volatile` lazy singletons with `synchronized` initialization blocks.
- **No domain layer** — DTOs returned by Retrofit flow directly to ViewModels and UI. No separate domain model mapping.
- **Sealed `Result<T>`** — all repositories return `Result.Ok<T>` or `Result.Err(message)`. ViewModels never throw; errors surface through LiveData.
- **Mixed UI stack** — most screens use XML layouts with View Binding; the HR Analytics section is entirely Jetpack Compose.

---

## Module & Package Structure

```
app/src/main/java/com/example/emotionsai/
├── data/
│   ├── local/
│   │   ├── TokenStorage.kt          JWT + role persistence
│   │   └── SettingsStorage.kt       App preferences (face ID flag)
│   ├── remote/
│   │   ├── ApiService.kt            All 30+ Retrofit endpoint definitions
│   │   ├── ApiClient.kt             OkHttp + Retrofit setup (two clients)
│   │   ├── AuthInterceptor.kt       Attaches Bearer token to requests
│   │   └── TokenAuthenticator.kt    Handles 401 + silent token refresh
│   └── repo/
│       ├── AuthRepository.kt
│       ├── UserRepository.kt
│       ├── FeedbackRepository.kt
│       ├── ReferenceRepository.kt
│       ├── FaceAuthRepository.kt
│       ├── EventRepository.kt
│       └── RequestRepository.kt
├── di/
│   └── ServiceLocator.kt            Manual DI singleton
├── ui/
│   ├── launch/
│   │   └── LaunchActivity.kt        Splash screen
│   ├── login/
│   │   ├── LoginActivity.kt
│   │   └── LoginViewModel.kt
│   ├── main/
│   │   └── MainActivity.kt          Role-based nav graph switcher
│   ├── shared/
│   │   ├── AuthGateFragment.kt      Entry point for both nav graphs
│   │   └── FaceLoginCameraFragment.kt
│   ├── employee/
│   │   ├── profile/
│   │   ├── events/                  EmployeeEventsFragment, CameraFragment, ResultFragment
│   │   └── requests/                EmployeeRequestsFragment, EmployeeRequestDetailsFragment, CreateRequestFragment
│   └── hr/
│       ├── profile/
│       ├── events/                  HrEventsFragment, CreateEventFragment
│       ├── analytics/               HrAnalyticsFragment (Compose), HrAnalyticsViewModel, composables
│       └── requests/                HrRequestsFragment, HrRequestDetailsFragment
├── util/
│   ├── Result.kt
│   └── PhotoVerifyResult.kt
└── ws/
    └── ChatWebSocket.kt             OkHttp-based WebSocket flow
```

---

## Dependency Injection — ServiceLocator

`ServiceLocator` is a plain Kotlin `object`. All fields are `@Volatile` and initialized on first access with double-checked locking.

```kotlin
// Pattern used for every dependency
@Volatile private var _tokenStorage: TokenStorage? = null
fun tokenStorage(ctx: Context): TokenStorage =
    _tokenStorage ?: synchronized(this) {
        _tokenStorage ?: TokenStorage(ctx.applicationContext).also { _tokenStorage = it }
    }
```

**Available singletons:**
`tokenStorage`, `settingsStorage`, `apiClient`, `authRepository`, `userRepository`, `feedbackRepository`, `referenceRepository`, `faceAuthRepository`, `eventRepository`, `requestRepository`

**ViewModel factory methods** (one per ViewModel that needs constructor injection):
`hrAnalyticsVMFactory`, `createEventVMFactory`, `employeeEventsVMFactory`, `hrEventsVMFactory`, `employeeRequestsVMFactory`, `createRequestVMFactory`, `employeeRequestDetailsVMFactory`, `hrRequestsVMFactory`, `hrRequestDetailsVMFactory`

---

## Networking Layer

### OkHttp Clients

Two separate clients are constructed to prevent token-refresh recursion:

| Client | Purpose | Has AuthInterceptor | Has TokenAuthenticator |
|---|---|---|---|
| `mainOkHttp` | All regular API calls | Yes | Yes |
| `refreshOnlyOkHttp` | Token refresh only | No | No |

Timeouts: connect 30 s, read 60 s, write 60 s.

### Retrofit Instances

| Instance | Client | Used by |
|---|---|---|
| `api` | `mainOkHttp` | All repositories except token refresh |
| `refreshApi` | `refreshOnlyOkHttp` | `TokenAuthenticator` only |

Base URL: `http://185.5.206.121/` — cleartext HTTP is explicitly allowed via `android:usesCleartextTraffic="true"` in the manifest.

### AuthInterceptor

Reads the access token from `TokenStorage` and prepends `Authorization: Bearer <token>` to every outgoing request on the main client.

### TokenAuthenticator

Triggered automatically by OkHttp when any response returns HTTP 401.

```
1. Skip if path is /api/auth/refresh or /api/auth/photo-login
2. Return null after the 3rd retry (counted via response.priorResponse chain)
3. Acquire ReentrantLock to serialize concurrent refresh attempts
4. Check if another thread already refreshed the token
5. Call POST /api/auth/refresh via refreshApi
6. Write new access token to TokenStorage
7. Return the original request re-built with the new token
```

---

## Local Storage

### TokenStorage

```
SharedPreferences file: "auth_tokens"
Keys:
  "access"   — JWT access token (short-lived)
  "refresh"  — JWT refresh token (long-lived)
  "role"     — "HR" or "EMPLOYEE"
```

Methods: `getAccess()`, `getRefresh()`, `getRole()`, `saveTokens(access, refresh, role)`, `updateAccess(access)`, `clear()`, `isLoggedIn()` (returns true if refresh token is present).

### SettingsStorage

```
SharedPreferences file: "app_settings"
Keys:
  "face_id_enabled" — Boolean, default false
```

Methods: `isFaceIdEnabled()`, `setFaceIdEnabled(enabled: Boolean)`

---

## Navigation Architecture

### Role-Based Graph Switching — MainActivity

```kotlin
val role = UserRole.from(tokenStorage.getRole())   // HR or EMPLOYEE
navHost.navController.setGraph(
    if (role == UserRole.HR) R.navigation.nav_hr else R.navigation.nav_employee
)
bottomNav.inflateMenu(
    if (role == UserRole.HR) R.menu.bottom_hr else R.menu.bottom_employee
)
```

Bottom nav visibility is controlled via `OnDestinationChangedListener`:
- Hidden for `cameraFragment` and `faceLoginCameraFragment`
- Selected item is manually overridden for nested destinations (e.g., `hrRequestDetailsFragment` keeps the Requests tab highlighted)

### nav_employee.xml Structure

```
authGateFragment (start)
  └─ faceLoginCameraFragment
       └─ profileFragment (home)
            [bottom nav]
            ├─ employeeEventsFragment
            │    └─ cameraFragment → resultFragment → [back to events]
            └─ employeeRequestsFragment
                 ├─ createRequestFragment → employeeRequestDetailsFragment
                 └─ employeeRequestDetailsFragment
```

### nav_hr.xml Structure

```
authGateFragment (start)
  └─ faceLoginCameraFragment
       └─ hrProfileFragment (home)
            [bottom nav]
            ├─ hrAnalyticsFragment
            ├─ hrEventsFragment
            │    └─ createEventFragment (create or edit)
            └─ hrRequestsFragment
                 └─ hrRequestDetailsFragment
```

### AuthGateFragment

Both graphs share this entry point. On resume it checks:
1. If `TokenStorage.isLoggedIn()` is false → `LoginActivity` with `NEW_TASK | CLEAR_TASK`
2. If `SettingsStorage.isFaceIdEnabled()` is true → `faceLoginCameraFragment`
3. Otherwise → role home fragment

---

## ViewModels

All ViewModels extend `androidx.lifecycle.ViewModel` and use `viewModelScope` for coroutines. State is exposed as `LiveData` (XML screens) or Compose `mutableStateOf` (HrAnalyticsViewModel).

### Error Pattern

```kotlin
_error.value = when (result) {
    is Result.Ok -> { /* use result.value */ null }
    is Result.Err -> result.message
}
```

### One-shot Event Pattern

Several ViewModels use a boolean LiveData flag (`created`, `success`, `forceLogout`) that the Fragment observes and immediately resets by calling `vm.xyzHandled()` after acting on it.

### ViewModel Reference

| ViewModel | Key LiveData / State | Key Actions |
|---|---|---|
| `LoginViewModel` | `loading`, `error`, `success` | `login()`, `register()` |
| `ProfileViewModel` | `me`, `loading`, `error`, `forceLogout` | `loadMe()`, `logout()` |
| `EmployeeEventsViewModel` | `events`, `loading`, `error` | `load()` |
| `CameraViewModel` | `uiState` (Idle/Loading/Success/Error) | `submitPhoto(file, eventId?)`, `resetState()` |
| `EmployeeRequestsViewModel` | `items`, `loading`, `error` | `load()`, `toggleSortOrder()` |
| `CreateRequestViewModel` | `types`, `hrs`, `created`, `loading`, `error` | `loadRefs()`, `create()` |
| `EmployeeRequestDetailsViewModel` | `details`, `loading`, `sending`, `error` | `load(id)`, `send(id, text?, file?)` |
| `HrEventsViewModel` | `events`, `filter`, `loading`, `error` | `loadEvents()`, `onSearch()`, `setDateFilter()`, `deleteEvent()`, `setActivity()` |
| `CreateEventViewModel` | `employees`, `eventDetails`, `success`, `loading`, `error` | `loadEmployees()`, `loadEventDetails()`, `createEvent()`, `updateEvent()` |
| `HrRequestsViewModel` | `items`, `loading`, `error` | `load()`, `toggleSortOrder()` |
| `HrRequestDetailsViewModel` | `details`, `loading`, `sending`, `error` | `load(id)`, `send()`, `setInProgress()`, `close()` |
| `HrAnalyticsViewModel` | Compose state: `uiState`, filters | `init()`, `loadAnalytics()`, `onFiltersChanged()` (debounced 250 ms) |

---

## Repository Layer

All public repository methods are `suspend` functions returning `Result<T>`.

```kotlin
suspend fun someCall(): Result<ResponseType> = try {
    val response = apiService.someEndpoint()
    Result.Ok(response)
} catch (e: Exception) {
    Result.Err(e.message ?: "Unknown error")
}
```

### RequestRepository — Multipart Helpers

```kotlin
fun String.toTextPart(): RequestBody = toRequestBody("text/plain".toMediaTypeOrNull())
fun File.toFilePart(): MultipartBody.Part =
    MultipartBody.Part.createFormData("file", name, asRequestBody("application/octet-stream".toMediaTypeOrNull()))
```

---

## API Endpoints Reference

Base URL: `http://185.5.206.121/`

### Authentication

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/auth/register` | Register (username, name, password, company_id?, department_id?) |
| POST | `/api/auth/login` | Login → returns access + refresh tokens + user |
| POST | `/api/auth/refresh` | Refresh access token using refresh token |
| GET | `/api/auth/me` | Get current authenticated user profile |
| POST | `/api/auth/photo-login` | Face biometric verification (multipart photo) |

### Reference Data

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/feedback/companies` | List all companies |
| GET | `/api/feedback/departments` | List departments (optional `?company_id=`) |
| GET | `/api/hr/company/departments` | HR's own company departments |
| GET | `/api/hr/company/employees` | HR's company employees |

### Employee Feedback

| Method | Path | Purpose |
|---|---|---|
| POST | `/api/employee/feedback` | Submit photo for emotion analysis (multipart, optional event_id) |
| GET | `/api/employee/events/my` | Get employee's assigned events |

### HR Analytics

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/hr/analytics/feedbacks/` | Filtered feedbacks (start_date, end_date, departments, emotions, event_id, has_event) |

### HR Event Management

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/hr/events/` | List all company events |
| POST | `/api/hr/events/` | Create event |
| GET | `/api/hr/events/{id}/` | Get event details with participants |
| PUT | `/api/hr/events/{id}/` | Update event |
| DELETE | `/api/hr/events/{id}/` | Delete event |

### Employee Requests

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/employee/requests/` | List own requests |
| POST | `/api/employee/requests/` | Create request (type, hr, comment) |
| GET | `/api/employee/requests/types/` | List available request types |
| GET | `/api/employee/requests/hr-list/` | List HR people available to address |
| GET | `/api/employee/requests/{id}/` | Get request details + messages |
| POST | `/api/employee/requests/{id}/messages/` | Send message / file attachment |

### HR Requests

| Method | Path | Purpose |
|---|---|---|
| GET | `/api/hr/requests/` | List all requests assigned to this HR |
| GET | `/api/hr/requests/{id}/` | Get request details + messages |
| POST | `/api/hr/requests/{id}/messages/` | Send message / file attachment |
| PATCH | `/api/hr/requests/{id}/status/` | Update request status |
| POST | `/api/hr/requests/{id}/close/` | Close request |

---

## Data Transfer Objects (DTOs)

### Auth

```kotlin
data class LoginRequest(val username: String, val password: String)
data class RefreshRequest(val refresh: String)
data class AuthResponse(val access: String, val refresh: String, val user: UserDto)
data class AccessResponse(val access: String)
data class UserDto(val id: Int, val username: String, val name: String, val role: String,
                   val company: Int?, val company_name: String?,
                   val department: Int?, val department_name: String?)
data class MeResponse(/* same fields as UserDto */)
data class PhotoLoginResponse(val verdict: String, val detail: String)
```

### Events

```kotlin
data class HrEventDto(val id: Int, val title: String, val starts_at: String,
                      val ends_at: String?, val company: Int, val company_name: String,
                      val participants_count: Int)
data class EmployeeEventDto(/* HrEventDto fields */ val has_feedback: Boolean)
data class HrEventDetailsDto(/* HrEventDto fields */ val participants: List<Int>)
data class EventCreateRequest(val title: String, val starts_at: String,
                               val ends_at: String?, val company: Int?,
                               val participants: List<Int>?)
```

### Feedback / Analytics

```kotlin
data class FeedbackResponse(val id: Int, val emotion: String)
data class Feedback(val id: Int, val user_id: Int, val user_username: String,
                    val emotion: String, val created_at: String,
                    val department: Int?, val department_name: String?,
                    val event: Int?)
```

### Requests & Messages

```kotlin
data class RequestTypeDto(val id: Int, val name: String, val description: String)
data class HrShortDto(val id: Int, val username: String, val name: String)
data class EmployeeRequestItemDto(val id: Int, val type: Int, val type_name: String,
                                   val hr: Int, val hr_username: String, val hr_name: String,
                                   val status: String, val created_at: String,
                                   val closed_at: String?, val messages_count: Int,
                                   val last_message_at: String?)
data class RequestMessageDto(val id: Int, val sender: Int, val sender_username: String,
                              val sender_name: String, val text: String?,
                              val file: String?, val created_at: String, val is_mine: Boolean)
data class WsMessageDto(val id: Int, val sender: Int, val sender_username: String,
                         val sender_name: String, val text: String,
                         val file: String?, val created_at: String)
```

---

## WebSocket — Real-time Chat

File: `ws/ChatWebSocket.kt`

```
Endpoint: ws://185.5.206.121/ws/chat/{requestId}/?token={accessToken}
Protocol: OkHttp WebSocket
```

```kotlin
fun connect(requestId: Int, token: String): Flow<WsMessageDto> = callbackFlow {
    val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)    // no read timeout for persistent connection
        .pingInterval(30, TimeUnit.SECONDS)        // keep-alive
        .build()
    val ws = client.newWebSocket(request, object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            trySend(gson.fromJson(text, WsMessageDto::class.java))
        }
        override fun onFailure(...) { close(throwable) }
    })
    awaitClose { ws.close(1000, "Flow cancelled") }
}
```

The Flow is collected inside a `viewModelScope.launch` block in the details ViewModels. The coroutine (and thus the WebSocket connection) is cancelled automatically when the ViewModel is cleared.

---

## Camera Integration

Uses **CameraX** (version 1.3.1) for both features that require the camera.

### Emotion Feedback (CameraFragment)

- Binds `Preview` + `ImageCapture` use cases.
- Front camera (`CameraSelector.DEFAULT_FRONT_CAMERA`) preferred; falls back to any available camera.
- On capture: saves to a temp JPEG file in the app's cache directory.
- Passes the file to `CameraViewModel.submitPhoto()`.

### Face Authentication (FaceLoginCameraFragment)

- Same CameraX setup as above.
- Sends photo to `FaceAuthRepository.verifyFace()`.
- Tracks failure count in a local variable; enforces the 3-attempt limit.
- On 3rd failure: calls `AuthRepository.logout()` then starts `LoginActivity`.

---

## HR Analytics — Compose UI

All files under `ui/hr/analytics/`:

| File | Purpose |
|---|---|
| `HrAnalyticsFragment.kt` | Fragment host; creates `ComposeView`, calls `vm.init()` on resume |
| `HrAnalyticsScreen.kt` | Root composable; renders filter bar + chart area based on `uiState` |
| `HrAnalyticsViewModel.kt` | State holder; Compose `mutableStateOf` for filters + `StateFlow`-like debounce |
| `KpiCards.kt` | Summary cards showing count per emotion |
| `FilterBar.kt` | Composable row with department, emotion, event, date range pickers |
| Chart composables | MPAndroidChart wrappers for pie and bar charts |

### UiState sealed class

```kotlin
sealed class HrAnalyticsUiState {
    object Loading : HrAnalyticsUiState()
    data class Success(val data: List<Feedback>) : HrAnalyticsUiState()
    data class Error(val message: String) : HrAnalyticsUiState()
}
```

### Theme

Custom `EmotionColors` object defines the indigo/pink color palette used in Compose screens. XML screens use the corresponding Material3 `Theme.EmotionsAi` theme defined in `res/values/themes.xml`.

---

## Result Type

```kotlin
sealed class Result<out T> {
    data class Ok<T>(val value: T) : Result<T>()
    data class Err(val message: String) : Result<Nothing>()
}
```

Used as the return type of every repository call. ViewModels destructure it with a `when` expression; errors are mapped to the `_error` LiveData; successes update the relevant data LiveData.

---

## PhotoVerifyResult Type

```kotlin
sealed class PhotoVerifyResult {
    object Approved : PhotoVerifyResult()
    data class Rejected(val reason: String) : PhotoVerifyResult()
    data class Error(val httpCode: Int?, val throwable: Throwable) : PhotoVerifyResult()
}
```

Note the distinction: the server returns HTTP 200 with `verdict = "NO"` for a legitimate rejection, not an HTTP error. `FaceAuthRepository` maps the HTTP 200 + "NO" case to `Rejected`, and genuine network or server errors to `Error`.

---

## Key Dependencies

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.2.21 | Language |
| Coroutines | 1.10.2 | Async / suspend |
| Lifecycle ViewModel + LiveData | 2.10.0 | MVVM state |
| Navigation Component | 2.9.6 | Fragment navigation, Safe Args |
| Retrofit | 3.0.0 | REST client |
| OkHttp | 5.3.2 | HTTP + WebSocket |
| Gson converter | (with Retrofit) | JSON serialization |
| CameraX | 1.3.1 | Camera capture |
| Jetpack Compose BOM | 2026.01.01 | Compose UI |
| Material3 Compose | 1.4.0 | Compose component library |
| MPAndroidChart | v3.1.0 | Pie / bar charts |
| Material Components | 1.13.0 | XML UI components |
| AppCompat | 1.7.0 | Backwards compatibility |
| ConstraintLayout | 2.2.1 | XML layouts |

---

## Manifest Permissions and Configuration

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" />

<uses-feature android:name="android.hardware.camera" android:required="false" />
<uses-feature android:name="android.hardware.camera.front" android:required="false" />
```

`android:usesCleartextTraffic="true"` is set on `<application>` to allow HTTP traffic to the backend.

### Activity Stack

| Activity | Exported | Launcher |
|---|---|---|
| `LaunchActivity` | true | Yes (MAIN + LAUNCHER) |
| `LoginActivity` | false | No |
| `MainActivity` | false | No |

Both `LoginActivity` and `MainActivity` are started with `Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK` to ensure a clean back stack when switching between auth states.

---

## Build Variants

| Task | Output |
|---|---|
| `./gradlew assembleDebug` | Debug APK with logging |
| `./gradlew assembleRelease` | Release APK (requires signing config) |
| `./gradlew test` | JVM unit tests |
| `./gradlew connectedAndroidTest` | Instrumented tests (device required) |
| `./gradlew lint` | Static analysis |
