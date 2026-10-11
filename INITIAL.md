# INITIAL.md - Define Your Product

> Fill this out, then run `/generate-prp INITIAL.md`

---

## PRODUCT

**Name:** Tic Tac Toe

**Description:** 
This application is designed for casual mobile gamers, commuters seeking quick mental breaks, and children learning basic strategic thinking. Because it is strictly single-player against the computer, it targets users who want instant, offline gameplay without waiting for online matchmaking or coordinating with friends.

To get the best results from Claude Code, structure your request using the RISE framework (Role, Input, Steps, Expectation) to ensure the generated architecture and code are production-ready.

Copy and paste the following specification directly into your terminal environment:

Role:
Act as an Expert Android Developer and Game Architect specializing in Kotlin and Jetpack Compose.

Input:
I need a complete Android mobile application for a Tic Tac Toe game. The app must be exclusively single-player, where the human user always plays against a computer AI opponent.

Steps:

Scaffold the Project: Initialize a modern Android project using Kotlin and Jetpack Compose for the UI layer. Ensure a clean, decoupled architecture (MVVM).

Design the UI/UX:

Create a modern, minimalist interface centered around a responsive 3x3 grid.

Include a top header displaying the current scores (Player vs. Computer) and the current turn indicator.

Add a "Restart Game" button to clear the board without resetting the total session score.

Add a "Reset Score" button to clear the session tally.

Implement Game Mechanics:

The human player is always 'X' and goes first. The computer is always 'O'.

Standard Tic Tac Toe rules apply: first to align 3 symbols horizontally, vertically, or diagonally wins.

Implement immediate win/draw detection after every move.

Develop the AI Opponent:

Implement two difficulty levels:

Easy: The computer selects a random available empty cell.

Unbeatable: The computer uses the Minimax algorithm to ensure it either wins or forces a draw.

Add a slight, artificial delay (e.g., 500ms) to the computer's move so it feels natural rather than instantaneous.

Manage State: Use Kotlin StateFlow or LiveData within a ViewModel to manage board state, score tallies, and active player turns, ensuring the UI recomposes efficiently.

Expectation:
Generate the necessary project structure, build.gradle dependencies, and the complete, well-documented Kotlin code for the UI components, ViewModel, and Game Logic. The output should be ready to compile and run on an Android emulator or physical device without requiring major logic corrections

**Type:** SaaS

---

## TECH STACK

| Layer | Choice |
|-------|--------|
| Backend | FastAPI + Python |
| Frontend | React + TypeScript + Vite |
| Database | PostgreSQL |
| Auth | JWT + Google OAuth |
| UI | Chakra UI / Tailwind |
| Payments | None / Stripe / Dodo |

---

## MODULES

### Module 1: Authentication (Built-in)

**Models:** User, RefreshToken

**Endpoints:**
- POST /auth/register, /auth/login, /auth/refresh
- GET /auth/me, /auth/google

**Pages:** /login, /register, /profile

---

### Module 2: [Your Core Module]

**Description:** [What does this module do?]

**Models:**
```
[ModelName]:
  - id, user_id (FK)
  - [field]: [type]
  - created_at, updated_at
```

**Endpoints:**
```
GET    /api/[resource]      - List all
POST   /api/[resource]      - Create
GET    /api/[resource]/{id} - Get one
PUT    /api/[resource]/{id} - Update
DELETE /api/[resource]/{id} - Delete
```

**Pages:**
```
/[resource]           - List
/[resource]/new       - Create
/[resource]/{id}      - Detail
/[resource]/{id}/edit - Edit
```

---

### Module 3: [Another Module]

[Same structure as above]

---

### Module 4: Dashboard (Optional)

**Pages:** /dashboard, /settings

---

## MVP SCOPE

Must Have:
- [x] User registration/login
- [ ] [Core feature 1]
- [ ] [Core feature 2]

---

## ACCEPTANCE CRITERIA

- [ ] Users can register and login
- [ ] Users can CRUD their [resources]
- [ ] 80%+ test coverage
- [ ] Docker builds successfully

---

## RUN

```bash
/generate-prp INITIAL.md
/execute-prp PRPs/[name]-prp.md
```
