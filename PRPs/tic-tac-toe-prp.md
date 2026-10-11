# PRP: Tic Tac Toe

> Implementation blueprint for parallel agent execution

---

## METADATA

| Field | Value |
|-------|-------|
| **Product** | Tic Tac Toe |
| **Type** | Native Android app (offline, single-player) |
| **Version** | 1.0 |
| **Created** | 2026-10-11 |
| **Complexity** | Low |

> **Stack override:** INITIAL.md's description specifies a native Android app, while its Tech Stack table and CLAUDE.md list the FastAPI/React/PostgreSQL template stack. The user chose **Android native**. The backend, database, auth and Docker layers are therefore out of scope. CLAUDE.md rules that still apply: type safety, no hardcoded secrets, no `print`/`console.log` equivalents (use `Log`/Timber, not `println`), no inline magic values, tests required.

---

## PRODUCT OVERVIEW

**Description:** A minimalist, strictly single-player Tic Tac Toe game. The human is always `X` and moves first; the computer is always `O`. It works fully offline, with instant play and no matchmaking.

**Target users:** Casual mobile gamers, commuters wanting a quick mental break, and children learning basic strategic thinking.

**Value Proposition:** Instant, offline, ad-free play against an opponent of selectable strength (random or unbeatable).

**MVP Scope:**
- [ ] 3x3 responsive game grid (Jetpack Compose)
- [ ] Header with Player vs Computer scores and current-turn indicator
- [ ] Human `X` first, computer `O`; standard win rules; immediate win/draw detection after every move
- [ ] Easy AI (random empty cell) and Unbeatable AI (Minimax)
- [ ] Difficulty selector (Easy / Unbeatable)
- [ ] ~500 ms artificial delay before the computer moves; board input locked during that time
- [ ] "Restart Game" (clear board, keep scores) and "Reset Score" (clear tally)
- [ ] MVVM with `StateFlow` in a `ViewModel`

**Out of scope:** Online play, accounts, backend, persistence across process death (optional stretch: DataStore for scores), payments, multiplayer.

---

## TECH STACK

| Layer | Technology | Skill Reference |
|-------|------------|-----------------|
| Language | Kotlin 2.x (JVM 17) | skills/BACKEND.md (logic/architecture principles only) |
| UI | Jetpack Compose + Material 3 | skills/FRONTEND.md (design principles only) |
| Architecture | MVVM, unidirectional data flow, `ViewModel` + `StateFlow` | skills/BACKEND.md |
| Async | Kotlin Coroutines (`viewModelScope`, `delay`) | - |
| Persistence | None (in-memory session score) | skills/DATABASE.md (N/A) |
| Auth / Network | None (fully offline) | - |
| Testing | JUnit4/5, kotlinx-coroutines-test, Turbine, Compose UI test | skills/TESTING.md |
| Build | Gradle (Kotlin DSL) + version catalog, AGP 8.x | skills/DEPLOYMENT.md |
| CI | GitHub Actions (`./gradlew lint test assembleDebug`) | skills/DEPLOYMENT.md |

**SDK:** minSdk 24, targetSdk/compileSdk 35 (adjust to the latest stable). Package: `com.example.tictactoe`.

---

## DATA MODELS (in-memory Kotlin types, no database)

```kotlin
enum class Player { X, O }                       // X = human, O = computer
enum class Difficulty { EASY, UNBEATABLE }
enum class Cell { EMPTY, X, O }

sealed interface GameStatus {
    data object InProgress : GameStatus
    data class Won(val winner: Player, val line: List<Int>) : GameStatus  // line = 3 winning indices
    data object Draw : GameStatus
}

data class Score(val player: Int = 0, val computer: Int = 0, val draws: Int = 0)

data class GameUiState(
    val board: List<Cell> = List(9) { Cell.EMPTY },  // immutable, index 0..8 row-major
    val currentTurn: Player = Player.X,
    val status: GameStatus = GameStatus.InProgress,
    val score: Score = Score(),
    val difficulty: Difficulty = Difficulty.UNBEATABLE,
    val isComputerThinking: Boolean = false,
)
```

---

## MODULES

### Module 1: Game Engine (pure Kotlin, no Android deps)
**Agents:** BACKEND-AGENT (logic) + TEST-AGENT

**Package:** `domain/`

| Component | Responsibility |
|-----------|----------------|
| `Board` helpers | `placeMove(board, index, player)`, `emptyCells(board)`, bounds and occupied-cell validation |
| `GameRules` | `evaluate(board): GameStatus` checks 8 win lines and draw (board full and no winner) |
| `AiStrategy` (interface) | `fun chooseMove(board: List<Cell>): Int` |
| `RandomAi` | Picks a random empty cell; `Random` is injected for deterministic tests |
| `MinimaxAi` | Minimax with depth-aware scoring (win = 10 - depth, loss = depth - 10, draw = 0). Alpha-beta pruning. O is maximizer. Prefers the faster win and the slower loss. |
| `AiFactory` | `Difficulty -> AiStrategy` |

**Rules:** Functions are pure and immutable. `chooseMove` is never called on a full or finished board (throw `IllegalStateException`).

**Public API (contract with the ViewModel):**
```kotlin
object GameRules { fun evaluate(board: List<Cell>): GameStatus }
interface AiStrategy { fun chooseMove(board: List<Cell>): Int }
```

---

### Module 2: Game State / ViewModel
**Agents:** BACKEND-AGENT

**Package:** `ui/game/GameViewModel.kt`

**State:** `val uiState: StateFlow<GameUiState>` (backed by `MutableStateFlow`, exposed read-only, updated via `update {}`).

**Intents (functions):**
| Function | Behavior |
|----------|----------|
| `onCellClicked(index: Int)` | Ignored if the cell is occupied, the game is over, it is not the human's turn, or the computer is thinking. Places `X`, evaluates the board, then triggers the computer turn if the game continues. |
| `onRestartGame()` | Cancels any in-flight computer job, clears the board, sets the turn to X, keeps the score. |
| `onResetScore()` | Sets the score to `Score()`. Also restarts the current board (resetting the tally mid-game would otherwise leave an inconsistent round). |
| `onDifficultyChanged(d: Difficulty)` | Updates the difficulty and restarts the board (the score is kept). |

**Computer turn:** Launched in `viewModelScope`; sets `isComputerThinking = true`, `delay(COMPUTER_MOVE_DELAY_MS = 500)`, runs `chooseMove` on `Dispatchers.Default`, places `O`, evaluates, updates score on a win or draw, and sets `isComputerThinking = false`. The job is stored and cancelled on restart/reset/difficulty change so a stale move can never land on a fresh board. The `ViewModel` takes `AiFactory`, `CoroutineDispatcher` and the move delay via constructor injection for testability.

**Score update:** Incremented exactly once per finished round (guard against double-count on a repeated click).

---

### Module 3: Game UI (Compose)
**Agents:** FRONTEND-AGENT

**Screens / Components:**
| Component | Description |
|-----------|-------------|
| `MainActivity` | Single-activity host, `setContent { TicTacToeTheme { GameScreen() } }` |
| `GameScreen` | Collects `uiState` via `collectAsStateWithLifecycle()`; stateless content composable receives state + lambdas (preview-friendly) |
| `ScoreHeader` | "You (X)" score, draws, "Computer (O)" score, plus a turn indicator: "Your turn", "Computer is thinking...", "You win!", "Computer wins", "Draw" |
| `DifficultySelector` | `SingleChoiceSegmentedButtonRow` (Easy / Unbeatable) |
| `GameBoard` | Square 3x3 grid using `BoxWithConstraints`/`aspectRatio(1f)`, adapts to portrait and landscape |
| `CellView` | Clickable cell with a drawn/animated X or O mark; highlights the winning line; disabled when not clickable. Accessible `contentDescription` ("Row 1, column 2, empty") |
| `ControlsRow` | "Restart Game" and "Reset Score" buttons (Reset Score gets a confirmation only if the tally is non-zero; optional) |
| `ui/theme/` | Material 3 color scheme (light/dark, dynamic color on Android 12+), typography, shapes |

**UX rules:** Minimalist look, 48dp+ touch targets, subtle animation on mark placement and win highlight, all strings in `res/values/strings.xml` (no hardcoded text), state survives configuration change (`ViewModel` + `rememberSaveable` for UI-only state).

---

### Module 4: Project Scaffold & Quality
**Agents:** DEVOPS-AGENT + DATABASE-AGENT (N/A, reassign to scaffold)

- Gradle Kotlin DSL, `libs.versions.toml`, `settings.gradle.kts`, `app/build.gradle.kts`
- `AndroidManifest.xml`, launcher icon, `themes.xml`
- ktlint or detekt, Android Lint config
- `.github/workflows/android.yml`
- `.gitignore`, `README.md` (build/run instructions)

---

## FILE STRUCTURE

```
TicTacToe/
├── settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/values/{strings,colors,themes}.xml
│       │   └── java/com/example/tictactoe/
│       │       ├── MainActivity.kt
│       │       ├── domain/   (Models.kt, GameRules.kt, AiStrategy.kt, RandomAi.kt, MinimaxAi.kt, AiFactory.kt)
│       │       └── ui/
│       │           ├── game/ (GameViewModel.kt, GameScreen.kt, ScoreHeader.kt, GameBoard.kt, CellView.kt, ControlsRow.kt, DifficultySelector.kt)
│       │           └── theme/ (Color.kt, Theme.kt, Type.kt)
│       ├── test/             (GameRulesTest, RandomAiTest, MinimaxAiTest, GameViewModelTest)
│       └── androidTest/      (GameScreenTest)
└── .github/workflows/android.yml
```

---

## AGENT MAPPING

| Agent | Responsibility | Skills |
|-------|---------------|--------|
| DATABASE-AGENT | N/A (no DB). Reassigned: data model types in `Models.kt` | skills/DATABASE.md (reference only) |
| BACKEND-AGENT | Game engine, AI, ViewModel | skills/BACKEND.md |
| FRONTEND-AGENT | Compose UI, theme, resources | skills/FRONTEND.md |
| DEVOPS-AGENT | Gradle scaffold, CI workflow, lint config | skills/DEPLOYMENT.md |
| TEST-AGENT | Unit, ViewModel and Compose UI tests | skills/TESTING.md |
| REVIEW-AGENT | Correctness, concurrency, accessibility, performance review | - |

---

## PHASE EXECUTION PLAN

**Phase 1: Foundation (3 agents in parallel)**
- DEVOPS-AGENT: Gradle project, manifest, version catalog, CI, lint
- BACKEND-AGENT: `Models.kt` and `GameRules.kt` (the shared contract)
- FRONTEND-AGENT: Theme, `strings.xml`, `MainActivity`, and static preview of `GameBoard`/`CellView`

**Validation Gate 1:** `./gradlew assembleDebug` succeeds; the project syncs with no errors.

**Phase 2: Modules (logic and UI in parallel)**
- Engine: `RandomAi`, `MinimaxAi`, `AiFactory` (BACKEND-AGENT)
- ViewModel: `GameViewModel` wired to the engine (BACKEND-AGENT)
- UI: `GameScreen`, `ScoreHeader`, `ControlsRow`, `DifficultySelector`, win highlight and animations (FRONTEND-AGENT), bound to the `GameUiState` contract

**Validation Gate 2:** `./gradlew lintDebug ktlintCheck` (or `detekt`) and `./gradlew assembleDebug`.

**Phase 3: Quality (3 agents in parallel)**
- TEST-AGENT: unit, ViewModel, and Compose UI tests
- REVIEW-AGENT: race conditions on restart during the delay, double score increment, accessibility, recomposition efficiency
- RESEARCH-AGENT: validate current AGP/Compose BOM/Kotlin versions and Material 3 APIs

**Final Validation:** `./gradlew test assembleDebug`, and launch on an emulator for a manual smoke test.

---

## TEST PLAN

| Area | Cases |
|------|-------|
| `GameRules` | All 8 winning lines for X and O; draw board; in-progress board; winning line indices returned |
| `RandomAi` | Always returns an empty cell; with one empty cell, returns it; deterministic with a seeded `Random` |
| `MinimaxAi` | Takes an immediate win; blocks an immediate loss; **never loses**: exhaustive simulation against all possible human move sequences (every game ends in a computer win or draw); an empty board and a full board are handled |
| `GameViewModel` | Human move places X; the computer replies after 500 ms (virtual time, `advanceTimeBy`); clicks ignored while thinking, on an occupied cell, and after game over; score increments once on win/loss/draw; Restart keeps score; Reset clears score; restart during the delay cancels the pending move; difficulty change restarts the board |
| Compose UI | Tap a cell shows X; the header shows the correct turn text; Restart and Reset buttons work; the difficulty toggle works |

**Coverage target:** 80%+ on `domain/` and `ui/game/GameViewModel`.

---

## VALIDATION GATES

| Gate | Commands |
|------|----------|
| 1 | `./gradlew assembleDebug` |
| 2 | `./gradlew lintDebug` + `./gradlew ktlintCheck` (or `detekt`) |
| 3 | `./gradlew testDebugUnitTest` (+ `jacocoTestReport` for coverage), `./gradlew connectedDebugAndroidTest` (emulator) |
| Final | `./gradlew clean test assembleDebug`, install the APK on an emulator and play through win, loss, draw, restart and reset |

---

## ACCEPTANCE CRITERIA

- [ ] The app compiles and runs on an emulator/device with no changes
- [ ] The human is always X and moves first; the computer is always O
- [ ] Win (rows, columns, diagonals) and draw are detected immediately after every move
- [ ] Easy mode plays random empty cells; Unbeatable mode never loses (verified by exhaustive test)
- [ ] The computer waits ~500 ms before moving and the board ignores taps while it thinks
- [ ] Restart Game clears the board and keeps the score; Reset Score clears the tally
- [ ] The header shows both scores and the correct turn/result indicator
- [ ] State is managed via `StateFlow` in a `ViewModel` and survives rotation
- [ ] 80%+ unit test coverage on the domain layer and ViewModel
- [ ] Lint passes and CI builds successfully

---

## CONFIGURATION

No secrets or environment variables are required (fully offline, no network permission declared in the manifest).

---

## NEXT STEP

Execute with parallel agents:
/execute-prp PRPs/tic-tac-toe-prp.md
