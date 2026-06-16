# Image Classification "Threshold" — Design Discussion

> Draft for the customer meeting. Goal: agree on **how to define when an image is "done"** so it
> leaves the classification stream and its result is reported to the researcher.
>
> **Assumption for this draft:** there are **enough experts** in the system. We can therefore treat
> expert votes as a first-class signal (weighting, tie-breaking, even finalisation) rather than a
> rare edge case.

---

## 1. The problem

Today every image stays in the classification stream indefinitely — users keep being served images
with no notion of "this one has enough answers." We want a **threshold**: a per-task rule that decides
when an image has collected *enough* classifications. When an image crosses its threshold:

1. It is **removed from the stream** — users are no longer served it.
2. Its **aggregated result** (the agreed label) is finalised and reported to the researcher.

The open questions are: *what exactly* do we count toward the threshold, and *whose* votes count, and
*how much*.

---

## 2. What we already have (no new concepts needed for the basics)

The system is already most of the way there:

| Concept | Where it lives today | Notes |
|---|---|---|
| `minClassificationsPerImage` (default **3**) | `Task` entity, per task, researcher-configurable | A *count* threshold already exists in the schema. |
| `consensusThreshold` (default **80%**) | `Task` entity, per task | An *agreement* threshold already exists too. |
| Per-image / per-(image, species) vote counts | `ClassificationRepository` | We can count answers per image and per question. |
| `UserResponse` = `YES, NO, DONT_KNOW, TRASH` | `Classification` entity | **Answers are NOT binary** — important for the definition. |
| Majority vote + consensus strength | `CredibilityCalculator` | Already computes "the agreed label" and "how strong agreement is." |
| Per-user **credibility score** (0–100) | `User` / `CredibilityCalculator` | Combines gold accuracy, majority agreement, expert kappa. |
| Expert vs. non-expert | `userRole == RESEARCHER` marks an expert | Experts already treated specially elsewhere; repo has dedicated expert-classification queries. |

> The repository even has a comment marking threshold-gated completion as a known *future enhancement*.
> So this is mostly **wiring existing pieces together**, not building from scratch.

---

## 3. Relevant entities & parameters

**Entities**
- **Task** — owns the threshold configuration; researcher edits it.
- **Image** — the unit whose life-cycle ends at the threshold.
- **(Image, querySpecies) pair** — the *real* unit, because one image can be asked about multiple
  species. The threshold likely applies per pair, not per raw image.
- **Classification** — one user's answer (`YES/NO/DONT_KNOW/TRASH`) to one (image, species) question.
- **User** — has a role (expert / non-expert) and a credibility score.

**Parameters the researcher might set (per task)**
- `minClassifications` — how many answers are required.
- `consensusThreshold` — how strong the agreement must be to call a result (e.g. ≥80%).
- `requiredExpertVotes` — how many expert answers are required (can be 0, 1, or more).
- (Optional) which answers "count" (e.g. do `DONT_KNOW` / `TRASH` count toward the total?).
- (Optional) weighting policy — equal vote vs. credibility-weighted vs. expert-driven.

---

## 4. Open question A — *count* threshold vs. *consensus* threshold

> "Is the threshold a number of YES, a number of total classifications, or an agreement level?"

| Option | Definition of "done" | Pros | Cons |
|---|---|---|---|
| **A1. Count of total answers** | Done when image has ≥ N answers (any answer). | Dead simple; predictable cost per image; already in schema (`minClassificationsPerImage`). | An image with 50/50 split is "done" but has no real answer. |
| **A2. Count of YES answers** | Done when ≥ N users said YES. | Good if the task is "find positives." | Asymmetric — a clearly-NO image never finishes; ambiguous for multi-class answers. |
| **A3. Consensus** | Done when agreement on one label ≥ X% (with a minimum number of voters). | Reports a *confident* label, not just "enough votes." | An always-split image never converges → needs a **cap** (see §6). |
| **A4. Hybrid (recommended)** | Done when **count ≥ N AND consensus ≥ X%**, OR a hard cap of M votes is reached. | Guarantees both *enough* data and a *confident* result; cap prevents images living forever. | Two/three parameters to explain to the researcher. |

**Note on non-binary answers:** because answers are `YES/NO/DONT_KNOW/TRASH`, a pure "count of YES"
definition (A2) is fragile. We should decide whether `DONT_KNOW`/`TRASH` count toward the total, or
are ignored, or route the image elsewhere (e.g. flagged as junk).

**Our leaning:** **A4** — it reuses both fields already on `Task`.

---

## 5. Open question B — whose votes count (experts, non-experts, credibility)

> "Should experts and non-experts count equally? What about credibility differences?"
>
> **Because we assume enough experts**, expert-driven options below are realistic, not theoretical —
> we can rely on experts being available for most images, not just a lucky few.

| Option | How votes are weighted | Pros | Cons |
|---|---|---|---|
| **B1. Equal vote** | Every classification counts as 1. | Simplest; easy to explain & defend statistically. | Wastes the trust signals (expertise, credibility) we already compute. |
| **B2. Required expert confirmation (recommended)** | Crowd collects votes as usual, **but the image is only "done" once it also has ≥ K expert votes that agree** (K configurable, default 1). | With enough experts this is achievable per image; gives every finalised result a ground-truth-quality stamp; directly uses the `RESEARCHER` role we already track. | Slower per image; needs enough expert throughput (we assume we have it). |
| **B3. Expert override / fast-track** | A sufficient set of agreeing expert votes can **finalise an image immediately**, short-cutting the crowd count. | Finalises easy/clear images fast; spends crowd effort only where experts disagree or are unsure. | A small expert set can be wrong; less crowd data collected for analysis. |
| **B4. Credibility-weighted vote** | Each vote weighted by the user's credibility score (experts naturally weigh most); threshold is on the *weighted* agreement. | Smooth — no hard expert/non-expert cliff; uses the credibility engine we already built; high-trust users converge images faster. | Harder to explain; "weight collected" ≠ "people who saw it" → still needs a **minimum distinct-humans** floor. |

**Recommended combination:** **B2 + B4** — collect credibility-weighted crowd votes *and* require a
small number of agreeing expert votes before finalising. The crowd gives scale and an agreement
signal; the required expert vote(s) give each result an authoritative stamp; credibility weighting
means a few low-trust users can't swing a result on their own.

**Things to clarify with the customer:**
- How many agreeing expert votes should finalise an image — 1, 2, or "experts must out-agree"?
- Should experts be able to **fast-track** (B3) clear images, or must the crowd count always be met too?
- If we weight by credibility (B4), what's the **minimum number of distinct humans** before we'll call
  a result, regardless of weight? (Avoids "one 99-credibility user finishes the image alone.")
- What happens if **experts disagree with the crowd majority**? (Expert wins? Keep collecting? Flag
  for the researcher?)

---

## 6. Cross-cutting decisions (apply to any option)

1. **Granularity** — threshold per **(image, species) pair**, not per raw image (the data is already
   keyed this way).
2. **Hard cap** — a maximum number of votes after which the image leaves the stream *even without
   consensus*, reported as "no agreement / ambiguous." Prevents immortal images.
3. **Expert routing (because we have enough experts)** — if an image needs expert votes to finish, the
   distribution logic should *preferentially* serve such images to experts so they don't stall waiting
   for a chance expert encounter.
4. **What we report** — the finalised label (crowd majority + expert label), the agreement %, the
   number of voters, and how many were experts, so the researcher can judge confidence.
5. **De-duplication** — a user must not be counted twice for the same (image, species) (already
   enforced via `existsByUsernameAndImage_IdAndQuerySpecies`).
6. **Where it's enforced** — image selection already flows through one place
   (`getNextImageForUser` / `getNextBatchForApi`); the threshold check belongs there, so "done"
   images simply stop being served.

---

## 7. Our recommendation to put in front of the customer

- **Definition (A4):** an image is done when it has **≥ N answers _and_ ≥ X% agreement _and_ ≥ K
  agreeing expert votes**, with a **hard cap of M** to retire stubborn images. N, X are already
  per-task fields; K (required expert votes) is a small addition.
- **Weighting (B2 + B4):** credibility-weighted crowd votes for scale and agreement, plus required
  expert confirmation for authority. Keep a **minimum distinct-humans floor** so weighting can't let
  one person finalise an image.
- **Expert routing:** since experts are plentiful, route expert-needing images to experts first so
  nothing stalls.
- **Reporting:** on crossing the threshold, finalise the (image, species) result (label + agreement %
  + voter count + expert count) and surface it to the researcher.

**Questions for the customer:**
1. Count-based, consensus-based, or both (our rec)?
2. Do `DONT_KNOW` / `TRASH` count toward the total, or are they ignored / flagged?
3. How many agreeing expert votes (K) should be required to finalise an image — 1, 2, or more?
4. Should experts be able to **fast-track** clearly-correct images, or must the crowd count always be met?
5. If experts and the crowd disagree, who wins — expert, keep collecting, or flag for the researcher?
6. What are sensible defaults for N (min answers), X (% agreement), and M (hard cap)?
