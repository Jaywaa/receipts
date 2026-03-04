# Agents

## Project

Android app (Kotlin, Jetpack Compose, Material 3). Package: `com.jaywaa.receipts`.
minSdk 24, targetSdk 36. Requires CAMERA and POST_NOTIFICATIONS permissions.

## Architecture

- **Entry**: `MainActivity` (edge-to-edge, permission requests, reminder scheduling) → `ReceiptsApp` (theme + NavHost)
- **UI**: Compose screens + AndroidViewModel per screen
- **Navigation**: Type-safe routes via `@Serializable` objects in `navigation/AppNavigation.kt`
- **Data**: Room (`data/db/`), DataStore (`data/preferences/`), ReceiptRepository (`data/repository/`) wrapping DAO + file I/O
- **Services**: `PdfGenerator` (android.graphics.pdf), `EmailIntentBuilder` (ACTION_SEND + FileProvider)
- **Worker**: `ReminderWorker` (WorkManager periodic Friday notification)

## Conventions

- Currency: ZAR, formatted as `R%.2f`
- No dependency injection — ViewModels use `AndroidViewModel` with direct repository instantiation
- Type-safe navigation via `@Serializable` route objects
- KSP for Room annotation processing
- Photos stored as JPEG in `context.filesDir`, PDFs in `context.cacheDir`
- Material 3 dynamic color on Android 12+, light/dark theme support

## Key Paths

- `app/src/main/java/com/jaywaa/receipts/` — `MainActivity.kt`, `ReceiptsApp.kt`
- `app/src/main/java/com/jaywaa/receipts/navigation/` — `AppNavigation.kt` (routes + NavHost)
- `app/src/main/java/com/jaywaa/receipts/data/db/` — Room entity (`Receipt`), DAO, `AppDatabase`
- `app/src/main/java/com/jaywaa/receipts/data/preferences/` — `SettingsDataStore`, `AppSettings`
- `app/src/main/java/com/jaywaa/receipts/data/repository/` — `ReceiptRepository` (DAO + photo file I/O)
- `app/src/main/java/com/jaywaa/receipts/ui/` — screens and ViewModels (home, addreceipt, detail, send, settings)
- `app/src/main/java/com/jaywaa/receipts/service/` — PDF generation, email intent building
- `app/src/main/java/com/jaywaa/receipts/worker/` — Friday reminder worker
- `gradle/libs.versions.toml` — all dependency versions
