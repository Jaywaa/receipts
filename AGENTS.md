# Agents

## Project

Android app (Kotlin, Jetpack Compose, Material 3). Package: `com.jaywaa.receipts`.

## Architecture

- **UI**: Compose screens + AndroidViewModel per screen
- **Data**: Room (receipts DB), DataStore (settings/preferences), internal file storage (photos)
- **Services**: `PdfGenerator` (android.graphics.pdf), `EmailIntentBuilder` (ACTION_SEND + FileProvider)
- **Worker**: `ReminderWorker` (WorkManager periodic Friday notification)

## Conventions

- Currency: ZAR, formatted as `R%.2f`
- No dependency injection — ViewModels use `AndroidViewModel` with direct repository instantiation
- Type-safe navigation via `@Serializable` route objects
- KSP for Room annotation processing

## Key Paths

- `app/src/main/java/com/jaywaa/receipts/data/` — Room entities, DAO, repository, DataStore
- `app/src/main/java/com/jaywaa/receipts/ui/` — screens and ViewModels (home, addreceipt, detail, send, settings)
- `app/src/main/java/com/jaywaa/receipts/service/` — PDF generation, email intent building
- `app/src/main/java/com/jaywaa/receipts/worker/` — Friday reminder worker
- `gradle/libs.versions.toml` — all dependency versions
