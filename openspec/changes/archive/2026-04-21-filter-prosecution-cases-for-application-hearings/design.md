## Context

The Hearing Query API exposes a `getHearingDetails` endpoint that returns full hearing data, including prosecution cases (defendants and their offences). For **application hearings** — hearings driven by a `CourtApplication` — the data model has two parallel places where offences can appear: at the prosecution-case/defendant level, and at the court-application-case level.

The previous implementation (`filterOutOffences`) tried to reconcile this by keeping prosecution cases but nullifying the offences list inside each defendant when the application carried its own offences. This left a skeleton prosecution-cases structure (cases and defendants, but no offences) in the response, which was misleading and inconsistent with what consumers expected.

The corrected approach removes prosecution cases entirely when the application hearing has offences. The court-application offences are the authoritative source in that scenario.

## Goals / Non-Goals

**Goals:**
- When an application hearing has offences on its court application cases, return `prosecutionCases: null` in the hearing details response.
- When an application hearing has **no** offences on its court application cases, return prosecution cases unchanged.
- When the hearing is not an application hearing, return the payload unchanged (no filtering applied).
- Simplify the filtering logic by removing the multi-level offence traversal.

**Non-Goals:**
- Changing the REST API contract or response schema (prosecution cases is already nullable).
- Altering how non-application hearings are handled.
- Modifying the write (command) side or event processing.

## Decisions

### Decision: Remove prosecution cases entirely instead of nullifying offences within them

**Rationale:** Consumers of the hearing details endpoint for application hearings should receive offence data exclusively from the court application cases. Leaving a skeleton prosecution-cases structure (cases + defendants, but `offences: null`) is ambiguous — consumers cannot tell if offences are absent because they were filtered or because none exist. A `null` prosecution cases list unambiguously signals "look at court applications for offence data."

**Alternative considered:** Keep the old behaviour of nullifying offences at the defendant level. Rejected because it produces a partial structure that is harder to reason about and was not the intended consumer experience.

### Decision: Add early-exit for application hearings with no offences

**Rationale:** If an application hearing has no offences attached to its court application cases, the prosecution-case offences (if any) are the only offence data available and should be preserved. The new `isApplicationHasNoOffences` guard covers this case before the filtering path is entered, keeping the logic explicit.

**Alternative considered:** Always remove prosecution cases for any application hearing. Rejected because it would discard valid offence data when the application has none of its own.

## Risks / Trade-offs

- **Risk:** Consumers that were previously relying on the skeleton prosecution-cases structure (cases/defendants without offences) may break. → **Mitigation:** The change is narrowly scoped to cases where application offences exist; those consumers should be using court application offences in that scenario. No known downstream consumers depend on the skeleton structure.
- **Trade-off:** `null` prosecution cases vs. an empty list — `null` is chosen to be consistent with the existing `withProsecutionCases(null)` pattern and because an empty list would imply "no cases" rather than "cases not applicable here".