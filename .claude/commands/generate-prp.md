# Generate PRP Command

You are a **Product PRP Generator**. Create a focused implementation blueprint for a native **Android** app (Kotlin + Jetpack Compose).

## Input
Read the product definition file: INITIAL.md (defaults to INITIAL.md)

---

## Context Files

Read these files before generating:
- `CLAUDE.md` (project rules)
- `agents/ORCHESTRATOR.md` (orchestrator role)

---

## Step 1: Parse INITIAL.md

Extract from the input file:

```yaml
PRODUCT:
  name: [Product name]
  description: [What it does]
  target_users: [Who it is for]

TECH_STACK:
  platform: [Android, minSdk / compileSdk / targetSdk]
  language: [Kotlin version]
  ui: [Jetpack Compose + Material 3]
  architecture: [MVVM, ViewModel + StateFlow]
  persistence: [None / DataStore / Room]
  network: [None / Retrofit / Ktor]
  auth: [None / Google Sign-In / Firebase]

MODULES:
  - name: [Module 1]
    types: [Data classes, enums, sealed types]
    logic: [Pure Kotlin classes and functions]
    screens: [Composable screens and key components]

MVP_SCOPE:
  - [Feature 1]
  - [Feature 2]

ACCEPTANCE_CRITERIA:
  - [Criterion 1]
  - [Criterion 2]
```

**If INITIAL.md contradicts itself** (for example an Android description next to a web tech stack table), ask the user which target to use before generating. Do not guess.

---

## Step 2: Map Agents to Modules

```yaml
BACKEND-AGENT:        # domain + state
  modules: [Pure-Kotlin logic, AI/algorithms, ViewModels]
  skills: [skills/BACKEND.md]   # principles only

FRONTEND-AGENT:       # Compose
  modules: [Screens, components, theme, strings.xml]
  skills: [skills/FRONTEND.md]  # principles only

DEVOPS-AGENT:
  tasks: [Gradle + version catalog, manifest, CI workflow, lint/ktlint, signing]
  skills: [skills/DEPLOYMENT.md]

TEST-AGENT:
  coverage: [Unit tests for domain + ViewModel, Compose UI tests]
  skills: [skills/TESTING.md]

REVIEW-AGENT:
  review: [Correctness, coroutine/concurrency, accessibility, manifest/permissions]

RESEARCH-AGENT:
  research: [Library/AGP/Kotlin compatibility, Play target-SDK rules, API stability]

DATABASE-AGENT:
  only_if: persistence is Room/DataStore, otherwise unused
```

---

## Step 3: Generate PRP File

Create `PRPs/[product-name-kebab-case]-prp.md` with this structure:

```markdown
# PRP: [Product Name]

> Implementation blueprint for parallel agent execution

---

## METADATA

| Field | Value |
|-------|-------|
| **Product** | [Name] |
| **Type** | Native Android app |
| **Version** | 1.0 |
| **Created** | [Date] |
| **Complexity** | [Low / Medium / High] |

---

## PRODUCT OVERVIEW

**Description:** [What this product does]
**Target users:** [Who]
**Value Proposition:** [Why users want it]

**MVP Scope:**
- [ ] [Feature 1]
- [ ] [Feature 2]

**Out of scope:** [Explicit list]

---

## TECH STACK

| Layer | Technology |
|-------|------------|
| Language | Kotlin [version], JVM 17 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM, unidirectional data flow, ViewModel + StateFlow |
| Async | Coroutines |
| Persistence / Network | [None or choice] |
| Testing | JUnit, kotlinx-coroutines-test, Turbine, Compose UI test |
| Build | Gradle Kotlin DSL, version catalog, AGP [version] |
| CI | GitHub Actions |

**SDK:** minSdk [n], compileSdk/targetSdk [n]. Package: `[com.example.app]`.

---

## DATA MODELS (in-memory Kotlin types, or Room entities if persisted)

[Kotlin snippets of enums, sealed interfaces and data classes, including the UI state class]

---

## MODULES

### Module 1: [Domain / Engine]
**Agents:** BACKEND-AGENT + TEST-AGENT
[Components, responsibilities, public API contract]

### Module 2: [State / ViewModel]
**Agents:** BACKEND-AGENT
[State flow, intents, side effects, cancellation rules, injected dependencies]

### Module 3: [UI]
**Agents:** FRONTEND-AGENT
| Component | Description |
|-----------|-------------|
| [Screen / composable] | [What it shows and how it behaves] |

[UX rules: touch targets >= 48dp, strings in strings.xml, rotation survival, accessibility]

### Module 4: Project Scaffold & Quality
**Agents:** DEVOPS-AGENT
[Gradle, manifest, lint/ktlint, CI, signing]

---

## FILE STRUCTURE

[Tree under app/src/main, test, androidTest]

---

## AGENT MAPPING

[Table of agents, responsibilities, skills]

---

## PHASE EXECUTION PLAN

**Phase 1: Foundation (parallel)**
- DEVOPS-AGENT: Gradle project, manifest, version catalog, CI, lint
- BACKEND-AGENT: shared types + core rules (the contract other agents code against)
- FRONTEND-AGENT: theme, strings.xml, MainActivity, base components

**Validation Gate 1:** `./gradlew assembleDebug`

**Phase 2: Modules (logic and UI in parallel)**
- BACKEND-AGENT: algorithms, ViewModel, their unit tests
- FRONTEND-AGENT: screens bound to the UI-state contract

**Validation Gate 2:** `./gradlew ktlintCheck lintDebug assembleDebug`

**Phase 3: Quality (parallel)**
- TEST-AGENT: audit and fill unit + Compose tests
- REVIEW-AGENT: read-only review with severity and file:line
- RESEARCH-AGENT: verify versions (AGP / Gradle / Kotlin / Compose BOM / compileSdk compatibility)

**Final Validation:** full Gradle pipeline, then instrumented tests and a device smoke test

---

## TEST PLAN

[Table: area -> cases. Include "never loses" or other exhaustive checks where the logic allows]

---

## VALIDATION GATES

| Gate | Commands |
|------|----------|
| 1 | `./gradlew assembleDebug` |
| 2 | `./gradlew ktlintCheck lintDebug` |
| 3 | `./gradlew testDebugUnitTest jacocoTestReport` |
| 4 | `./gradlew connectedDebugAndroidTest` (unlocked device, screen on) |
| Final | `./gradlew clean ktlintCheck lintDebug testDebugUnitTest assembleDebug assembleRelease` |

---

## ACCEPTANCE CRITERIA

[Checklist from INITIAL.md, plus: builds, lint clean, 80%+ coverage on domain/ViewModel]

---

## CONFIGURATION

No secrets in the repo. Release signing uses `keystore.properties` (git-ignored) or env vars.

---

## NEXT STEP

/execute-prp PRPs/[product-name]-prp.md
```

---

## Output

Save to: `PRPs/[product-name-kebab-case]-prp.md`

Report to user:
```
PRP GENERATED

Product: [Name]
File: PRPs/[name]-prp.md
Modules: [count]
Types: [count]
Screens/components: [count]

Phases:
1. Foundation: 3 agents parallel
2. Modules: logic + UI parallel
3. Quality: 3 agents parallel

NEXT: /execute-prp PRPs/[name]-prp.md
```
