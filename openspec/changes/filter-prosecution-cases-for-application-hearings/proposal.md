## Why

When returning hearing details for application hearings, the previous logic only nullified offences at the defendant level when court application offences exist, but left the prosecution cases structure intact. The correct behaviour is to remove prosecution cases entirely when the application carries offences, ensuring the query response is not polluted with case-level data that belongs to the application context.

## What Changes

- Rename `filterOutOffences` → `filterOutProsecutionCases` in `HearingService` and `HearingQueryApi` to reflect the new intent.
- Change filtering logic: instead of selectively nullifying offences within each defendant inside prosecution cases, set `prosecutionCases` to `null` entirely when the application hearing has offences.
- Add an early-exit condition: if the application hearing has **no** offences, return the payload unchanged (prosecution cases are preserved).
- Remove helper methods `buildProsecutionCasesWithFilteredOffences`, `buildFilteredOffenceList`, and `isApplicationHasOffences` — replaced by the simpler `isApplicationHasNoOffences`.
- Update unit tests to match the new method names and assertions.

## Capabilities

### New Capabilities

- `filter-prosecution-cases-for-application-hearings`: When a hearing is an application hearing and the court application cases carry offences, the entire prosecution cases list is stripped from the hearing details response.

### Modified Capabilities

<!-- No existing spec-level requirements are changing — this corrects the filtering behaviour that was incompletely specified. -->

## Impact

- **`HearingService.filterOutProsecutionCases`** — replaces `filterOutOffences`; downstream callers updated.
- **`HearingQueryApi`** — single call-site updated to use the renamed method.
- **`HearingServiceTest`** — three test methods renamed and assertions updated to reflect that prosecution cases are `null` (not just offences within them).
- No REST API contract changes; the response shape is unchanged — the prosecution cases field was already nullable.