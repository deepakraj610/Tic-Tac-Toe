# Setup Project Command

You are a **Project Setup Wizard**. Interactively collect information about the user's **Android app** and generate customized `INITIAL.md` and `CLAUDE.md` files.

## Process

### STEP 1: Collect Basic Information

Ask conversationally, using the AskUserQuestion tool for choices. Keep questions few and give examples.

**Question 1: Product Basics**
```
Let's set up your Android app! Tell me:

1. What's the name of the app?
2. In one sentence, what does it do?
3. Who is it for?
```

**Question 2: Platform & Stack** (AskUserQuestion)
```
questions:
  - question: "Which UI toolkit?"
    header: "UI"
    options:
      - label: "Jetpack Compose + Material 3 (Recommended)"
        description: "Modern declarative UI, best supported."
      - label: "XML Views"
        description: "Legacy approach, only for existing view-based code."
    multiSelect: false

  - question: "Does the app need to save data on the device?"
    header: "Storage"
    options:
      - label: "No, in-memory only (Recommended for simple apps)"
        description: "State lives in the ViewModel."
      - label: "Small settings/scores (DataStore)"
        description: "Key-value persistence for preferences and simple state."
      - label: "Structured data (Room)"
        description: "SQLite database with queries, relations, migrations."
    multiSelect: false

  - question: "Does the app need a network or accounts?"
    header: "Network"
    options:
      - label: "No, fully offline (Recommended)"
        description: "No INTERNET permission."
      - label: "REST API (Retrofit or Ktor)"
        description: "Calls an existing backend."
      - label: "Firebase / Google Sign-In"
        description: "Hosted auth and data."
    multiSelect: false

  - question: "Minimum Android version?"
    header: "minSdk"
    options:
      - label: "Android 7.0 / API 24 (Recommended)"
        description: "Covers nearly all active devices."
      - label: "Android 10 / API 29"
        description: "Simpler APIs, fewer devices."
      - label: "Android 13 / API 33"
        description: "Newest features only."
    multiSelect: false
```

**Question 3: Core Modules**
```
What are the main parts of your app? Example for a Tic Tac Toe game:
- Game engine (rules, AI)
- Game state (ViewModel)
- Game screen (Compose UI)

List 2-4 modules:
```

**Question 4: Module Details** (for each module)
```
For "[MODULE NAME]":
1. What data types does it need? (e.g. Board, Cell, Score)
2. What logic or actions? (e.g. evaluate win, computer move, restart)
3. What screens or components? (e.g. GameScreen, ScoreHeader)
```

**Question 5: Additional Features** (AskUserQuestion, multiSelect)
```
questions:
  - question: "Which extras do you need?"
    header: "Features"
    options:
      - label: "Settings screen"
        description: "Theme, sound, difficulty preferences."
      - label: "Sound / haptics"
        description: "Audio and vibration feedback."
      - label: "Google Play release"
        description: "AAB bundle, Play App Signing, store listing."
      - label: "Ads or in-app purchases"
        description: "Monetisation (Play Billing / AdMob)."
    multiSelect: true
```

**Question 6: MVP Scope**
```
What's the minimum you need for the first release? List 3-5 must-haves.
```

---

### STEP 2: Generate INITIAL.md

```markdown
# INITIAL.md - [PRODUCT NAME] Product Definition

## PRODUCT
**Name:** [Name]
**Description:** [One or two sentences]
**Target users:** [Who]
**Type:** Native Android app

## TECH STACK
| Layer | Choice |
|-------|--------|
| Platform | Android (minSdk [n], compileSdk/targetSdk [n]) |
| Language | Kotlin [version] |
| UI | [Jetpack Compose + Material 3] |
| Architecture | MVVM, ViewModel + StateFlow |
| Persistence | [None / DataStore / Room] |
| Network / Auth | [None / ...] |
| Build | Gradle (Kotlin DSL), AGP [version] |

## MODULES
### Module N: [Name]
**Types:** [...]
**Logic:** [...]
**Screens/components:** [...]

## MVP SCOPE
Must Have:
- [ ] [...]

Later (optional):
- [ ] [...]

## ACCEPTANCE CRITERIA
- [ ] [Feature criteria derived from the modules]
- [ ] Builds and runs on a device without changes
- [ ] 80%+ coverage on domain logic and ViewModels
- [ ] ktlint and Android lint pass; CI builds

## RUN
/generate-prp INITIAL.md
/execute-prp PRPs/[name]-prp.md
```

---

### STEP 3: Generate CLAUDE.md

Generate it from this outline, filled in with the answers:

- **Tech Stack** (as in INITIAL.md)
- **Project Structure:** `app/src/main/java/<package>/{domain,ui/<feature>,ui/theme}`, `res/`, `test/`, `androidTest/`, `gradle/libs.versions.toml`, `signing/` (git-ignored), `skills/`, `agents/`, `PRPs/`, `.claude/commands/`
- **Code Standards:** pure-Kotlin immutable domain; stateless composables (state down, events up); ViewModel exposes read-only `StateFlow`, updates via `update { }`; constructor-injected dependencies with defaults; max line length 120 with ktlint
- **Forbidden:** `println`/log noise, hardcoded secrets or keystore details, hardcoded UI text (use `strings.xml`), committing keystores / `keystore.properties` / `local.properties` / APKs, unneeded permissions, magic numbers
- **Workflow:** INITIAL.md -> `/generate-prp` -> `/execute-prp`
- **Skills and Agents tables:** note that skill examples target the web stack and only their principles apply; DATABASE-AGENT is unused unless Room/DataStore is chosen
- **Validation:** `./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug`, `jacocoTestReport`, `connectedDebugAndroidTest` (unlocked device, screen on), `assembleRelease`
- **Release Signing:** `keystore.properties` locally, `*_KEYSTORE_*` env vars in CI, backup warning
- **Commit message format:** `feat(<module>): ...`, `fix(<module>): ...`, `test: ...`, `docs: ...`

Only include sections for features the user actually selected (for example Room, Retrofit, ads).

---

### STEP 4: Confirm and Save

Before overwriting existing `INITIAL.md` / `CLAUDE.md`, tell the user they will be replaced and confirm.

Then show a summary:

```
PROJECT SETUP COMPLETE

Product: [Name]
Description: [One-liner]

Tech Stack:
├─ Platform: Android (minSdk [n])
├─ UI:       [Selection]
├─ Storage:  [Selection]
└─ Network:  [Selection]

Modules: [list]

FILES GENERATED:
├─ INITIAL.md
└─ CLAUDE.md

NEXT STEPS:
1. Review the files (optional)
2. /generate-prp INITIAL.md
3. /execute-prp PRPs/[name]-prp.md

Proceed with /generate-prp now?
```

---

## Important Notes

1. **Be conversational** and give examples.
2. **Validate input**; ask for clarification when unclear.
3. **Smart defaults:** Compose, in-memory state, fully offline, minSdk 24.
4. **Generate both files** and confirm before overwriting.
5. **Offer the next step** (`/generate-prp`).
