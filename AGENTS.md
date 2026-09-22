# BYD Toolkit GB - Antigravity Project Configuration

## Project Overview
- **Project Name:** BYD Toolkit GB (Car Infotainment Assistant for Vietnam)
- **Target Platform:** Android 10 - 12 (BYD DiLink OS - 12.8" & 15.6" Infotainment displays)
- **Tech Stack:** Kotlin, Jetpack Compose, Android Accessibility Service, AndroidX, Gradle Kotlin DSL
- **Package:** `com.byd.toolkit.gb`

## Architecture & Components
- **`app/src/main/java/com/byd/toolkit/gb/`**
  - **`BYDApplication.kt`**: Application entry point, initialization, snapshot baseline.
  - **`MainActivity.kt`**: Root navigation controller for Compose screens.
  - **`service/SteeringKeyAccessibilityService.kt`**: Captures steering wheel key events via AccessibilityService & passthrough / fallback logic.
  - **`service/AppSequencerService.kt`**: Auto-launch sequencing and split-screen management.
  - **`receiver/BootAndWifiReceiver.kt`**: Trigger service on vehicle boot (`BOOT_COMPLETED`) or mobile hotspot match.
  - **`ui/screens/`**: Dashboard, AutoLaunch, SteeringRemap, RevertToStock, KeyDetective dialog.

## Common Commands
```powershell
# Build Debug APK
.\gradlew.bat assembleDebug

# Clean Project
.\gradlew.bat clean

# Run Unit Tests
.\gradlew.bat test

# Unlock Accessibility Service via ADB (Helper Script)
.\AdbUnlockAccessibility.ps1
```

## Coding Conventions & Safety Guidelines
1. **Safety First:** This software runs on vehicle infotainment screens while driving. Never block the main thread or cause ANR (Application Not Responding).
2. **Key Recovery / Watchdog:** Ensure `SteeringKeyAccessibilityService` always maintains passthrough fallback so stock vehicle buttons (e.g., volume, safety controls) never lock up.
3. **UI / Compose:** Maintain high contrast UI (Obsidian Black & Cyan theme) optimized for daytime glanceability and nighttime dark mode.
