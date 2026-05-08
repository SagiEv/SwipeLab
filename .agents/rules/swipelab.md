---
trigger: always_on
---

# SwipeLab Rules


## 1. IDENTITY & COMMUNICATION
* Tone: Technical, concise, objective.
* Efficiency: Skip apologies and greetings. Focus on code, RCA, and execution logs.
* Documentation: Comments explain "Why", not "What".

## 2. TECH STACK & ARCHITECTURE (STRICT)
* Frontend: React Native (Expo), TypeScript, Zustand, React Query. Use @/ alias. No Expo Router.
* Backend: Java 21, Spring Boot 3.2.x, PostgreSQL. Modular Hexagonal/DDD structure.
* Layers: Strict separation between api, application, domain, and infrastructure.

## 3. TWO-TIER MEMORY PROTOCOL (STRICT)
* Shared Memory (MEMORY.md):
  - Purpose: Strategic project state, milestones, and active GitHub Issues.
  - Usage: Read at start of session. Update ONLY when a task is completed or architecture changes.
  - Version Control: This file is tracked in Git.
* Local Scratchpad (LOCAL_SCRATCHPAD.md):
  - Purpose: Tactical task execution, terminal logs, and failed attempts.
  - Usage: Update continuously during the session to track current progress and avoid repeating errors.
  - Version Control: NEVER commit this file (must be in .gitignore).

## 4. GITHUB INTEGRATION
* Always verify the active GitHub Issue number before starting work.
* References to issues (e.g., #204) must be used in commit messages and memory updates.

## 5. AGENT BEHAVIOR
* Investigation First: Perform RCA and propose a solution before modifying files.
* Self-Healing: Retry failed terminal commands once after analyzing the error.
* Cognitive Strategy: Use ### Thought Process for complex tasks, followed by a Red Team self-review.