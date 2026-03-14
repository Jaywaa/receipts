# Receipts (Parking Receipts)

Android app for capturing parking receipts, tracking amounts in ZAR, and sending selected receipts as a single PDF by email.

## Features

- Capture receipts via camera or gallery with amount, date, and optional note
- Import shared images (`image/*`) from other apps directly into Add Receipt
- Manage Unsent/Sent receipts with swipe-to-delete, undo, multi-select, and bulk mark sent/unsent
- View receipt details with zoomable photo, edit fields, delete, and toggle sent status
- Generate a collated A4 PDF and open your email app with From/To/CC, subject/body, and attachment prefilled
- Configure subject and PDF filename templates using `{date_range}`, `{total}`, and `{count}`
- Friday reminder notifications (WorkManager) with configurable reminder time
- Home screen quick-add widget for launching receipt capture

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Navigation Compose · Room (KSP) · CameraX · WorkManager · DataStore Preferences · Coil · Kotlinx Serialization · Glance App Widget

## Build

Open in Android Studio, sync Gradle, and run on a device/emulator (API 24+), or build from CLI:

```bash
./gradlew :app:assembleDebug
```

Toolchain versions:

- AGP 9.0.1
- Kotlin 2.0.21
- Gradle 9.2.1
- Java 17+
- minSdk 24, targetSdk 36, compileSdk 36

## Permissions

- **CAMERA** — capture receipt photos
- **POST_NOTIFICATIONS** — Friday reminder alerts

Camera hardware is optional (`android.hardware.camera` is not required).
