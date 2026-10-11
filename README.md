# Tic Tac Toe (Android)

Minimalist, offline, single-player Tic Tac Toe. You are `X` and move first; the computer is `O`.
Choose **Easy** (random) or **Unbeatable** (Minimax) difficulty. Built with Kotlin, Jetpack Compose,
Material 3 and MVVM (`ViewModel` + `StateFlow`). Package: `com.example.tictactoe`.

The app declares no INTERNET permission and stores no data.

## Requirements

- JDK 17
- Android Studio (Ladybug or newer) or the Android SDK command-line tools (platform 36)
- minSdk 24, compileSdk/targetSdk 36

## Build and run

This repository does not include the Gradle wrapper jar. Do one of:

- Open the project in Android Studio and let it sync, or
- With Gradle 8.13+ installed, run `gradle wrapper` once in the project root.

Then:

```bash
./gradlew assembleDebug             # build the debug APK
./gradlew installDebug              # install on a connected device/emulator
./gradlew testDebugUnitTest         # unit tests
./gradlew ktlintCheck lintDebug     # code style + Android Lint
./gradlew jacocoTestReport          # coverage (app/build/reports/jacoco)
./gradlew connectedDebugAndroidTest # Compose UI tests (needs emulator/device)
```

On Windows use `gradlew.bat`. CI (`.github/workflows/android.yml`) runs
`ktlintCheck lintDebug testDebugUnitTest assembleDebug` on every push and pull request.

## Project workflow

The product is defined in `INITIAL.md` and the implementation blueprint is `PRPs/tic-tac-toe-prp.md`
(Android/Kotlin; the FastAPI/React stack described in `CLAUDE.md` does not apply). Agent definitions
live in `agents/`, skills in `skills/`.

## Release signing

`./gradlew assembleRelease` signs the APK when signing details are available, otherwise it produces an unsigned APK.

- Local: `keystore.properties` in the project root (git-ignored) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`. The keystore lives in `signing/` (also git-ignored).
- CI: set these environment variables instead of the file:
  `TICTACTOE_KEYSTORE_PATH`, `TICTACTOE_KEYSTORE_PASSWORD`, `TICTACTOE_KEY_ALIAS`, `TICTACTOE_KEY_PASSWORD`.

**Back up the keystore and its password.** Updates to an installed or published app must be signed with the same key; if it is lost, it cannot be recovered.
