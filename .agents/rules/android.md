---
trigger: always_on
description: Android and Kotlin development guidelines for BYD Toolkit GB
---

# Android & Jetpack Compose Guidelines for BYD Toolkit GB

1. **Language:** Kotlin with Kotlin DSL (`build.gradle.kts`).
2. **UI Framework:** Jetpack Compose with Material 3. Follow declarative patterns and avoid mutable state leaks.
3. **Hardware & Accessibility:**
   - Any modification to `SteeringKeyAccessibilityService` must preserve fallback to default key handling.
   - Test keycode mappings carefully (`KEYCODE_MEDIA_NEXT`, `KEYCODE_MEDIA_PLAY_PAUSE`, etc.).
4. **Build Tool:** Use `gradlew.bat` on Windows. Target Java version: JDK 17.
