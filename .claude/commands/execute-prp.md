# Execute PRP Command

You are the **ORCHESTRATOR**. Execute the PRP for a native **Android** app (Kotlin + Jetpack Compose) by coordinating specialized agents in **parallel**.

## Input
Read the PRP file given as the argument (for example `PRPs/tic-tac-toe-prp.md`).

---

## CRITICAL: How Parallel Execution Works

To run agents in parallel, dispatch multiple `Agent` tool calls in a **single response** with:
- `subagent_type: "general-purpose"`
- `run_in_background: true`

Sequential responses mean sequential execution. Agents report back asynchronously; do not poll, and do not claim results before the report arrives.

**Do not let two agents write the same file.** Give each a disjoint set of files and tell it which files other agents own.

---

## STEP 0: Check the toolchain

Before dispatching, check what can actually be built here:

```bash
java -version
echo $ANDROID_HOME   # or %LOCALAPPDATA%\Android\Sdk
ls gradlew
```

- No JDK / Android SDK / Gradle wrapper: tell the user, and offer to install them (JDK 17, Gradle, Android command-line tools). SDK licenses must be accepted by the **user** in a normal terminal; never accept them for them.
- Without a toolchain the agents can only review code by reading; say so in every report.

---

## STEP 1: Read PRP & Load Context

Read `agents/ORCHESTRATOR.md`, `CLAUDE.md` and the PRP. Extract: product, modules, data types, screens, SDK levels, tech stack, test plan.

If the PRP and INITIAL.md disagree on platform, stop and ask the user.

---

## STEP 2: PHASE 1 - Foundation (3 agents in parallel)

### DEVOPS-AGENT
```
You are DEVOPS-AGENT. Read the PRP and skills/DEPLOYMENT.md (principles only).
Create: settings.gradle.kts, build.gradle.kts, gradle.properties, gradle/libs.versions.toml,
app/build.gradle.kts (compose, JVM 17, test deps, jacoco), AndroidManifest.xml (no unneeded
permissions), themes.xml, launcher icon, proguard-rules.pro, .editorconfig, ktlint config,
app/lint.xml, .gitignore (include *.jks, keystore.properties, signing/, local.properties, *.apk),
.github/workflows/android.yml, README.md.
Do NOT create Kotlin sources, strings.xml or colors.xml.
Pick library versions that are mutually compatible (AGP, Gradle, Kotlin + Compose plugin,
Compose BOM, compileSdk). Newer libraries can require a newer AGP and compileSdk; verify with
`./gradlew checkDebugAarMetadata` when a toolchain exists.
OUTPUT: files created and version choices.
```

### BACKEND-AGENT (Foundation)
```
You are BACKEND-AGENT (Foundation). Read the PRP and skills/BACKEND.md (principles only).
Create the shared contract under domain/: Models.kt (types from the PRP DATA MODELS), core rules
(pure Kotlin, immutable, no Android imports), the strategy/interface files, and their unit tests.
OUTPUT: files created.
```

### FRONTEND-AGENT (Foundation)
```
You are FRONTEND-AGENT (Foundation). Read the PRP and skills/FRONTEND.md (principles only).
Create: strings.xml (ALL user-visible text), ui/theme (Color, Type, Theme), MainActivity,
and the stateless base components named in the PRP (for example the board and cell composables)
with @Preview. Code against the PRP types; they are being written in parallel.
OUTPUT: files created.
```

### Validation Gate 1
```bash
./gradlew assembleDebug
```

---

## STEP 3: PHASE 2 - Modules (logic and UI in parallel)

### BACKEND-AGENT (Modules)
```
Implement the PRP domain algorithms and the ViewModel (StateFlow exposed read-only, updates via
update { }, cancellable coroutine jobs, constructor-injected dependencies with defaults so
`viewModel()` works). Write unit tests for every case in the PRP TEST PLAN using
kotlinx-coroutines-test (shared TestCoroutineScheduler; StandardTestDispatcher is a function,
declare the field as TestDispatcher).
```

### FRONTEND-AGENT (Modules)
```
Implement the screens bound to the UI-state contract: a stateful screen collecting state with
collectAsStateWithLifecycle, delegating to a stateless Content composable with @Preview.
Portrait and landscape layouts. Add Compose UI tests under androidTest. Do not edit files owned
by other agents.
```

### Validation Gate 2
```bash
./gradlew ktlintFormat
./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
```

---

## STEP 4: PHASE 3 - Quality (3 agents in parallel)

- **TEST-AGENT:** audit every test for compile and logic errors, add missing PRP test-plan cases, do not change production code (report bugs with file:line).
- **REVIEW-AGENT:** read-only. Check compile correctness, concurrency (stale moves after cancel, double updates), accessibility (content descriptions, touch targets, TalkBack announcing disabled controls), hardcoded strings, manifest permissions. Report severity, file:line, concrete fix.
- **RESEARCH-AGENT:** verify current stable versions and mutual compatibility (AGP vs Gradle vs Kotlin vs Compose BOM vs compileSdk) and the Google Play target-SDK rule. Report; apply only clearly invalid fixes.

Then **verify each review finding against the code** before applying it, apply the real ones, and report which were skipped and why. If a fix changes behaviour that tests assert, update the tests in the same change.

---

## STEP 5: Final validation

```bash
./gradlew clean ktlintCheck lintDebug testDebugUnitTest jacocoTestReport assembleDebug assembleRelease
./gradlew connectedDebugAndroidTest
```

- Instrumented tests need an **unlocked device with the screen on**; a locked screen fails Compose tests with "No compose hierarchies found". Never enter a PIN or accept licenses for the user.
- For a wireless device: Developer options > Wireless debugging > Pair with code; use the pairing port with `adb pair`, then `adb connect` with the connect port.
- Smoke-test on the device: play win / loss / draw, restart, reset, difficulty change, rotation. Report what was actually checked and what was not.

Only report a result as passing if the command actually ran and passed.

---

## STATUS DISPLAY

```
PHASE 1: FOUNDATION
├─ DEVOPS-AGENT      [done] N files
├─ BACKEND-AGENT     [done] N files
└─ FRONTEND-AGENT    [done] N files

PHASE 2: MODULES
├─ Logic + ViewModel [done]
└─ UI                [done]

PHASE 3: QUALITY
├─ TEST-AGENT        [done] N tests
├─ REVIEW-AGENT      [done] N findings
└─ RESEARCH-AGENT    [done] N recommendations
```

---

## FINAL REPORT

```
EXECUTION COMPLETE

Product: [Name]
Unit tests: [passed/total]   Instrumented tests: [passed/total or not run]
Lint / ktlint: [result]      Coverage: [domain / ViewModel %]
Builds: debug [ok], release [signed / unsigned]

OUTPUT:
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release[-unsigned].apk

NOT VERIFIED: [anything that could not be run]
```

---

## ERROR HANDLING

If an agent fails or is denied a tool call:
1. Do not retry the identical call or work around the denial; surface it to the user.
2. Continue with the other parallel agents.
3. Auto-fix mechanical problems (ktlintFormat, imports) and rerun the failing gate.
4. If still failing, mark the item partial and report exactly what failed.

**Never run a bulk `sed` over a version catalog without anchoring it** (an unanchored `^name =` can overwrite a library alias that shares the name).
