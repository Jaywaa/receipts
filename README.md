# Parking Receipts

Android app for photographing parking receipts, tracking amounts in ZAR, and bulk-sending them as a collated PDF via email.

## Features

- **Capture receipts** via camera or gallery with amount, date, and optional note
- **Track unsent/sent** receipts with swipe-to-delete
- **Bulk send** — select receipts, generate a PDF, and open your email client with everything pre-filled (To, CC, subject, body with total, PDF attachment)
- **Friday reminder** notification via WorkManager
- **Configurable** recipient, CC, subject template, and reminder time

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Room · Navigation Compose · CameraX · WorkManager · DataStore · Coil

## Build

Open in Android Studio, sync Gradle, run on device/emulator (API 24+).

Requires: AGP 9.0.1, Kotlin 2.0.21, Java 17+, Gradle 9.2.1.
