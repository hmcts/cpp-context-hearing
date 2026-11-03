# hearing: Java 17 → Java 25 parity test report

Process: [Java 25 upgrade with parity tests (team CHD)](https://hmcts.atlassian.net/wiki/spaces/CHD/pages/366839818).
Risks come from `java25-stage2-risk-review.md` (Stage 2). Branch: `dev/25.104-chd-j25-j17-parity`, from
`dev/25.104-chd-j25` (`c1e5322d8e`).

Each parity test asserts the **Java 17** behaviour, so a failure on Java 25 is a behaviour change to triage in Stage 5.
Stage 4 ran the same tests (identical method names and bodies, harness changed only) on `main` with JDK 17, on
`dev/CHD-2864-chd-parity-with-j25` (not merged). Every expectation was confirmed there, apart from CHD-05, which can't run on
Java 17 with H2 (see the table).

## Results

| Run | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|
| Java 25 unit tests (Hibernate 6.6.1, BOM default) | 420 | 2,876 | 3 | 0 | 40 |
| Java 25 persistence tests on Hibernate 7.3.2 (`-Phibernate-7.3.2`) | — | 237 | 2 | 0 | 4 |
| Java 17 unit tests (`main` + parity tests, JDK 17.0.9; Stage 4) | 417 | 2,777 | 0 | 1 (CHD-05, can't run on H2) | 38 |
| Java 25 integration tests (Stage 6) | | | | | |
| Java 17 integration tests (Stage 6) | | | | | |

All failures are parity tests (below); no other test fails on either Hibernate version. The 7.3.2 run fails the same two
persistence parity tests as 6.6.1 and nothing else.

Parity tests by runtime (15 methods; same names and bodies in both copies):

| | Pass | Fail | Can't run |
|---|---|---|---|
| Java 17 (`main` + parity tests, JDK 17.0.9) | 14 | 0 | 1 (CHD-05) |
| Java 25 (JDK 25.0.3, Hibernate 6.6.1) | 12 | 3 (CHD-04 read, CHD-06 managed, CHD-17) | 0 |

## Findings

| ID | Severity | What could change | Parity test | Java 25 result | Suggested fix / decision |
|---|---|---|---|---|---|
| CHD-01 | Medium | Lazy collections read after the session ends | `HearingRepositoryTest.chd01_lazyCollectionsOfAHearingAreReadableAfterTheSessionThatLoadedItHasEnded` | Pass (with production's `enable_lazy_load_no_trans=true`) | Stage 5 design fix: remove the flag, add `@Transactional` to query-view entry points. This repository-level test will then go red by design; replace it with a service-level test of those entry points |
| CHD-02 | Medium | Runtime Hibernate 7.3.2 vs tests on 6.6.1 | Whole persistence module with `-Phibernate-7.3.2` | Same results as 6.6.1 | None. The profile needs `hibernate-scan-jandex` (test scope) because Hibernate 7 moved classpath scanning out of core; WildFly discovers entities itself |
| CHD-03 | Medium | `UnspecifiedUuidUserType` parses `ha_defendant`'s `text` column with `UUID.fromString` | Not written: H2 can't hold non-UUID text in a column mapped as `uuid`, so a unit test can't reproduce it | — | Needs a PostgreSQL-level check (IT or a query against NFT data for non-UUID `nationality_id` values) |
| CHD-04 | Low–Medium | `ZonedDateTime` zone after reading `…Z` | `ObjectMapperProducerTest.chd04_writesAUtcZonedDateTimeWithAZSuffix`; `.chd04_readsAZSuffixedTimeWithTheUtcZoneId` | Write passes; read **fails**: zone is `Z`, Java 17 gives `UTC` (confirmed in Stage 4) | Framework behaviour (BC-08): same instant and same JSON, different zone identity. Likely accept with sign-off |
| CHD-05 | Low–Medium | JSON column mapping changed | `HearingRepositoryTest.chd05_jsonColumnRoundTripsNestedObjectsNumbersAndNullFields` | Pass | None. **Can't run on Java 17**: hibernate-types' `JsonNodeBinaryType` can't read H2's column (`JsonParseException: Unrecognized token 'B'`), the reason `main`'s own case-note tests are `@Ignore`d. Parity rests on the Stage 6 ITs |
| CHD-06 | Low | `save` is always `merge`; DeltaSpike persisted new entities (checked in its bytecode: `persist` when the ID has no row and the entity isn't managed) | `HearingRepositoryTest.chd06_saveOfANewHearingReturnsAManagedCopyAndLeavesThePassedInstanceDetached` (was `…LeavesThePassedInstanceManaged`); `.chd06_saveOfANewHearingStoresItsChildren` | Managed instance differs from Java 17 (managed there, confirmed in Stage 4); children: pass | **Accepted (provisional, pending owner review).** No production code changes an entity after `save`, so nothing relies on the passed instance being managed. Test changed to assert the Java 25 behaviour. Alternative if rejected: persist-if-new in `save` |
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
| **CHD-17** | **High** | **New in Stage 3 (BC-01).** `DefendantRepository.getDefendantDetailsForSearching` returns `null` for an unknown defendant; on `main` (DeltaSpike `@Query(max = 1)`, default single-result mode) it threw `NoResultException`, which `HearingQueryView.getOutstandingFinesQueryFromDefendantId` catches to return an empty payload. On Java 25 `convertToOutstandingFinesQuery(null)` throws a `NullPointerException`: the outstanding-fines query fails instead of returning empty | `DefendantRepositoryTest.chd17_getDefendantDetailsForSearchingThrowsNoResultWhenTheDefendantIsUnknown` | **Fails**: nothing thrown (Java 17 throws `NoResultException`, confirmed in Stage 4) | **Fixed** in `5046ba6c4e`: ends the finder with `setMaxResults(1).getSingleResult()` and restores `main`'s `testDefendantDetailsForSearchingNoResultFound` expectation. `chd17` now passes on Hibernate 6.6.1 and 7.3.2 |

## Stage 5 decisions

| ID | Decision | Commit | Why |
|---|---|---|---|
| CHD-17 | Fixed | `5046ba6c4e` | Restores Java 17's `NoResultException`, so the outstanding-fines query returns an empty payload for an unknown defendant instead of failing. Only `chd17` went from red to green (persistence module: 237 tests before and after) |
| CHD-06 | Accepted, provisional | this commit | No production caller uses an entity after `save`; pending owner review, as asked. Reverting this commit restores the Java 17 assertion |

## How to run

```shell
# parity tests only, JDK 25
mvn test -pl hearing-viewstore/hearing-viewstore-persistence,hearing-query/hearing-query-view -Dtest='*Test#chd*' -Dsurefire.failIfNoSpecifiedTests=false
# persistence tests on the Hibernate version WildFly 40.0.1 runs
mvn test -pl hearing-viewstore/hearing-viewstore-persistence -Phibernate-7.3.2
```

## Still to do

- Stage 5: decision for CHD-04; the CHD-01 design fix; owner review of the provisional CHD-06 acceptance.
- Stage 6: integration tests on both JDKs.
