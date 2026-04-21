## 1. HearingService — Replace filtering logic

- [x] 1.1 Rename `filterOutOffences` to `filterOutProsecutionCases` in `HearingService`
- [x] 1.2 Add `isApplicationHasNoOffences` guard: return payload unchanged when application hearing has no offences
- [x] 1.3 Replace `buildHearingWithFilteredOffences` with `buildHearingWithoutProsecutionCases` that sets `prosecutionCases(null)`
- [x] 1.4 Remove `buildProsecutionCasesWithFilteredOffences`, `buildFilteredOffenceList`, and `isApplicationHasOffences` helper methods

## 2. HearingQueryApi — Update call site

- [x] 2.1 Update `HearingQueryApi` to call `hearingService.filterOutProsecutionCases(...)` instead of `filterOutOffences`

## 3. Unit tests — Align with new behaviour

- [x] 3.1 Rename test `shouldFilterOutCaseLevelOffencesWhenApplicationHasOffences` → `shouldFilterOutProsecutionCasesWhenApplicationHasOffences`
- [x] 3.2 Update assertions in that test: expect `prosecutionCases` to be `null` (not a list with null offences)
- [x] 3.3 Rename test `shouldKeepOffencesWhenApplicationHasNoOffences` → `shouldKeepProsecutionCasesWhenApplicationHasNoOffences`
- [x] 3.4 Update `filterOutOffences` call in the non-application-hearing test to `filterOutProsecutionCases`
- [x] 3.5 Verify all three test scenarios from the spec are covered: application-with-offences, application-without-offences, non-application hearing
