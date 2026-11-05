# hearing: Java 25 upgrade, Stage 1 change list and Stage 2 risk review

Compared: `main` (`6683b26770`, Java 17) with `dev/25.104-chd-j25` (`cfbba01970`, Java 25).
Process: [Java 25 upgrade with parity tests (team CHD)](https://hmcts.atlassian.net/wiki/spaces/CHD/pages/366839818);
catalogue: [Parity Testing Java17 → Java25](https://hmcts.atlassian.net/wiki/spaces/CTP/pages/279752455).
No code was changed for this review.

Stage 1 results on `cfbba01970`, JDK 25:
- unit tests: 419 classes, 2,862 tests, 0 failures, 0 errors, 40 skipped (`main` on JDK 17: 416 classes, 2,762 tests, 38 skipped);
- integration tests (`runIntegrationTests.sh`, `cpp-developers-docker` `java-25`): WAR deployed, 5/5 healthchecks,
  263 tests, 0 failures, 0 errors, 23 skipped; no Java 25 regression signatures in `server.log`.

## Stage 1: changes that aren't purely mechanical

673 Java files differ from `main`. 551 differ only by the javax→jakarta rename and imports. The rest:

| Area | Change | Files |
|---|---|---|
| Repositories | 29 DeltaSpike repositories rewritten as plain JPA (`@PersistenceContext`), queries kept JPQL or native as before; `save` is `entityManager.merge` | `hearing-viewstore-persistence/.../repository/*Repository.java` |
| Lazy loading | `hibernate.enable_lazy_load_no_trans=true` in place of fetch-joins | `META-INF/persistence.xml` |
| UUID columns | New `UnspecifiedUuidUserType` for `Person.nationalityId` / `additionalNationalityId` (`uuid` in `associated_person`, `text` in `ha_defendant`) | `Person.java`, `persist/type/UnspecifiedUuidUserType.java` |
| JSON columns | `@Type(jsonb-node)` → `@JdbcTypeCode(SqlTypes.JSON)` | 8 entities, e.g. `HearingCaseNote`, `SessionTime`, `DraftResult`, `ReusableInfo`, counsel entities |
| Column name | `day` quoted (`` `day` ``) | `DefendantAttendance.java` |
| Mapper | `DefendantSearchMapper` no longer extends DeltaSpike's `SimpleQueryInOutMapperBase` | `DefendantSearchMapper.java` |
| Time conversion | `convertToZonedDateTime` also accepts `OffsetDateTime` / `Instant` (Hibernate 6), converted to Europe/London as on `main` | `HearingService.java:274` |
| Listing DTOs | `@JsonProperty` on constructor parameters | `CourtMappingsList`, `CourtRoomMapping`, `*List` classes |
| Build / tests | JUnit 5 only; tests run in UTC; MI contract-test unpack skipped; access-control rule-count tests (BC-20) | POMs, test sources |

Restored to `main`'s behaviour since the squash (so no longer differences):
deleted judicial results, `isDeemedServed`, `crackedIneffectiveSubReasonId` (BC-15); Xhibit court lookups (`e1062b08ca`);
`convertToZonedDateTime` back to Europe/London (`b0bb58c671`); the three DeltaSpike `OPTIONAL` finders (`cfbba01970`).

## Stage 2: risk table

Severity: how likely the change is to alter behaviour that callers or users see, and how much.
"Parity test" is what Stage 3 should assert (the Java 17 behaviour).

| ID | Catalogue | Where | What could change | Severity | Parity test (Stage 3) |
|---|---|---|---|---|---|
| CHD-01 | BC-06 | `persistence.xml` (`enable_lazy_load_no_trans`) | Lazy collections read after the transaction no longer throw, but each access opens its own session: extra queries and reads outside the caller's transaction. `main` had no such flag: the framework's `@RequestScoped` `EntityManagerProducer` kept one session open per request and DeltaSpike joined it to each transaction | Medium | Service-tier test on the real persistence layer (H2, not mocks): a query-view method returns entities and their lazy collections (e.g. `Hearing.prosecutionCases`) are readable afterwards, as on Java 17. **Plan:** Stage 3 writes it (passes with the flag on); Stage 4 confirms it on Java 17; Stage 5 removes the flag in its own commit on `dev/25.104-chd-j25-j17-parity`, adding `@Transactional` to the query-view entry points that need it, with the CHD-01 test staying green; Stage 6 ITs on Java 25 confirm no `LazyInitializationException`. Record it as a deliberate design fix with sign-off, since the test doesn't fail while the flag is on |
| CHD-02 | Strict JPA | All JPQL in `hearing-viewstore-persistence` | Unit tests run on Hibernate 6.6.1, but WildFly 40.0.1 runs 7.3.2 with strict JPQL compliance; a query that passes the tests could fail at runtime | Medium | Run the persistence tests on Hibernate 7.3.2 (the CHD page asks for this). 7.3.2 isn't in the local Maven repository yet |
| CHD-03 | — | `UnspecifiedUuidUserType.java` `nullSafeGet` | `UUID.fromString` on `ha_defendant`'s `text` column: an empty or non-UUID value fails the whole load. Unknown whether Java 17's mapping also failed | Medium | Load a defendant whose `nationality_id` text is `''`, and one with a valid UUID; assert the Java 17 outcome for each |
| CHD-04 | BC-08 | Query responses with `ZonedDateTime` fields | The framework now writes `…Z` / `ZoneOffset.UTC` where Java 17 gave `ZoneId.of("UTC")`; 77 IT expectations were changed for it. Equality and `toString` differ for consumers comparing values | Low–Medium | Pin the serialised string of a `ZonedDateTime` field in one query response (e.g. `hearing.get.hearing` sitting day) |
| CHD-05 | — | 8 entities with JSON columns | Different JSON mapping (Hibernate's `SqlTypes.JSON` vs hibernate-types `JsonNodeBinaryType`): JSON `null` vs SQL `NULL`, number or key formatting | Low–Medium | Round-trip a `JsonNode` with nested objects, numbers and a null field through `HearingCaseNote` and `SessionTime` |
| CHD-06 | — | All repositories' `save` | `merge` instead of DeltaSpike's persist-if-new. All 25 relationships are `CascadeType.ALL` and IDs are client-assigned; no production code changes an entity after `save` | Low | Save a new `Hearing` with children; assert children are stored and the returned instance is the managed one |
| CHD-07 | BC-01/02 | `HearingRepository.findByHearingIdAndJurisdictionType:192` → `HearingEventQueryView:238` | Throws `NoResultException` on no match on both branches (DeltaSpike's default mode also threw); the caller's `nonNull` check suggests it expects `null`, so a non-Crown hearing fails this documents query on both. DeltaSpike may have wrapped the exception, which changes the type callers see | Low | Assert the Java 17 exception type when no Crown hearing matches |
| CHD-08 | BC-02 | `HearingRepository.findCourtCenterByHearingId`, `.findHearingDayByHearingIdAndDate`; `DefendantAttendanceRepository.findByHearingIdDefendantIdAndDate` | Restored in `cfbba01970` to `OPTIONAL` behaviour (`null` / one row / `NonUniqueResultException`) | Low (fixed) | Already: two sittings on one date → `NonUniqueResultException`. Missing: the attendance duplicate case |
| CHD-09 | BC-24 | `HearingService.convertToZonedDateTime:274` | Native `timestamptz` values now arrive as `OffsetDateTime`; handled, converted to Europe/London as on Java 17 | Low (fixed) | Already: `shouldConvertHearingEventTimesToEuropeLondonRegardlessOfJdbcType` |
| CHD-10 | BC-24 | `HearingService.convertToLocalDate:314` | Has no `OffsetDateTime` / `LocalDateTime` branch and returns `null` for them; today it only reads `event_date`, a `date` column, so parity holds | Low | Native latest-hearings row: `event_date` comes back as the same `LocalDate` |
| CHD-11 | — | `DefendantAttendance` `` `day` `` | Quoted identifier; only the column name in SQL changes | Low | Covered by `DefendantAttendanceRepositoryTest` |
| CHD-12 | — | `DefendantSearchMapper` | No longer a DeltaSpike query mapper; same `toDto` mapping | Low | Map a defendant and compare with Java 17 output |
| CHD-13 | BC-13 | JSON schema validation (everit) | Stricter validation could reject borderline payloads. ITs green; the one schema rejection seen in the IT log is identical on `main` | Low | Verify on both IT runs (Stage 6) |
| CHD-14 | BC-15 | `coredomain` | 25.104.0-M4 lacked deleted-judicial-result types, `isDeemedServed`, `crackedIneffectiveSubReasonId`; all three restored from `main` on 25.104.5 | Fixed | Restored unit tests; `ShareResultsIT` trial-type checks |
| CHD-15 | BC-20 | Drools `COMMAND_API`, `QUERY_API` | A harness loading 0 rules would let deny-tests pass vacuously | Fixed | `AccessControlRuleCountTest` (×2) |
| CHD-17 | BC-01 | `DefendantRepository.getDefendantDetailsForSearching` → `HearingQueryView.getOutstandingFinesQueryFromDefendantId` | Found in Stage 3. Returns `null` for an unknown defendant where `main` threw `NoResultException`; the caller's `catch` no longer fires, so the outstanding-fines query fails with a `NullPointerException` instead of returning an empty payload. Team changed the test to expect `null` | High | `DefendantRepositoryTest.chd17_getDefendantDetailsForSearchingThrowsNoResultWhenTheDefendantIsUnknown` |
| CHD-16 | — | `hearing-event-listener` | `HearingSchemaContractValidationTest` (19 tests) no longer runs: the MI contract jar is still javax. Coverage gap, not a behaviour change | Medium (coverage) | Re-enable once MI publishes a Jakarta test jar |

## Catalogue items that don't apply, or are already covered

| Item | Disposition |
|---|---|
| BC-01/02 | All other `getSingleResult()` calls are COUNTs; `CourtListRepository.courtListPublishStatuses` is identical to `main` (catches `NoResultException`) |
| BC-04 | 10 primitive `boolean` entity fields (e.g. `Offence.proceedingsConcluded`, `HearingEvent.deleted`); a NULL throws on both runtimes per the corrected BC-04, so parity |
| BC-05 | No JPQL `!= null` |
| BC-07 | `liquibase.hub.mode` removed, `analytics.enabled: false` set |
| BC-09/10 | No Activiti engine; only the Liquibase jar in the Dockerfile (`activiti_version` 25.104.0) |
| BC-11, BC-03, BC-14, BC-23 | Refuted in the catalogue |
| BC-12 | RESTEasy packaging: WAR deploys and ITs pass |
| BC-16 | No use of `/internal/metrics` |
| BC-17 | Framework-level (`stream_error`); no context code; verify with the framework fix |
| BC-18 | Framework IT harness; ITs pass |
| BC-19, BC-22 | Not used (no agent-prosecutor rules, no Tika) |
| BC-21 | Not yet checked: compare generated sources between the Java 17 and Java 25 builds |

## Decisions for the owning team

- `hibernate.enable_lazy_load_no_trans=true` (CHD-01): planned for removal in Stage 5, replaced by `@Transactional`
  query-view entry points (see CHD-01); needs sign-off as a design fix, since the parity test passes with the flag on.
- Time zones: Europe/London restored for parity with `main`, against the skill's "all date-times are UTC" rule;
  14 production files use default-zone or Europe/London times.
- PR 1 was a local squash (`029a703912`) pushed directly; no pull request exists.
- `team/25.104-chd-j25` has `17.104.210-JAVA-25-SNAPSHOT` and the dev branch `25.104.210-SNAPSHOT`: PR 3 will conflict on the POMs.
