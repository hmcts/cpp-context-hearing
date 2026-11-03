# hearing: Java 17 → Java 25 parity test report

Process: [Java 25 upgrade with parity tests (team CHD)](https://hmcts.atlassian.net/wiki/spaces/CHD/pages/366839818);
catalogue: [Parity Testing Java17 → Java25](https://hmcts.atlassian.net/wiki/spaces/CTP/pages/279752455).
Risks and the list of non-mechanical changes: `java25-stage2-risk-review.md`.

| Branch | JDK | Role |
|---|---|---|
| `dev/25.104-chd-j25` | 25 | Java 25 upgrade (squash of `team/25.104.x` onto `main`, plus Stage 1 fixes) |
| `dev/25.104-chd-j25-j17-parity` | 25 | This branch: parity tests, Stage 5 fixes, this report. PR 2 → `dev/25.104-chd-j25` |
| `dev/CHD-2864-chd-parity-with-j25` | 17 | Java 17 copy of the parity tests on `main`. Never merged; establishes the Java 17 behaviour |

Each parity test (`chdNN_…`) asserts the **Java 17** behaviour, so a Java 25 failure is a behaviour change. Both copies use
the same method names and bodies; only the test harness differs. Where Java 25 behaviour was accepted, the Java 25 copy was
changed to assert it (CHD-04, CHD-06) and the Java 17 copy keeps the original assertion.

## Results

All numbers below are from actual runs, read from the surefire/failsafe reports those runs wrote.

| Run | Commit | Classes | Tests | Failures | Errors | Skipped |
|---|---|---|---|---|---|---|
| `main`, JDK 17 (baseline, before parity tests) | `6683b26770` | 416 | 2,762 | 0 | 0 | 38 |
| Java 17 unit tests (`main` + parity tests, JDK 17.0.9) | `d523e25780` | 417 | 2,777 | 0 | 0 | 39 |
| Java 25 unit tests (JDK 25.0.3, Hibernate 6.6.1) | `f498097729` | 420 | 2,876 | 0 | 0 | 40 |
| Java 25 persistence tests on Hibernate 7.3.2 (`-Phibernate-7.3.2`) | `f498097729` | 64 | 236 | 0 | 0 | 4 |
| Java 17 integration tests (`cpp-developers-docker` `java-17`) | `d523e25780` | 61 | 263 | 0 | 0 | 23 |
| Java 25 integration tests (`cpp-developers-docker` `java-25`) | `f498097729`* | 61 | 263 | 0 | 0 | 23 |

\* The Java 25 IT run was on `c38e7749a2`; `f498097729` only changes this report.

- **Unit tests:** Java 17 has one more skipped test than the baseline (`chd05`, which can't run on Java 17; see CHD-05).
  Java 25 has 3 more classes and 99 more tests than Java 17: the newer JUnit 5 Theories counting, the Java 25-only tests
  (`UnspecifiedUuidUserTypeTest`, `PtphDetailRepositoryTest`, two `AccessControlRuleCountTest`s) and team's additions,
  less the 19 MI contract tests that don't run on Java 25 (CHD-16). Per-class differences are explained in
  `java25-stage2-risk-review.md`.
- **Hibernate 7.3.2:** the persistence module gives the same results as on 6.6.1.
- **Integration tests:** both JDKs deployed the WAR, passed 5/5 healthchecks, and ran the same 61 classes with identical
  per-class counts. Server-side exceptions were the same types and counts on both (null reference-data payloads,
  identical JSON-schema rejections, one Xhibit XML error, and an NPE in `HearingService.getHearingDetailsResponseById`
  when a DDJ poll arrives before its hearing exists, also on `main`). Java 25 had no `LazyInitializationException`,
  `TransactionRequiredException`, strict-JPQL or alias errors.
- An earlier Java 17 IT run, before the flaky-test fixes were cherry-picked (`d523e25780`), failed
  `CaseMarkerIT.shouldUpdateCaseMarkers` and `ShareResultsV2IT.shouldRetainHigherPriorityOffenceRemandStatusFromOutsideLaterHearingDay`:
  event-ordering races in the tests themselves, fixed on the Java 25 branch in `ec13040048`.

### Parity tests by runtime

| | Pass | Fail | Skipped |
|---|---|---|---|
| Java 17 copy (15 tests) | 14 | 0 | 1 (`chd05`) |
| Java 25 copy (15 tests) | 15 | 0 | 0 |

## Findings

| ID | Severity | What changed (Java 17 → Java 25) | Parity test | Decision |
|---|---|---|---|---|
| CHD-01 | Medium | Lazy collections: Java 17 kept one session open per request (the framework's request-scoped `EntityManager` with DeltaSpike); the upgrade replaced it with `hibernate.enable_lazy_load_no_trans` | `HearingQueryViewTest.chd01_queryViewEntryPointsRunInATransaction` (Java 25); Java 17 copy: `HearingRepositoryTest.chd01_lazyCollectionsOfAHearingAreReadableAfterTheSessionThatLoadedItHasEnded` | **Fixed (design)**, `1f8da5305c`: flag removed; `HearingQueryView`, `HearingEventQueryView` and `SessionTimeQueryView` are `@Transactional`. Java 25 ITs: no `LazyInitializationException`. Needs sign-off as a design change |
| CHD-02 | Medium | WildFly 40.0.1 runs Hibernate 7.3.2; unit tests use 6.6.1 | Persistence module with `-Phibernate-7.3.2` | **No change needed**: same results on 7.3.2 |
| CHD-03 | Medium | `UnspecifiedUuidUserType` parses `ha_defendant`'s `text` nationality columns with `UUID.fromString` | Not testable on H2; data check below | **No change needed**: no non-UUID values in NFT (both counts 0). Re-run the check against production before release if NFT's data may not be representative |
| CHD-04 | Low–Medium | Reading `…Z` gives zone `ZoneOffset.UTC`; Java 17 gave `ZoneId.of("UTC")`. Same instant and JSON | `ObjectMapperProducerTest.chd04_writesAUtcZonedDateTimeWithAZSuffix`; `.chd04_readsAZSuffixedTimeAsTheSameInstantWithZoneOffsetUtc` (Java 17 copy: `…WithTheUtcZoneId`) | **Accepted**, `20c3ac50a9`: framework serialisation (BC-08); needs sign-off |
| CHD-05 | Low–Medium | JSON columns mapped with `@JdbcTypeCode(SqlTypes.JSON)` instead of hibernate-types | `HearingRepositoryTest.chd05_jsonColumnRoundTripsNestedObjectsNumbersAndNullFields` | **No change needed**: passes on Java 25; can't run on Java 17 (hibernate-types' `jsonb` on H2, as for `main`'s own `@Ignore`d case-note tests); ITs green on both |
| CHD-06 | Low | `save` always merges; DeltaSpike persisted new entities, leaving the passed instance managed | `HearingRepositoryTest.chd06_saveOfANewHearingReturnsAManagedCopyAndLeavesThePassedInstanceDetached` (Java 17 copy: `…LeavesThePassedInstanceManaged`); `.chd06_saveOfANewHearingStoresItsChildren` | **Accepted, provisional**, `ef6dd8a1fc`: no production code uses an entity after `save`. Pending owner review; revert to restore the Java 17 assertion, then make `save` persist new entities |
| CHD-07 | Low | None: `findByHearingIdAndJurisdictionType` throws `NoResultException` on both | `HearingRepositoryTest.chd07_findByHearingIdAndJurisdictionTypeThrowsNoResultWhenNothingMatches` | **Parity.** Existing bug on `main` (out of scope) |
| CHD-08 | Low | Three DeltaSpike `OPTIONAL` finders had become first-row | `HearingRepositoryTest.chd08_findHearingDayByHearingIdAndDateThrowsNonUniqueResultWhenTheHearingHasTwoSittingsOnTheDate`; `DefendantAttendanceRepositoryTest.chd08_…` ×2 | **Fixed**, `cfbba01970` (Stage 1) |
| CHD-09 | Low | `Timestamp` → Europe/London conversion | `HearingServiceTest.chd09_…` | **Parity** |
| CHD-10 | Low | `java.sql.Date` → `LocalDate` conversion | `HearingServiceTest.chd10_…` | **Parity** |
| CHD-11 | Low | `day` column quoted | `DefendantAttendanceRepositoryTest.chd11_…` | **Parity** |
| CHD-12 | Low | `DefendantSearchMapper` no longer a DeltaSpike mapper | `DefendantRepositoryTest.chd12_…` | **Parity** |
| CHD-13 | Low | Stricter JSON schema validation (BC-13) | Both IT runs | **Parity**: identical schema rejections on both JDKs |
| CHD-14 | High | `coredomain` 25.104.0-M4 lacked deleted judicial results, `isDeemedServed`, `crackedIneffectiveSubReasonId` (BC-15) | `DeletedJudicialResultTransformerTest`, `RestructuringHelperV3Test`, `PublishResultsDelegateV3Test`, `ShareResultsIT` | **Fixed** (restored from `main` on 25.104.5, Stage 1) |
| CHD-15 | Medium | Drools harness could load 0 rules (BC-20) | `AccessControlRuleCountTest` ×2 | **Fixed** (team) |
| CHD-16 | Medium (coverage) | `HearingSchemaContractValidationTest` (19 tests) doesn't run: the MI contract jar is still `javax` | — | **Open, external**: re-enable when MI publishes a Jakarta test jar |
| CHD-17 | High | `getDefendantDetailsForSearching` returned `null` for an unknown defendant where Java 17 threw `NoResultException`; the outstanding-fines query then failed with a `NullPointerException` instead of returning an empty payload | `DefendantRepositoryTest.chd17_getDefendantDetailsForSearchingThrowsNoResultWhenTheDefendantIsUnknown` | **Fixed**, `5046ba6c4e`: `setMaxResults(1).getSingleResult()`; `main`'s test expectation restored |

## CHD-03 data check

Run against NFT or SIT, read-only. Both counts 0 means no exposure; otherwise list the values before deciding.

```sql
SELECT 'nationality_id' AS column_name, count(*) AS non_uuid_rows
FROM ha_defendant
WHERE nationality_id IS NOT NULL
  AND nationality_id !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
UNION ALL
SELECT 'additional_nationality_id', count(*)
FROM ha_defendant
WHERE additional_nationality_id IS NOT NULL
  AND additional_nationality_id !~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$';
```

Result (NFT, 2026-11-03): `nationality_id` 0, `additional_nationality_id` 0. No exposure in NFT. The query scans the
whole table (regex conditions can't use an index), so expect it to be slow on large data.

## Checked and unchanged

- **Catalogue items not applicable or already handled:** BC-01/02 (every other `getSingleResult()` is a COUNT;
  `CourtListRepository` identical to `main`), BC-04 (primitive `boolean` fields throw on NULL on both runtimes), BC-05 (no
  JPQL `!= null`), BC-07 (Liquibase 5 properties done), BC-09/10 (no Activiti engine), BC-12 (WAR deploys, ITs pass),
  BC-16 (no `/internal/metrics`), BC-17/18 (framework level), BC-19/22 (not used), BC-03/11/14/23 (refuted in the
  catalogue).
- **Restored to `main`'s behaviour in Stage 1:** Xhibit court lookups, `convertToZonedDateTime` (Europe/London), the
  three `coredomain` features (CHD-14), the three `OPTIONAL` finders (CHD-08).
- **Repositories:** all 29 use `@PersistenceContext(unitName = "hearing-persistence-unit")`; all 25 relationships cascade
  `ALL`; no production code changes an entity after `save`.

## How to run

```shell
# Java 25 (this branch): parity tests, then the persistence module on WildFly's Hibernate
mvn test -pl hearing-viewstore/hearing-viewstore-persistence,hearing-query/hearing-query-view -Dtest='*Test#chd*' -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl hearing-viewstore/hearing-viewstore-persistence -Phibernate-7.3.2

# Java 17 (dev/CHD-2864-chd-parity-with-j25); main's enforcer rejects progression 17.0.286
JAVA_HOME=<jdk-17> mvn verify -Dprogression.version=17.104.306

# Integration tests, one JDK at a time (shared Docker stack)
CPP_DOCKER_DIR=<cpp-developers-docker on java-25> ./runIntegrationTests.sh </dev/null                      # Java 25
CPP_DOCKER_DIR=<cpp-developers-docker on java-17> MAVEN_ARGS="-Dprogression.version=17.104.306" \
  ./runIntegrationTests.sh </dev/null                                                                       # Java 17
```

## Out of scope

- **CHD-07:** `HearingEventQueryView.getHearingEventLogForDocuments` fails for a non-Crown hearing on both branches
  (`findByHearingIdAndJurisdictionType` throws where the caller expects `null`). Existing bug on `main`.
- **NPE in `HearingService.getHearingDetailsResponseById`** when a DDJ poll arrives before its hearing exists. Seen in
  both IT runs; existing on `main`.
- **CHD-16:** MI's contract-test jar; owned by the MI team.
- **Time zones:** 14 production files use default-zone or Europe/London times, kept for parity with `main` despite the
  upgrade skill's UTC rule.
- **Other `ShareResultsIT` races:** only the test that failed was fixed; CLAUDE.md still lists the class as flaky.

## Still needed before Stage 8

- Sign-off: CHD-01 (design fix), CHD-04 (accepted), CHD-06 (provisional acceptance).
