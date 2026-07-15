# cpp-context-hearing

Court Hearing Management Service for the Crime Common Platform (CPP). Manages the full lifecycle of court hearings — creation, amendment, result recording, counsel management, court list publication, and audit trails — across Crown Court, Magistrates, and Youth Court.

## Programme
Crime Common Platform (CPP) — Modern by Default (MbD)
Team: Listing / Hearing

## Identity

| Field          | Value                                    |
|----------------|------------------------------------------|
| Group ID       | `uk.gov.moj.cpp.hearing`                 |
| Artifact       | `hearing-parent`                         |
| Version        | `17.104.158-SNAPSHOT`                    |
| Packaging      | WAR (WildFly / JBoss)                    |
| Java           | 17                                       |
| Build tool     | **Maven** (NEVER Gradle)                 |
| Root package   | `uk.gov.moj.cpp.hearing`                 |
| Local port     | 8080 (WildFly default)                   |

## Architecture

**CQRS + Event Sourcing** via HMCTS Justice Services Framework.

```
Command API → Command Handler → Domain Aggregate → Domain Events
                                                        ↓
                                             Event Listeners → View Store
                                                        ↓
                                             Query API ← View Store
```

- **Command side** (`hearing-command`): accepts write requests via REST, dispatches commands, handlers update aggregates and emit events.
- **Query side** (`hearing-query`): reads from the view store updated by event listeners.
- **Event store**: separate PostgreSQL schema; events are the system of record.
- **View store**: denormalised JPA read model in a second PostgreSQL schema.
- **Messaging**: JMS topic `jms:topic:hearing.event`; public events on `jms:topic:public.event`.
- **Framework**: `uk.gov.moj.cpp.common:service-parent-pom:17.104.0`

## Module Structure

```
hearing-command/
  hearing-command-api/        # REST entry points — RAML-first, generated resources
  hearing-command-handler/    # @Handles business logic, aggregate updates
hearing-query/
  hearing-query-api/          # REST query resources — RAML-first
  hearing-query-view/         # Query view components
hearing-domain/
  hearing-domain-event/       # Domain event classes
  hearing-domain-aggregate/   # Aggregate roots
  hearing-domain-common/      # Shared domain types
  hearing-domain-xhibit/      # Xhibit court system integration
hearing-event/
  hearing-event-listener/     # 40+ domain event listeners
  hearing-event-processor/    # Event processing logic
hearing-viewstore/
  hearing-viewstore-persistence/  # JPA entities + repositories
  hearing-viewstore-liquibase/    # DB schema (Liquibase)
hearing-service/              # WAR assembly module
hearing-common/               # Shared utilities (e.g. SessionTimeUUIDService)
hearing-healthchecks/         # Health check endpoints
hearing-json/                 # JSON processing utilities
hearing-event-sources/        # Event source config (event-sources.yaml)
hearing-integration-test/     # BDD integration tests
hearing-performance-test/     # JMeter performance tests
test-utilities/               # Test helper classes
pojo-plugin/                  # Custom Maven plugin
```

## API Design

**Source of truth: RAML specs.**

| Side    | Spec location                                                  | Base URI                                                    |
|---------|----------------------------------------------------------------|-------------------------------------------------------------|
| Command | `hearing-command/hearing-command-api/src/raml/hearing-command-api.raml` | `/hearing-command-api/command/api/rest/hearing`   |
| Query   | `hearing-query/hearing-query-api/src/raml/hearing-query-api.raml`       | `/hearing-query-api/query/api/rest/hearing`       |

All request/response bodies use vendor media types: `application/vnd.hearing.<name>+json`.

### Command Endpoints (all POST → 202 Accepted)

| Path | Key media types |
|------|----------------|
| `POST /hearings` | `hearing.initiate`, `hearing.generate-nows`, `hearing.update-defendant-attendance-on-hearing-day` |
| `POST /hearings/{hearingId}` | `hearing.add/remove/update-prosecution-counsel`, `hearing.add/remove/update-defence-counsel`, `hearing.update-plea`, `hearing.update-verdict`, `hearing.save-draft-result`, `hearing.save-multiple-draft-results`, `hearing.amend`, `hearing.mark-as-duplicate`, `hearing.change-hearing-detail`, `hearing.set-trial-type`, `hearing.add-witness`, `hearing.youth-court-defendants`, `hearing.unlock-hearing`, `hearing.replicate-shared-results`, and 30+ more |
| `POST /hearings/{hearingId}/{hearingDay}` | `hearing.share-days-results`, `hearing.save-days-draft-result`, `hearing.save-draft-result-v2`, `hearing.delete-draft-result-v2` |
| `POST /hearings/{hearingId}/hearing-slots` | `hearing.book-provisional-hearing-slots` |
| `POST /hearings/{hearingId}/share-results` | `hearing.share-results`, `hearing.share-results-v2` |
| `POST /hearings/{hearingId}/event` | `hearing.log-hearing-event` |
| `POST /hearings/{hearingId}/events` | `hearing.update-hearing-events` |
| `POST /hearings/{hearingId}/event/{hearingEventId}` | `hearing.correct-hearing-event` |
| `POST /hearings/event-definitions` | `hearing.create-hearing-event-definitions` |
| `POST /publish-court-list` | `hearing.publish-court-list` |
| `POST /publishHearingListsForCrownCourts` | `hearing.publish-hearing-lists-for-crown-courts` |
| `POST /publishHearingListsForCrownCourtsWithIds` | `hearing.publish-hearing-lists-for-crown-courts-with-ids` |
| `POST /record-session-time` | `hearing.record-session-time` |
| `POST /request-approval` | `hearing.request-approval` |
| `POST /validate-result-amendments` | `hearing.validate-result-amendments` |
| `POST /reusable-info/{hearingId}` | `hearing.reusable-info` |
| `POST /remove-offences-from-existing-hearing` | `hearing.remove-offences-from-existing-hearing` |
| `POST /correct-hearing-days-without-court-centre` | `hearing.correct-hearing-days-without-court-centre` |
| `POST /upload-subscriptions/{referenceDate}` | `hearing.upload-subscriptions` |

### Query Endpoints (all GET → 200 OK)

| Path | Response media type |
|------|---------------------|
| `GET /hearings` | `hearing.get.hearings` (params: `date`, `courtCentreId`, `roomId`) |
| `GET /hearings/{hearingId}` | `hearing.get.hearing` |
| `GET /hearings/{hearingId}/draft-result` | `hearing.get-draft-result` |
| `GET /hearings/{hearingId}/{hearingDay}` | `hearing.get-results` |
| `GET /hearings/{hearingId}/{hearingDay}/draft-result` | `hearing.get-draft-result-v2` |
| `GET /hearings/{hearingId}/{hearingDay}/share-results` | `hearing.get-share-result-v2` |
| `GET /hearings/{hearingId}/event-log` | `hearing.get-hearing-event-log` |
| `GET /hearings/{hearingId}/event-definitions/{id}` | `hearing.get-hearing-event-definition` |
| `GET /hearings/{hearingId}/{hearingDay}/offences/{offenceId}` | `hearing.custody-time-limit` |
| `GET /hearings/{hearingId}/{hearingEventDefinitionId}/prosecution-case` | `hearing.prosecution-case-by-hearingid` |
| `GET /hearings/{hearingId}/active-hearings-for-court-room/{eventDate}` | `hearing.get-active-hearings-for-court-room` |
| `GET /hearings/court-centres` | `hearing.hearings-court-centres-for-date` |
| `GET /hearings/event-log` | `hearing.get-hearing-event-log-for-documents` |
| `GET /hearings/event-log/extract` | `hearing.get-hearing-event-log-extract-for-documents` |
| `GET /hearings/event-log-count` | `hearing.get-hearing-event-log-count` |
| `GET /hearings-for-today` | `hearing.get.hearings-for-today` |
| `GET /hearings-for-future` | `hearing.get.hearings-for-future` |
| `GET /future-hearings-by-cases` | `hearing.get.future-hearings` |
| `GET /event-definitions` | `hearing.get-hearing-event-definitions` |
| `GET /nows/{hearingId}` | `hearing.get.nows` |
| `GET /timeline/{id}` | `hearing.case.timeline` / `hearing.application.timeline` |
| `GET /court-list-publish-status/{courtCentreId}` | `hearing.court.list.publish.status` |
| `GET /get-latest-hearings-by-court-centres` | `hearing.latest-hearings-by-court-centres` |
| `GET /get-cracked-ineffective-reason` | `hearing.get-cracked-ineffective-reason` |
| `GET /session-time/{courtHouseId}/{courtRoomId}` | `hearing.query.session-time` |
| `GET /reusable-info/{hearingId}` | `hearing.query.reusable-info` |
| `GET /defendant` | `hearing.defendant.info` |
| `GET /defendant/{defendantId}/outstanding-fines` | `hearing.defendant.outstanding-fines` |
| `POST /outstanding-fines` | `hearing.query.outstanding-fines` |
| `GET /search` | `hearing.query.search-by-material-id` |
| `GET /retrieve` | `hearing.retrieve-subscriptions` |

## Domain Model

### Key View Store Entities (`hearing-viewstore-persistence`)

| Entity | Description |
|--------|-------------|
| `Hearing` | Main aggregate root — hearing details, status, type, court centre |
| `HearingDay` | Individual sitting day within a hearing |
| `ProsecutionCase` | Case linked to a hearing |
| `Defendant` | Defendant on a case/hearing |
| `Offence` | Offence record for a defendant |
| `DraftResult` | In-progress result recording |
| `Target` | Result recording target per offence/defendant |
| `HearingApplication` | Court application within a hearing |
| `Witness` | Witness records |
| `CaseMarker` | Markers applied to cases |
| `Now` (NOWS) | Notice of Weapon/Substance records |

### Key Command Handlers (`hearing-command-handler`)

50+ handlers including: `InitiateHearingCommandHandler`, `UpdateDefendantCommandHandler`, `AddDefenceCounselCommandHandler`, `ShareResultsCommandHandler`, `PublishCourtListStatusHandler`, `DeleteHearingCommandHandler`, `DuplicateHearingCommandHandler`, `CustodyTimeLimitClockHandler`, `HearingEventCommandHandler`

### Key Event Listeners (`hearing-event-listener`)

40+ listeners including: `InitiateHearingEventListener`, `HearingEventListener`, `PleaUpdateEventListener`, `VerdictUpdateEventListener`, `DefenceCounselEventListener`, `CourtListRestrictionEventListener`, `ReusableInfoEventListener`

### Key Services (`hearing-viewstore-persistence` / domain)

`HearingService`, `ProgressionService`, `ReusableInfoService`, `CTLExpiryDateCalculatorService`, `ReferenceDataService`

## Databases

| Schema | JNDI datasource | Purpose |
|--------|-----------------|---------|
| `hearing-event-store-db` | `java:/app/hearing-command-handler/DS.eventstore` | Event store (append-only) |
| `hearing-view-store-db` | `java:/DS.hearing` | Denormalised read model |
| `referencedata-view-store-db` | `java:/DS.referencedata` | Reference data |

Database schema managed by **Liquibase** (`hearing-viewstore-liquibase`).

## Build & Test

```bash
mvn clean install              # Full build (all modules)
mvn clean install -DskipTests  # Build without tests
mvn test                       # Unit tests
mvn verify -Pintegration-test  # Integration tests (requires Docker/DB)
cd hearing-performance-test && mvn clean verify -Pscheduling-performance-test  # Perf tests
cd hearing-performance-test && mvn jmeter:gui -Pscheduling-performance-test    # JMeter GUI
```

**NEVER use Gradle.** This project uses Maven exclusively.

## CI/CD

- **Pipeline**: `azure-pipelines.yaml` → Azure DevOps pipeline (ADO Pipeline 460)
- **Agent pool**: `MDV-ADO-AGENT-AKS-01` (CentOS 8, Java 17)
- **PR check**: `context-verify` stage
- **Main branch**: `context-validation` + integration tests
- **Static analysis**: SonarQube (`sonar.projectKey=uk.gov.moj.cpp.hearing:hearing-parent`)
- **Coverage**: JaCoCo (exclusions: DTOs, enums, generated code, mappers, event classes)

## Deployment

- **Runtime**: WildFly / JBoss application server
- **Artifact**: `hearing-service-{version}.war`
- **Config**: `standalone-hearing.xml` (datasources, JMS, messaging)
- **Docker**: `docker/Dockerfile_hearing-service`
- Liquibase migrations run at startup:
  - `hearing-viewstore-liquibase`
  - `event-repository-liquibase`
  - `aggregate-snapshot-repository-liquibase`
  - `event-buffer-liquibase`
  - `event-tracking-liquibase`
  - `framework-system-liquibase`
  - `activiti-liquibase`

## Key Dependencies

| Dependency | Version |
|-----------|---------|
| `service-parent-pom` | `17.104.0` |
| `coredomain` | `17.104.3` |
| `referencedata` | `17.103.131` |
| `material` | `17.0.57` |
| `results` | `17.0.58` |
| `progression` | `17.0.249` |
| `usersgroups` | `17.104.46` |
| `stagingenforcement` | `17.103.77` |
| `junit-dataprovider` | `1.13.1` |
| `xmlunit` | `2.6.3` |

## Coding Conventions

- **DI**: CDI (`@Inject`) — not Spring. No `@Autowired`.
- **Handlers**: annotated with `@Handles` from the HMCTS framework.
- **Queries**: annotated with query mapping framework annotations.
- **Entities**: JPA 2.x with `@Entity`, `@Table`.
- **Tests**: JUnit 4/5 + Mockito; integration tests suffix `IT`.
- **Naming**: follows HMCTS framework conventions — `*CommandHandler`, `*EventListener`, `*QueryView`, `*Repository`, `*Service`.

## Important Notes

- REST resources are **code-generated** from RAML descriptors via the `pojo-plugin` — do not hand-edit generated files under `target/generated-sources`.
- All commands return `202 Accepted`; queries return `200 OK`.
- Access control enforced by `HearingQueryService.validateIfUserHasAccessToHearing()` — never bypass.
- Welsh language support is first-class: `hearing.save-defendants-welsh-translations` command exists.
- Custody Time Limit (CTL) calculations are safety-critical: `CTLExpiryDateCalculatorService`.
