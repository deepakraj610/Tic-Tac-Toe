# INITIAL.md - Product Definition

> Edit this, then run `/generate-prp INITIAL.md`

---

## PRODUCT

**Name:** Tic Tac Toe

**Description:**
A minimalist, strictly single-player Tic Tac Toe game for Android. The human always plays X and moves first; the computer always plays O. It works fully offline, with instant play and no matchmaking.

**Target users:** Casual mobile gamers, commuters wanting a quick mental break, and children learning basic strategic thinking.

**Type:** Native Android app (offline, single-player)

---

## TECH STACK

| Layer | Choice |
|-------|--------|
| Platform | Android (minSdk 24, compileSdk/targetSdk 36) |
| Language | Kotlin 2.1 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, `ViewModel` + `StateFlow` |
| Async | Kotlin Coroutines |
| Build | Gradle (Kotlin DSL), AGP 8.13 |
| Backend / Database / Auth | None |
| Payments | None |

---

## MODULES

### Module 1: Game Engine (pure Kotlin)

**Types:** `Player`, `Cell`, `Difficulty`, `GameStatus` (InProgress, Won(winner, line), Draw), `Score`, `GameUiState`

**Logic:**
- `GameRules.evaluate(board)`: win on any of 8 lines, draw when the board is full
- `RandomAi`: random empty cell (Easy)
- `MinimaxAi`: Minimax with alpha-beta and depth-aware scoring, never loses (Unbeatable)
- `AiFactory`: Difficulty to strategy

### Module 2: Game State

**`GameViewModel`** exposes `uiState: StateFlow<GameUiState>`:
- `onCellClicked(index)`, `onRestartGame()`, `onResetScore()`, `onDifficultyChanged(difficulty)`
- The computer moves after a 500 ms delay; pending moves are cancelled on restart, reset or difficulty change
- Score is incremented once per finished round

### Module 3: Game UI (Compose)

**Screen:** a single `GameScreen`
- Header with scores (You / Draws / Computer) and turn or result text
- Difficulty selector (Easy / Unbeatable)
- Responsive 3x3 board with animated X/O marks and winning-line highlight
- "Restart Game" (keeps score) and "Reset Score" buttons
- Portrait and landscape layouts, accessible cell descriptions

### Module 4: Quality and Release

- Unit tests (rules, both AIs, ViewModel), Compose UI tests
- ktlint, Android lint, JaCoCo coverage
- GitHub Actions CI, signed release APK, GitHub release

---

## MVP SCOPE

Must Have:
- [x] 3x3 grid, human X first, computer O
- [x] Immediate win and draw detection
- [x] Easy (random) and Unbeatable (Minimax) AI
- [x] 500 ms computer move delay
- [x] Scores, turn indicator, Restart Game, Reset Score
- [x] StateFlow-based MVVM

Later (optional):
- [ ] Persist scores across app restarts (DataStore)
- [ ] Confirmation before Reset Score
- [ ] Tablet layout width cap
- [ ] Google Play release (AAB, Play App Signing)

---

## ACCEPTANCE CRITERIA

- [x] The app compiles and runs on a device without changes
- [x] The unbeatable AI never loses (verified by an exhaustive test)
- [x] The computer waits about 500 ms and the board ignores taps while it thinks
- [x] Restart keeps the score; Reset Score clears it
- [x] State survives rotation
- [x] 80%+ coverage on the domain layer and ViewModel
- [x] Lint and ktlint pass; CI builds

---

## RUN

```bash
/generate-prp INITIAL.md
/execute-prp PRPs/[name]-prp.md
```
