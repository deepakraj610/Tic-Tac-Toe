# CLAUDE.md - Project Rules

> Rules Claude follows in every conversation.

---

## Tech Stack

- **Platform:** Native Android (minSdk 24, compileSdk/targetSdk 36)
- **Language:** Kotlin 2.1, JVM 17
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM, unidirectional data flow (`ViewModel` + `StateFlow`)
- **Async:** Kotlin Coroutines
- **Build:** Gradle (Kotlin DSL), version catalog, AGP 8.13
- **Backend / Database / Auth:** none. The app is fully offline and single-player.

---

## Project Structure

```
project/
├── app/
│   ├── build.gradle.kts, lint.xml, proguard-rules.pro
│   └── src/
│       ├── main/java/com/example/tictactoe/
│       │   ├── MainActivity.kt
│       │   ├── domain/     # Pure Kotlin: models, GameRules, AI (Random, Minimax, AiFactory)
│       │   └── ui/
│       │       ├── game/   # GameViewModel, GameScreen, GameBoard, CellView, ScoreHeader, ...
│       │       └── theme/  # Color, Type, Theme
│       ├── main/res/       # strings.xml (all UI text), themes, icons
│       ├── test/           # JUnit unit tests (domain + ViewModel)
│       └── androidTest/    # Compose UI tests (need a device/emulator)
├── gradle/libs.versions.toml
├── signing/                # release keystore (git-ignored)
├── skills/                 # Skill files (principles; web examples are not Android-specific)
├── agents/                 # Agent definitions
├── PRPs/                   # Generated blueprints
└── .claude/commands/       # /generate-prp, /execute-prp
```

---

## Code Standards

### Kotlin
```kotlin
// Domain code is pure Kotlin (no Android imports) and immutable
fun evaluate(board: List<Cell>): GameStatus

// Composables are stateless: state + lambdas in, no ViewModel access below the screen level
@Composable
fun GameBoard(board: List<Cell>, winningLine: List<Int>, enabled: Boolean, onCellClick: (Int) -> Unit)

// ViewModel exposes read-only StateFlow and changes state via update { }
val uiState: StateFlow<GameUiState>
```

- Inject dependencies (AI factory, dispatcher, move delay) through constructors with defaults so tests can replace them.
- Max line length 120. ktlint (`.editorconfig`) is enforced.

---

## Forbidden

- `println()` / `Log` noise in production code
- Hardcoded secrets, passwords or keystore details in the repo (use `keystore.properties` or env vars)
- Hardcoded user-visible text in Kotlin (use `strings.xml`; lint treats `HardcodedText` as an error)
- Committing `signing/`, `keystore.properties`, `local.properties`, APK/AAB files
- `INTERNET` or other permissions the app does not need
- Magic numbers in UI/logic (use named constants)

---

## Workflow

```
1. Edit INITIAL.md (define product)
2. /generate-prp INITIAL.md
3. /execute-prp PRPs/[name]-prp.md
```

The web-template agent prompts in the commands (FastAPI, React, Docker) do not apply to this project. Adapt them to Kotlin/Compose, as in `PRPs/tic-tac-toe-prp.md`.

---

## Skills

| Task | Skill |
|------|-------|
| Logic / architecture principles | `skills/BACKEND.md` |
| UI design principles | `skills/FRONTEND.md` |
| Data modelling principles | `skills/DATABASE.md` |
| Tests | `skills/TESTING.md` |
| CI / release | `skills/DEPLOYMENT.md` |

Examples inside the skill files target the web stack; use only their principles.

---

## Agents

| Agent | Role |
|-------|------|
| BACKEND-AGENT | Domain logic, AI, ViewModel |
| FRONTEND-AGENT | Compose UI, theme, resources |
| DEVOPS-AGENT | Gradle, CI, signing |
| TEST-AGENT | Unit and Compose UI tests |
| REVIEW-AGENT | Correctness, concurrency, accessibility |
| DATABASE-AGENT | Not used (no database) |

---

## Validation

Windows needs JDK 17 and the Android SDK (`ANDROID_HOME`, `JAVA_HOME` set, `local.properties` with `sdk.dir`).

```bash
./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
./gradlew jacocoTestReport              # coverage report
./gradlew connectedDebugAndroidTest     # needs an unlocked device/emulator, screen on
./gradlew assembleRelease               # signed if keystore.properties or env vars exist
```

Run `./gradlew ktlintFormat` to auto-fix style. Do not claim something works until these have actually run.

---

## Release Signing

Secrets live outside git:

- Local: `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) plus `signing/*.jks`
- CI: `TICTACTOE_KEYSTORE_PATH`, `TICTACTOE_KEYSTORE_PASSWORD`, `TICTACTOE_KEY_ALIAS`, `TICTACTOE_KEY_PASSWORD`

Losing the keystore means the app can never be updated under the same signature. Keep a backup.
