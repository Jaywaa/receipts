# Parking Receipts

Android app for photographing parking receipts, tracking amounts in ZAR, and bulk-sending them as a collated PDF via email.

## Features

- **Capture receipts** via camera or gallery with amount, date, and optional note
- **Track unsent/sent** receipts on the home screen with tabbed views and swipe-to-delete
- **Receipt detail** — view, edit, or delete individual receipts with zoomable photo
- **Bulk send** — preview selected receipts, generate an A4 PDF (summary page + one page per receipt), and open your email client with everything pre-filled (To, CC, subject, body, PDF attachment)
- **Friday reminder** notification via WorkManager to prompt weekly submission
- **Configurable settings** — recipient (To/CC), subject template with `{date_range}`, `{total}`, `{count}` placeholders, and reminder time

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Room · Navigation Compose · CameraX · WorkManager · DataStore · Coil

## Build

Open in Android Studio, sync Gradle, run on device/emulator (API 24+).

Requires: AGP 9.0.1, Kotlin 2.0.21, Java 17+, Gradle 9.2.1.

## Permissions

- **CAMERA** — capture receipt photos
- **POST_NOTIFICATIONS** — Friday reminder alerts
