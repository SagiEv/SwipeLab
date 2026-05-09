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

## 6. TESTING REQUIREMENTS (STRICT)
* Test Coverage: Every time a new feature or code modification is added to the backend or frontend, tests MUST be added or updated along with the implementation.
* Minimum Scenarios: Test coverage must include at least one **happy flow** (expected successful behavior) and at least one **edge case** (failure, validation error, or boundary condition) for the new code.

## 7. ERROR HANDLING & LOGGING
* Backend: Utilize centralized exception handling (e.g., `@ControllerAdvice` in Spring Boot) and maintain consistent API error responses. Avoid scattered try-catch blocks.
* Frontend: Handle API failures gracefully with appropriate UI feedback (e.g., toasts, error boundaries). Avoid silent failures or unhandled promise rejections.

## 8. SECURE CODING & SECRETS
* Never hardcode API keys, credentials, or sensitive data in the code. Always rely on environment variables.
* Ensure `.env` and other secret configuration files remain correctly excluded via `.gitignore`.

## 9. CODE QUALITY & CLEANUP
* Ensure all code strictly follows the outlined Hexagonal/DDD architecture before finalizing changes.
* Always clean up temporary artifacts, `console.log`s, unused imports, and commented-out code.