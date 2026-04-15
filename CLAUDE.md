# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MobileEmotionAI is an Android application for employee emotion feedback and HR analytics. It supports two user roles — **Employee** and **HR** — with entirely separate navigation flows loaded at runtime based on the authenticated user's role.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew lint                   # Run lint checks
```

[//]: # (- **minSdk:** 28, **targetSdk/compileSdk:** 36)
- **JVM target:** Java 17
- **Kotlin:** 2.2.21, **AGP:** 8.13.2
- Dependencies are managed via `gradle/libs.versions.toml` (version catalog)

## Architecture

### Dual-Role Navigation
`MainActivity` dynamically swaps the entire NavController graph and bottom navigation menu at startup based on the role stored in `TokenStorage`. HR users get `nav_hr.xml`; employees get `nav_employee.xml`. There are no separate Activities per role.

### Layer Structure
- **`data/remote/`** — Retrofit `ApiService` (67 endpoints), `ApiClient` (builds OkHttp + Retrofit), `AuthInterceptor` (injects Bearer token), `TokenAuthenticator` (handles 401 + token refresh)
- **`data/local/`** — `TokenStorage` (SharedPreferences: `access`, `refresh`, `role` keys), `SettingsStorage`
- **`data/repo/`** — Repository classes that wrap API calls; all exceptions are caught and returned as `Result.Err`. No domain model layer — DTOs flow directly to the UI.
- **`di/ServiceLocator.kt`** — Manual DI using `@Volatile` lazy singletons + `synchronized` init blocks. Provides factory methods for every ViewModel. There is no Hilt/Dagger.
- **`ui/`** — MVVM Fragments + ViewModels. HR Analytics screens use Jetpack Compose (`HrAnalyticsScreen.kt`, `KpiCards.kt`, `FilterBar.kt`, chart composables); all other screens are XML + View Binding.

### Key Patterns
- **`util/Result.kt`** — Sealed class `Result<T>` (`Ok`/`Err`) used as the return type from all repositories.
- **Dual OkHttp clients:** `mainOkHttp` has the auth interceptor; `refreshOnlyOkHttp` skips it to avoid recursion during token refresh.
- **ViewModel creation:** Each Fragment calls `ServiceLocator` directly to obtain the ViewModel factory — not `ViewModelProvider.Factory` injected via constructor.

### Entry Flow
`LaunchActivity` (splash) → `LoginActivity` (credentials or face login via `CameraX`) → `MainActivity` (loads role-specific nav graph + bottom nav).

## Key Files

| File | Purpose |
|---|---|
| `data/remote/ApiService.kt` | All 67 REST endpoint definitions |
| `di/ServiceLocator.kt` | All dependency wiring and ViewModel factories |
| `data/local/TokenStorage.kt` | JWT + role persistence |
| `data/remote/TokenAuthenticator.kt` | Token refresh logic on 401 |
| `ui/main/MainActivity.kt` | Nav graph + bottom nav swap based on role |
| `ui/hr/analytics/HrAnalyticsViewModel.kt` | Analytics data orchestration (Compose-backed) |

## Backend

Base URL: `http://185.5.206.121/` — cleartext traffic is enabled in the manifest for this backend. Auth uses JWT Bearer tokens with a separate refresh endpoint.

## Mixed UI Stack

Most screens are XML layouts with View Binding. The HR Analytics section (`ui/hr/analytics/`) is entirely Jetpack Compose with a custom `EmotionColors` theme, MPAndroidChart for pie/bar charts, and a `HrAnalyticsUiState` sealed class for UI state management. When editing analytics screens, use Compose conventions; for all other screens, use XML Fragment conventions.
