# hearing: Java 17 → Java 25 parity test report

Process: [Java 25 upgrade with parity tests (team CHD)](https://hmcts.atlassian.net/wiki/spaces/CHD/pages/366839818).
Risks come from `java25-stage2-risk-review.md` (Stage 2). Branch: `dev/25.104-chd-j25-j17-parity`, from
`dev/25.104-chd-j25` (`c1e5322d8e`).

Each parity test asserts the **Java 17** behaviour, so a failure on Java 25 is a behaviour change to triage in Stage 5.
Where the Java 17 behaviour was inferred rather than observed, the row says so; Stage 4 (the Java 17 copy) confirms it.

## Results

| Run | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| Java 25 unit tests (Hibernate 6.6.1, BOM default) | 420 | 2,876 | 3 | 0 | 40 |
| Java 25 persistence tests on Hibernate 7.3.2 (`-Phibernate-7.3.2`) | — | 237 | 2 | 0 | 4 |
| Java 17 unit tests (Stage 4) | | | | | |
| Java 25 integration tests (Stage 6) | | | | | |
| Java 17 integration tests (Stage 6) | | | | | |

All failures are parity tests (below); no other test fails on either Hibernate version. The 7.3.2 run fails the same two
persistence parity tests as 6.6.1 and nothing else.

## Findings

| ID | Severity | What could change | Parity test | Java 25 result | Suggested fix / decision |
|---|---|---|---|---|---|
| CHD-01 | Medium | Lazy collections read after the session ends | `HearingRepositoryTest.chd01_lazyCollectionsOfAHearingAreReadableAfterTheSessionThatLoadedItHasEnded` | Pass (with production's `enable_lazy_load_no_trans=true`) | Stage 5 design fix: remove the flag, add `@Transactional` to query-view entry points. This repository-level test will then go red by design; replace it with a service-level test of those entry points |
| CHD-02 | Medium | Runtime Hibernate 7.3.2 vs tests on 6.6.1 | Whole persistence module with `-Phibernate-7.3.2` | Same results as 6.6.1 | None. The profile needs `hibernate-scan-jandex` (test scope) because Hibernate 7 moved classpath scanning out of core; WildFly discovers entities itself |
| CHD-03 | Medium | `UnspecifiedUuidUserType` parses `ha_defendant`'s `text` column with `UUID.fromString` | Not written: H2 can't hold non-UUID text in a column mapped as `uuid`, so a unit test can't reproduce it | — | Needs a PostgreSQL-level check (IT or a query against NFT data for non-UUID `nationality_id` values) |
| CHD-04 | Low–Medium | `ZonedDateTime` zone after reading `…Z` | `ObjectMapperProducerTest.chd04_writesAUtcZonedDateTimeWithAZSuffix`; `.chd04_readsAZSuffixedTimeWithTheUtcZoneId` | Write passes; read **fails**: zone is `Z`, expected `UTC` | Framework behaviour (BC-08). Java 17 expectation is inferred from 77 changed IT expectations; confirm in Stage 4, then likely accept |
| CHD-05 | Low–Medium | JSON column mapping changed | `HearingRepositoryTest.chd05_jsonColumnRoundTripsNestedObjectsNumbersAndNullFields` | Pass | None. Probably can't run on Java 17: `main`'s case-note JSON tests are `@Disabled` on H2 |
| CHD-06 | Low | `save` is always `merge`; DeltaSpike persisted new entities (checked in its bytecode: `persist` when the ID has no row and the entity isn't managed) | `HearingRepositoryTest.chd06_saveOfANewHearingLeavesThePassedInstanceManaged`; `.chd06_saveOfANewHearingStoresItsChildren` | Managed: **fails** (passed instance stays detached). Children: pass | Accept, or persist-if-new in `save`. No production code changes an entity after `save`, so nothing relies on it today |
| CHD-07 | Low | Not-found behaviour of `findByHearingIdAndJurisdictionType` | `HearingRepositoryTest.chd07_findByHearingIdAndJurisdictionTypeThrowsNoResultWhenNothingMatches` | Pass | None for parity. Existing bug on both branches: the caller expects `null` |
| CHD-08 | Low | Duplicate rows in the three `OPTIONAL` finders | `HearingRepositoryTest.chd08_findHearingDayByHearingIdAndDateThrowsNonUniqueResultWhenTheHearingHasTwoSittingsOnTheDate`; `DefendantAttendanceRepositoryTest.chd08_…ThrowsNonUniqueResultWhenTwoRowsMatch`, `chd08_…ReturnsNullWhenNothingMatches` | Pass | None (fixed in `cfbba01970`) |
| CHD-09 | Low | `Timestamp` → `ZonedDateTime` conversion | `HearingServiceTest.chd09_convertToZonedDateTimeConvertsATimestampToTheSameInstantInEuropeLondon` | Pass | None |
| CHD-10 | Low | `java.sql.Date` → `LocalDate` conversion | `HearingServiceTest.chd10_convertToLocalDateConvertsASqlDateToTheSameLocalDate` | Pass | None |
| CHD-11 | Low | Quoted `day` column | `DefendantAttendanceRepositoryTest.chd11_findByHearingIdDefendantIdAndDateMatchesOnlyTheRequestedDay` | Pass | None |
| CHD-12 | Low | `DefendantSearchMapper` no longer a DeltaSpike mapper | `DefendantRepositoryTest.chd12_getDefendantDetailsForSearchingMapsTheDefendantIdAndOrganisationName` | Pass | None |
| CHD-13 | Low | Stricter JSON schema validation | Stage 6 ITs on both JDKs | Java 25 ITs green | Compare with the Java 17 IT run |
| CHD-14 | Fixed | `coredomain` stale base (BC-15) | Restored tests: `DeletedJudicialResultTransformerTest`, `RestructuringHelperV3Test`, `PublishResultsDelegateV3Test`, `ShareResultsIT` | Pass | None |
| CHD-15 | Fixed | Drools 0-rule harness (BC-20) | `AccessControlRuleCountTest` ×2 | Pass | None |
| CHD-16 | Medium (coverage) | MI contract tests not run | — | 19 tests not run | Re-enable when MI publishes a Jakarta test jar |
| **CHD-17** | **High** | **New in Stage 3 (BC-01).** `DefendantRepository.getDefendantDetailsForSearching` returns `null` for an unknown defendant; on `main` (DeltaSpike `@Query(max = 1)`, default single-result mode) it threw `NoResultException`, which `HearingQueryView.getOutstandingFinesQueryFromDefendantId` catches to return an empty payload. On Java 25 `convertToOutstandingFinesQuery(null)` throws a `NullPointerException`: the outstanding-fines query fails instead of returning empty | `DefendantRepositoryTest.chd17_getDefendantDetailsForSearchingThrowsNoResultWhenTheDefendantIsUnknown` | **Fails**: nothing thrown | **Fix**: end the finder with `getSingleResult()` (as the catalogue's BC-01 fix says) and restore `main`'s `testDefendantDetailsForSearchingNoResultFound` expectation |

## How to run

```shell
# parity tests only, JDK 25
mvn test -pl hearing-viewstore/hearing-viewstore-persistence,hearing-query/hearing-query-view -Dtest='*Test#chd*' -Dsurefire.failIfNoSpecifiedTests=false
# persistence tests on the Hibernate version WildFly 40.0.1 runs
mvn test -pl hearing-viewstore/hearing-viewstore-persistence -Phibernate-7.3.2
```

## Still to do

- Stage 4: Java 17 copy of the `chd*` tests (harness changes only) and its results.
- Stage 5: decisions for CHD-04, CHD-06 and CHD-17; the CHD-01 design fix.
- Stage 6: integration tests on both JDKs.
