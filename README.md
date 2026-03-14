# Parking Receipts

Android app for photographing parking receipts, tracking amounts in ZAR, and bulk-sending them as a collated PDF via email.

## Features

- **Capture receipts** via camera or gallery, or share an image into the app from other apps
- **Track unsent/sent** receipts on the home screen with tabbed views and swipe-to-delete
- **Receipt detail** — view, edit, or delete individual receipts with zoomable photo
- **Bulk send** — preview selected receipts, generate an A4 PDF (summary page + one page per receipt), and open your email client with everything pre-filled (To, CC, subject, body, PDF attachment)
- **Quick Add widget** — add a home screen widget to jump straight to Add Receipt
- **Friday reminder** — weekly WorkManager reminder at a configurable time (notifies only when unsent receipts exist)
- **Configurable settings** — From/To/CC emails, subject + PDF filename templates with `{date_range}`, `{total}`, `{count}` placeholders, auto-mark-as-sent, and reminder time

## Tech Stack

Kotlin · Jetpack Compose · Material 3 · Room · Navigation Compose · CameraX · WorkManager · DataStore · Coil · Glance (App Widget) · Kotlin Serialization

## Build

Open in Android Studio, sync Gradle, run on device/emulator (API 24+).

SDK: min 24 · target 36 · compile 36  
Requires: AGP 9.0.1, Kotlin 2.0.21, Java 17+, Gradle 9.2.1.

## Permissions

- **CAMERA** — capture receipt photos (camera hardware is optional; gallery/share import also supported)
- **POST_NOTIFICATIONS** — Friday reminder alerts on Android 13+
