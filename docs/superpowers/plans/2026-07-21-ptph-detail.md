# Tier & List Type Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Capture, edit, finalise and delete Tier & List-type information for a seeding hearing, via new CQRS command + query endpoints on the existing `HearingAggregate`.

**Architecture:** Full CQRS/event-sourcing vertical slice mirroring the existing `set-trial-type` feature. Three commands (`save`, `finalise`, `delete`) flow REST → `HearingCommandApi` (`hearing.X` → re-send as `hearing.command.X`) → `PtphDetailCommandHandler` → `HearingAggregate` (via a new `HearingPtphDetailDelegate`) → three domain events → `PtphDetailEventListener` → a new `PtphDetail` view-store entity. One query (`GET /hearings/{hearingId}/ptph-detail`) reads it back.

**Tech Stack:** Java 17, Maven, HMCTS Justice Services Framework, CDI (`@Inject`), JPA + Apache DeltaSpike Data (`@Repository`), Liquibase, RAML 0.8, JSON Schema draft-04, JUnit 4/5 + Mockito.

## Global Constraints

- Build tool is **Maven only** — never Gradle. Full build: `mvn clean install`; single module: `mvn -pl <module> -am test`.
- DI is **CDI** (`@Inject`) — never Spring / `@Autowired`.
- Root package `uk.gov.moj.cpp.hearing`.
- All commands POST → 202 Accepted; queries GET → 200 OK.
- Vendor media types: `application/vnd.hearing.<name>+json`.
- JSON Schema dialect is **draft-04** (`http://json-schema.org/draft-04/schema#`) — no `if`/`then`; cross-field rules go in the handler.
- `{hearingId}` path parameter is auto-merged into the command payload by the framework — do NOT put it in request schemas.
- Do not hand-edit generated sources under `target/generated-sources`.
- Never bypass `HearingQueryService.validateIfUserHasAccessToHearing()`.
- Aggregate/domain validation failures throw `RuntimeException` with a descriptive message (existing convention — see `PleaDelegate`, `ConvictionDateDelegate`).
- Tier and list type are carried as **String** (enum name) inside events, the view entity and the query response, to avoid cross-module enum dependencies; the `Tier`/`ListType` enums are used only at the command boundary (DTO + JSON validation).

### Canonical names (used across all tasks)

| Concept | Value |
|---|---|
| Command action (API) | `hearing.save-ptph-detail`, `hearing.finalise-ptph-detail`, `hearing.delete-ptph-detail` |
| Command action (handler) | `hearing.command.save-ptph-detail`, `hearing.command.finalise-ptph-detail`, `hearing.command.delete-ptph-detail` |
| Command media types (API) | `application/vnd.hearing.save-ptph-detail+json` etc. |
| Command media types (handler) | `application/vnd.hearing.command.save-ptph-detail+json` etc. |
| Events (`@Event`) | `hearing.ptph-detail-saved`, `hearing.ptph-detail-finalised`, `hearing.ptph-detail-deleted` |
| Event classes | `PtphDetailSaved`, `PtphDetailFinalised`, `PtphDetailDeleted` |
| Query action | `hearing.get-ptph-detail` |
| Query media type | `application/vnd.hearing.get-ptph-detail+json` |
| Query path | `GET /hearings/{hearingId}/ptph-detail` |
| View table | `ha_ptph_detail` |
| Enum: Tier | `TIER_1`..`TIER_7` |
| Enum: ListType | `TYPE_1_FIXED` ("1F"), `TYPE_2_FLEXIBLE` ("2F") |

---

## Task 1: Domain value types (enums, events, command DTO)

**Files:**
- Create: `hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/Tier.java`
- Create: `hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/ListType.java`
- Create: `hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/SavePtphDetailCommand.java`
- Create: `hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetailSaved.java`
- Create: `hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetailFinalised.java`
- Create: `hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetailDeleted.java`
- Test: `hearing-domain/hearing-domain-event/src/test/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetailSavedTest.java`

**Interfaces:**
- Produces:
  - `enum Tier { TIER_1, TIER_2, TIER_3, TIER_4, TIER_5, TIER_6, TIER_7 }`
  - `enum ListType { TYPE_1_FIXED("1F"), TYPE_2_FLEXIBLE("2F"); String getCode(); }`
  - `SavePtphDetailCommand(UUID hearingId, Tier tier, ListType listType, String keyReason)` with getters.
  - `PtphDetailSaved(UUID hearingId, String tier, String listType, String keyReason)` `@Event("hearing.ptph-detail-saved")`, getters.
  - `PtphDetailFinalised(UUID hearingId)` `@Event("hearing.ptph-detail-finalised")`, getter.
  - `PtphDetailDeleted(UUID hearingId)` `@Event("hearing.ptph-detail-deleted")`, getter.

- [ ] **Step 1: Write the failing test**

`PtphDetailSavedTest.java`:
```java
package uk.gov.moj.cpp.hearing.domain.event;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static java.util.UUID.randomUUID;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PtphDetailSavedTest {

    @Test
    void shouldHoldSavedFields() {
        final UUID hearingId = randomUUID();
        final PtphDetailSaved event = new PtphDetailSaved(hearingId, "TIER_2", "TYPE_1_FIXED", "fixed reason");

        assertThat(event.getHearingId(), is(hearingId));
        assertThat(event.getTier(), is("TIER_2"));
        assertThat(event.getListType(), is("TYPE_1_FIXED"));
        assertThat(event.getKeyReason(), is("fixed reason"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-domain/hearing-domain-event -am test -Dtest=PtphDetailSavedTest`
Expected: FAIL — `PtphDetailSaved` cannot be resolved.

- [ ] **Step 3: Write the enums and DTO**

`Tier.java`:
```java
package uk.gov.moj.cpp.hearing.command;

public enum Tier {
    TIER_1, TIER_2, TIER_3, TIER_4, TIER_5, TIER_6, TIER_7
}
```

`ListType.java`:
```java
package uk.gov.moj.cpp.hearing.command;

public enum ListType {
    TYPE_1_FIXED("1F"),
    TYPE_2_FLEXIBLE("2F");

    private final String code;

    ListType(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
```

`SavePtphDetailCommand.java`:
```java
package uk.gov.moj.cpp.hearing.command;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SavePtphDetailCommand {

    private UUID hearingId;
    private Tier tier;
    private ListType listType;
    private String keyReason;

    public SavePtphDetailCommand() {
    }

    @JsonCreator
    public SavePtphDetailCommand(@JsonProperty("hearingId") final UUID hearingId,
                                      @JsonProperty("tier") final Tier tier,
                                      @JsonProperty("listType") final ListType listType,
                                      @JsonProperty("keyReason") final String keyReason) {
        this.hearingId = hearingId;
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public Tier getTier() {
        return tier;
    }

    public ListType getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }
}
```

- [ ] **Step 4: Write the three event classes**

`PtphDetailSaved.java`:
```java
package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.ptph-detail-saved")
public class PtphDetailSaved {

    private final UUID hearingId;
    private final String tier;
    private final String listType;
    private final String keyReason;

    @JsonCreator
    public PtphDetailSaved(@JsonProperty("hearingId") final UUID hearingId,
                                @JsonProperty("tier") final String tier,
                                @JsonProperty("listType") final String listType,
                                @JsonProperty("keyReason") final String keyReason) {
        this.hearingId = hearingId;
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getTier() {
        return tier;
    }

    public String getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }
}
```

`PtphDetailFinalised.java`:
```java
package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.ptph-detail-finalised")
public class PtphDetailFinalised {

    private final UUID hearingId;

    @JsonCreator
    public PtphDetailFinalised(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
```

`PtphDetailDeleted.java`:
```java
package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.ptph-detail-deleted")
public class PtphDetailDeleted {

    private final UUID hearingId;

    @JsonCreator
    public PtphDetailDeleted(@JsonProperty("hearingId") final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -pl hearing-domain/hearing-domain-event -am test -Dtest=PtphDetailSavedTest`
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/Tier.java \
        hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/ListType.java \
        hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/SavePtphDetailCommand.java \
        hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetail*.java \
        hearing-domain/hearing-domain-event/src/test/java/uk/gov/moj/cpp/hearing/domain/event/PtphDetailSavedTest.java
git commit -m "feat: tier & list type domain enums, command DTO and events"
```

---

## Task 2: Aggregate behaviour (momento + delegate + wiring)

**Files:**
- Modify: `hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingAggregateMomento.java`
- Create: `hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingPtphDetailDelegate.java`
- Modify: `hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/HearingAggregate.java`
- Test: `hearing-domain/hearing-domain-aggregate/src/test/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingPtphDetailDelegateTest.java`

**Interfaces:**
- Consumes: `PtphDetailSaved`, `PtphDetailFinalised`, `PtphDetailDeleted` (Task 1).
- Produces (public methods on `HearingAggregate`):
  - `Stream<Object> savePtphDetail(PtphDetailSaved event)`
  - `Stream<Object> finalisePtphDetail(PtphDetailFinalised event)`
  - `Stream<Object> deletePtphDetail(PtphDetailDeleted event)`
- Momento accessors: `String getTier()/setTier`, `String getListType()/setListType`, `String getPtphDetailKeyReason()/set...`, `boolean isPtphDetailFinalised()/setPtphDetailFinalised`.

- [ ] **Step 1: Write the failing test**

`HearingPtphDetailDelegateTest.java`:
```java
package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class HearingPtphDetailDelegateTest {

    private final HearingAggregateMomento momento = new HearingAggregateMomento();
    private final HearingPtphDetailDelegate delegate = new HearingPtphDetailDelegate(momento);

    @Test
    void saveStoresStateOnMomento() {
        final UUID hearingId = randomUUID();
        delegate.savePtphDetail(new PtphDetailSaved(hearingId, "TIER_2", "TYPE_1_FIXED", "reason"))
                .forEach(e -> delegate.handlePtphDetailSaved((PtphDetailSaved) e));

        assertThat(momento.getTier(), is("TIER_2"));
        assertThat(momento.getListType(), is("TYPE_1_FIXED"));
        assertThat(momento.getPtphDetailKeyReason(), is("reason"));
    }

    @Test
    void saveRejectedWhenFinalised() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_2_FLEXIBLE");
        momento.setPtphDetailFinalised(true);

        assertThrows(RuntimeException.class, () ->
                delegate.savePtphDetail(new PtphDetailSaved(randomUUID(), "TIER_3", null, null)));
    }

    @Test
    void finaliseRequiresBothPtphDetail() {
        momento.setTier("TIER_2");
        momento.setListType(null);

        assertThrows(RuntimeException.class, () ->
                delegate.finalisePtphDetail(new PtphDetailFinalised(randomUUID())));
    }

    @Test
    void finaliseSetsFlagWhenBothPresent() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_2_FLEXIBLE");

        delegate.finalisePtphDetail(new PtphDetailFinalised(randomUUID()))
                .forEach(e -> delegate.handlePtphDetailFinalised((PtphDetailFinalised) e));

        assertThat(momento.isPtphDetailFinalised(), is(true));
    }

    @Test
    void deleteClearsAllState() {
        momento.setTier("TIER_2");
        momento.setListType("TYPE_1_FIXED");
        momento.setPtphDetailKeyReason("reason");
        momento.setPtphDetailFinalised(true);

        delegate.deletePtphDetail(new PtphDetailDeleted(randomUUID()))
                .forEach(e -> delegate.handlePtphDetailDeleted((PtphDetailDeleted) e));

        assertThat(momento.getTier(), is((Object) null));
        assertThat(momento.getListType(), is((Object) null));
        assertThat(momento.getPtphDetailKeyReason(), is((Object) null));
        assertThat(momento.isPtphDetailFinalised(), is(false));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-domain/hearing-domain-aggregate -am test -Dtest=HearingPtphDetailDelegateTest`
Expected: FAIL — `HearingPtphDetailDelegate` and momento accessors do not exist.

- [ ] **Step 3: Add fields + accessors to `HearingAggregateMomento`**

Add near the other scalar fields (e.g. after `private boolean deleted = false;`, around line 43):
```java
    private String tier;
    private String listType;
    private String ptphDetailKeyReason;
    private boolean ptphDetailFinalised = false;
```
Add these accessors (place with the other getter/setter pairs):
```java
    public String getTier() {
        return tier;
    }

    public void setTier(final String tier) {
        this.tier = tier;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(final String listType) {
        this.listType = listType;
    }

    public String getPtphDetailKeyReason() {
        return ptphDetailKeyReason;
    }

    public void setPtphDetailKeyReason(final String ptphDetailKeyReason) {
        this.ptphDetailKeyReason = ptphDetailKeyReason;
    }

    public boolean isPtphDetailFinalised() {
        return ptphDetailFinalised;
    }

    public void setPtphDetailFinalised(final boolean ptphDetailFinalised) {
        this.ptphDetailFinalised = ptphDetailFinalised;
    }
```

- [ ] **Step 4: Create `HearingPtphDetailDelegate`**

```java
package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.isNull;

import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.io.Serializable;
import java.util.stream.Stream;

public class HearingPtphDetailDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingPtphDetailDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public Stream<Object> savePtphDetail(final PtphDetailSaved event) {
        if (momento.isPtphDetailFinalised()) {
            throw new RuntimeException("Tier and list type is finalised and cannot be changed");
        }
        return Stream.of(event);
    }

    public Stream<Object> finalisePtphDetail(final PtphDetailFinalised event) {
        if (isNull(momento.getTier()) || isNull(momento.getListType())) {
            throw new RuntimeException("Both tier and list type are required to finalise");
        }
        if (momento.isPtphDetailFinalised()) {
            throw new RuntimeException("Tier and list type is already finalised");
        }
        return Stream.of(event);
    }

    public Stream<Object> deletePtphDetail(final PtphDetailDeleted event) {
        return Stream.of(event);
    }

    public void handlePtphDetailSaved(final PtphDetailSaved event) {
        momento.setTier(event.getTier());
        momento.setListType(event.getListType());
        momento.setPtphDetailKeyReason(event.getKeyReason());
    }

    public void handlePtphDetailFinalised(final PtphDetailFinalised event) {
        momento.setPtphDetailFinalised(true);
    }

    public void handlePtphDetailDeleted(final PtphDetailDeleted event) {
        momento.setTier(null);
        momento.setListType(null);
        momento.setPtphDetailKeyReason(null);
        momento.setPtphDetailFinalised(false);
    }
}
```

- [ ] **Step 5: Wire the delegate into `HearingAggregate`**

Add imports (with the other event imports):
```java
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.HearingPtphDetailDelegate;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;
```
Add the delegate field (next to `hearingTrialTypeDelegate`, ~line 263):
```java
    private final HearingPtphDetailDelegate hearingPtphDetailDelegate = new HearingPtphDetailDelegate(momento);
```
Add three routes inside the `apply(final Object event)` `match(event).with(...)` block (alongside the trial-type `when(...)` entries, ~line 367), each ending with a comma:
```java
                when(PtphDetailSaved.class).apply(hearingPtphDetailDelegate::handlePtphDetailSaved),
                when(PtphDetailFinalised.class).apply(hearingPtphDetailDelegate::handlePtphDetailFinalised),
                when(PtphDetailDeleted.class).apply(hearingPtphDetailDelegate::handlePtphDetailDeleted),
```
Add three public command methods (next to the `setTrialType(...)` methods, ~line 1195):
```java
    public Stream<Object> savePtphDetail(final PtphDetailSaved event) {
        return apply(this.hearingPtphDetailDelegate.savePtphDetail(event));
    }

    public Stream<Object> finalisePtphDetail(final PtphDetailFinalised event) {
        return apply(this.hearingPtphDetailDelegate.finalisePtphDetail(event));
    }

    public Stream<Object> deletePtphDetail(final PtphDetailDeleted event) {
        return apply(this.hearingPtphDetailDelegate.deletePtphDetail(event));
    }
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -pl hearing-domain/hearing-domain-aggregate -am test -Dtest=HearingPtphDetailDelegateTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingAggregateMomento.java \
        hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingPtphDetailDelegate.java \
        hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/HearingAggregate.java \
        hearing-domain/hearing-domain-aggregate/src/test/java/uk/gov/moj/cpp/hearing/domain/aggregate/hearing/HearingPtphDetailDelegateTest.java
git commit -m "feat: tier & list type aggregate behaviour with finalise/immutability rules"
```

---

## Task 3: Command handler (+ handler messaging RAML/schemas)

**Files:**
- Create: `hearing-command/hearing-command-handler/src/main/java/uk/gov/moj/cpp/hearing/command/handler/PtphDetailCommandHandler.java`
- Create: `hearing-command/hearing-command-handler/src/raml/json/schema/hearing.command.save-ptph-detail.json`
- Create: `hearing-command/hearing-command-handler/src/raml/json/hearing.command.save-ptph-detail.json`
- Create: `hearing-command/hearing-command-handler/src/raml/json/schema/hearing.command.finalise-ptph-detail.json`
- Create: `hearing-command/hearing-command-handler/src/raml/json/hearing.command.finalise-ptph-detail.json`
- Create: `hearing-command/hearing-command-handler/src/raml/json/schema/hearing.command.delete-ptph-detail.json`
- Create: `hearing-command/hearing-command-handler/src/raml/json/hearing.command.delete-ptph-detail.json`
- Modify: `hearing-command/hearing-command-handler/src/raml/hearing-command-handler.messaging.raml`
- Test: `hearing-command/hearing-command-handler/src/test/java/uk/gov/moj/cpp/hearing/command/handler/PtphDetailCommandHandlerTest.java`

**Interfaces:**
- Consumes: `HearingAggregate.savePtphDetail/finalisePtphDetail/deletePtphDetail` (Task 2); `SavePtphDetailCommand`, `Tier`, `ListType` (Task 1).
- Produces: `@Handles` for `hearing.command.save-ptph-detail`, `hearing.command.finalise-ptph-detail`, `hearing.command.delete-ptph-detail`.

- [ ] **Step 1: Write the failing test**

`PtphDetailCommandHandlerTest.java`:
```java
package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;
import static uk.gov.justice.services.test.utils.core.enveloper.EnveloperFactory.createEnveloperWithEvents;
import static uk.gov.justice.services.test.utils.core.helper.EventStreamMockHelper.verifyAppendAndGetArgumentFrom;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PtphDetailCommandHandlerTest {

    @Spy
    private final Enveloper enveloper = createEnveloperWithEvents(
            PtphDetailSaved.class, PtphDetailFinalised.class, PtphDetailDeleted.class);

    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter =
            new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private EventSource eventSource;
    @Mock
    private EventStream eventStream;
    @Mock
    private AggregateService aggregateService;

    @InjectMocks
    private PtphDetailCommandHandler handler;

    private final UUID hearingId = randomUUID();

    @BeforeEach
    void setUp() {
        when(eventSource.getStreamById(hearingId)).thenReturn(eventStream);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(new HearingAggregate());
    }

    @Test
    void savesTierOnly() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream);
        assertThat(appended.get(0).payloadAsJsonObject().getString("tier"), org.hamcrest.CoreMatchers.is("TIER_2"));
    }

    @Test
    void savesFixedListTypeWithReason() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .add("keyReason", "court ordered")
                        .build());

        handler.savePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream);
        assertThat(appended.get(0).payloadAsJsonObject().getString("keyReason"), org.hamcrest.CoreMatchers.is("court ordered"));
    }

    @Test
    void rejectsFixedListTypeWithoutReason() {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.save-ptph-detail"),
                createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .build());

        assertThat(assertThrows(RuntimeException.class, () -> handler.savePtphDetail(envelope)),
                instanceOf(RuntimeException.class));
    }

    @Test
    void finalises() throws EventStreamException {
        // aggregate has tier+listType applied first via a save so finalise passes
        final HearingAggregate aggregate = new HearingAggregate();
        aggregate.savePtphDetail(new PtphDetailSaved(hearingId, "TIER_2", "TYPE_2_FLEXIBLE", null))
                .forEach(aggregate::apply);
        when(aggregateService.get(eventStream, HearingAggregate.class)).thenReturn(aggregate);

        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.finalise-ptph-detail"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        handler.finalisePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream);
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
    }

    @Test
    void deletes() throws EventStreamException {
        final JsonEnvelope envelope = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.command.delete-ptph-detail"),
                createObjectBuilder().add("hearingId", hearingId.toString()).build());

        handler.deletePtphDetail(envelope);

        final List<JsonEnvelope> appended = verifyAppendAndGetArgumentFrom(eventStream);
        assertThat(appended.size(), org.hamcrest.CoreMatchers.is(1));
    }
}
```

> Note: if `HearingAggregate` has no public no-arg constructor, replace `new HearingAggregate()` with however existing handler tests obtain an aggregate instance (check `SetTrialTypeCommandHandler`-related tests / `HearingAggregateTest` for the idiom).

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-command/hearing-command-handler -am test -Dtest=PtphDetailCommandHandlerTest`
Expected: FAIL — `PtphDetailCommandHandler` does not exist.

- [ ] **Step 3: Write the handler**

```java
package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.ListType;
import uk.gov.moj.cpp.hearing.command.SavePtphDetailCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailDeleted;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailFinalised;
import uk.gov.moj.cpp.hearing.domain.event.PtphDetailSaved;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(COMMAND_HANDLER)
public class PtphDetailCommandHandler extends AbstractCommandHandler {

    private static final String HEARING_ID = "hearingId";

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailCommandHandler.class.getName());

    @Handles("hearing.command.save-ptph-detail")
    public void savePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.save-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final SavePtphDetailCommand command = convertToObject(envelope, SavePtphDetailCommand.class);
        final String keyReason = resolveKeyReason(command);

        aggregate(HearingAggregate.class, command.getHearingId(), envelope,
                a -> a.savePtphDetail(new PtphDetailSaved(
                        command.getHearingId(),
                        command.getTier() == null ? null : command.getTier().name(),
                        command.getListType() == null ? null : command.getListType().name(),
                        keyReason)));
    }

    @Handles("hearing.command.finalise-ptph-detail")
    public void finalisePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.finalise-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final UUID hearingId = hearingId(envelope);
        aggregate(HearingAggregate.class, hearingId, envelope,
                a -> a.finalisePtphDetail(new PtphDetailFinalised(hearingId)));
    }

    @Handles("hearing.command.delete-ptph-detail")
    public void deletePtphDetail(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.delete-ptph-detail received {}", envelope.toObfuscatedDebugString());

        final UUID hearingId = hearingId(envelope);
        aggregate(HearingAggregate.class, hearingId, envelope,
                a -> a.deletePtphDetail(new PtphDetailDeleted(hearingId)));
    }

    private String resolveKeyReason(final SavePtphDetailCommand command) {
        if (command.getListType() == ListType.TYPE_1_FIXED) {
            if (isBlank(command.getKeyReason())) {
                throw new RuntimeException("keyReason is required when listType is TYPE_1_FIXED");
            }
            return command.getKeyReason();
        }
        // keyReason only applies to a fixed-date list type; ignore otherwise
        return nonNull(command.getListType()) ? null : command.getKeyReason() == null ? null : null;
    }

    private UUID hearingId(final JsonEnvelope envelope) {
        return fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));
    }
}
```

> Simplify `resolveKeyReason`'s non-fixed branch to `return null;` if the reviewer prefers — the intent is: fixed ⇒ require reason; otherwise ⇒ no reason stored.

- [ ] **Step 4: Create the handler JSON schema + example files**

`schema/hearing.command.save-ptph-detail.json`:
```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/hearing/courts/hearing.command.save-ptph-detail.json",
  "type": "object",
  "properties": {
    "hearingId": { "$ref": "http://justice.gov.uk/domain/core/common/definitions.json#/definitions/uuid" },
    "tier": { "type": "string", "enum": ["TIER_1","TIER_2","TIER_3","TIER_4","TIER_5","TIER_6","TIER_7"] },
    "listType": { "type": "string", "enum": ["TYPE_1_FIXED","TYPE_2_FLEXIBLE"] },
    "keyReason": { "type": "string" }
  },
  "required": ["hearingId","tier"],
  "additionalProperties": false
}
```
`hearing.command.save-ptph-detail.json` (example):
```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_2",
  "listType": "TYPE_1_FIXED",
  "keyReason": "Trial fixed date required by court order"
}
```
`schema/hearing.command.finalise-ptph-detail.json`:
```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/hearing/courts/hearing.command.finalise-ptph-detail.json",
  "type": "object",
  "properties": {
    "hearingId": { "$ref": "http://justice.gov.uk/domain/core/common/definitions.json#/definitions/uuid" }
  },
  "additionalProperties": false
}
```
`hearing.command.finalise-ptph-detail.json` (example):
```json
{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" }
```
`schema/hearing.command.delete-ptph-detail.json`: identical to the finalise schema but with `id` ending `hearing.command.delete-ptph-detail.json`.
`hearing.command.delete-ptph-detail.json` (example): identical to the finalise example.

- [ ] **Step 5: Register the three media types in the handler messaging RAML**

In `hearing-command-handler.messaging.raml`, under the `body:` list (near the `set-trial-type` entry, ~line 335) add:
```raml
        application/vnd.hearing.command.save-ptph-detail+json:
            schema: !include json/schema/hearing.command.save-ptph-detail.json
            example: !include json/hearing.command.save-ptph-detail.json

        application/vnd.hearing.command.finalise-ptph-detail+json:
            schema: !include json/schema/hearing.command.finalise-ptph-detail.json
            example: !include json/hearing.command.finalise-ptph-detail.json

        application/vnd.hearing.command.delete-ptph-detail+json:
            schema: !include json/schema/hearing.command.delete-ptph-detail.json
            example: !include json/hearing.command.delete-ptph-detail.json
```

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -pl hearing-command/hearing-command-handler -am test -Dtest=PtphDetailCommandHandlerTest`
Expected: PASS (all five test methods).

- [ ] **Step 7: Commit**

```bash
git add hearing-command/hearing-command-handler/src/main/java/uk/gov/moj/cpp/hearing/command/handler/PtphDetailCommandHandler.java \
        hearing-command/hearing-command-handler/src/raml/json/ \
        hearing-command/hearing-command-handler/src/raml/hearing-command-handler.messaging.raml \
        hearing-command/hearing-command-handler/src/test/java/uk/gov/moj/cpp/hearing/command/handler/PtphDetailCommandHandlerTest.java
git commit -m "feat: tier & list type command handler with save/finalise/delete"
```

---

## Task 4: Command API layer (RAML + schemas + HearingCommandApi routing)

**Files:**
- Modify: `hearing-command/hearing-command-api/src/main/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApi.java`
- Modify: `hearing-command/hearing-command-api/src/raml/hearing-command-api.raml`
- Create: `hearing-command/hearing-command-api/src/raml/json/schema/hearing.save-ptph-detail.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/hearing.save-ptph-detail.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/schema/hearing.finalise-ptph-detail.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/hearing.finalise-ptph-detail.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/schema/hearing.delete-ptph-detail.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/hearing.delete-ptph-detail.json`
- Test: `hearing-command/hearing-command-api/src/test/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApiTest.java` (add methods)

**Interfaces:**
- Consumes: nothing new (delegates by name to Task 3 handler).
- Produces: `HearingCommandApi` `@Handles("hearing.save-ptph-detail")` (+ finalise, delete) each re-sending as `hearing.command.<same>` via `sendEnvelopeWithName`.

- [ ] **Step 1: Write the failing test**

Add to `HearingCommandApiTest.java` (mirror the existing `set-trial-type` test that builds an envelope named `hearing.set-trial-type` and verifies the sender is called with `hearing.command.set-trial-type`):
```java
    @Test
    public void shouldSendSavePtphDetail() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName("hearing.save-ptph-detail");
        hearingCommandApi.savePtphDetail(envelope);
        verify(sender).send(argThat(jsonEnvelope(
                metadata().withName("hearing.command.save-ptph-detail"), payloadIsJson(notNullValue(String.class)))));
    }

    @Test
    public void shouldSendFinalisePtphDetail() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName("hearing.finalise-ptph-detail");
        hearingCommandApi.finalisePtphDetail(envelope);
        verify(sender).send(argThat(jsonEnvelope(
                metadata().withName("hearing.command.finalise-ptph-detail"), payloadIsJson(notNullValue(String.class)))));
    }

    @Test
    public void shouldSendDeletePtphDetail() {
        final JsonEnvelope envelope = buildDummyJsonRequestEnvelopeWithName("hearing.delete-ptph-detail");
        hearingCommandApi.deletePtphDetail(envelope);
        verify(sender).send(argThat(jsonEnvelope(
                metadata().withName("hearing.command.delete-ptph-detail"), payloadIsJson(notNullValue(String.class)))));
    }
```

> Match the exact matcher idiom used by the existing `set-trial-type` test in this file — copy that test and change the names. If the existing test uses a different verification helper, use the same one.

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-command/hearing-command-api -am test -Dtest=HearingCommandApiTest`
Expected: FAIL — `savePtphDetail` etc. not defined on `HearingCommandApi`.

- [ ] **Step 3: Add the three routing methods to `HearingCommandApi`**

Next to the existing `setTrialType` method (~line 228):
```java
    @Handles("hearing.save-ptph-detail")
    public void savePtphDetail(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.save-ptph-detail");
    }

    @Handles("hearing.finalise-ptph-detail")
    public void finalisePtphDetail(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.finalise-ptph-detail");
    }

    @Handles("hearing.delete-ptph-detail")
    public void deletePtphDetail(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.delete-ptph-detail");
    }
```

- [ ] **Step 4: Register the commands in the command API RAML**

In `hearing-command-api.raml`, under the `POST /hearings/{hearingId}` resource's `(mapping):` list (~line 168, alongside `set-trial-type`) add:
```raml
        (mapping):
            requestType: application/vnd.hearing.save-ptph-detail+json
            name: hearing.save-ptph-detail
        (mapping):
            requestType: application/vnd.hearing.finalise-ptph-detail+json
            name: hearing.finalise-ptph-detail
        (mapping):
            requestType: application/vnd.hearing.delete-ptph-detail+json
            name: hearing.delete-ptph-detail
```
And in the same resource's `body:` block (~line 321, alongside the `set-trial-type` body) add:
```raml
        application/vnd.hearing.save-ptph-detail+json:
            example: !include json/hearing.save-ptph-detail.json
            schema: !include json/schema/hearing.save-ptph-detail.json
        application/vnd.hearing.finalise-ptph-detail+json:
            example: !include json/hearing.finalise-ptph-detail.json
            schema: !include json/schema/hearing.finalise-ptph-detail.json
        application/vnd.hearing.delete-ptph-detail+json:
            example: !include json/hearing.delete-ptph-detail.json
            schema: !include json/schema/hearing.delete-ptph-detail.json
```

- [ ] **Step 5: Create the API JSON schema + example files**

`schema/hearing.save-ptph-detail.json` — identical body to the handler save schema (Task 3 Step 4) but with `id` ending `hearing.save-ptph-detail.json`. Example `hearing.save-ptph-detail.json` — same content as the handler save example. Create the finalise/delete API schema + example files the same way (empty-object schema + `{ }` example, `id` matching the file name). All schemas remain draft-04 with `additionalProperties: false`.

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -pl hearing-command/hearing-command-api -am test -Dtest=HearingCommandApiTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add hearing-command/hearing-command-api/src/main/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApi.java \
        hearing-command/hearing-command-api/src/raml/
git add hearing-command/hearing-command-api/src/test/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApiTest.java
git commit -m "feat: expose tier & list type commands on the command API"
```

---

## Task 5: View-store entity, repository and Liquibase

**Files:**
- Create: `hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/persist/entity/ha/PtphDetail.java`
- Create: `hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/repository/PtphDetailRepository.java`
- Create: `hearing-viewstore/hearing-viewstore-liquibase/src/main/resources/liquibase/hearing-view-store-db-changesets/137-create-ptph-detail.xml`
- Test: `hearing-viewstore/hearing-viewstore-persistence/src/test/java/uk/gov/moj/cpp/hearing/persist/entity/ha/PtphDetailTest.java`

**Interfaces:**
- Produces:
  - Entity `PtphDetail` — `UUID getHearingId()/setHearingId`, `String getTier()/setTier`, `String getListType()/setListType`, `String getKeyReason()/setKeyReason`, `boolean isFinalised()/setFinalised`.
  - Repository `PtphDetailRepository extends EntityRepository<PtphDetail, UUID>` — inherits `findBy(UUID)`, `save(entity)`, `removeAndFlush(entity)`.

- [ ] **Step 1: Write the failing test**

`PtphDetailTest.java`:
```java
package uk.gov.moj.cpp.hearing.persist.entity.ha;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class PtphDetailTest {

    @Test
    void holdsFields() {
        final UUID hearingId = randomUUID();
        final PtphDetail entity = new PtphDetail();
        entity.setHearingId(hearingId);
        entity.setTier("TIER_2");
        entity.setListType("TYPE_1_FIXED");
        entity.setKeyReason("reason");
        entity.setFinalised(true);

        assertThat(entity.getHearingId(), is(hearingId));
        assertThat(entity.getTier(), is("TIER_2"));
        assertThat(entity.getListType(), is("TYPE_1_FIXED"));
        assertThat(entity.getKeyReason(), is("reason"));
        assertThat(entity.isFinalised(), is(true));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-viewstore/hearing-viewstore-persistence -am test -Dtest=PtphDetailTest`
Expected: FAIL — `PtphDetail` does not exist.

- [ ] **Step 3: Create the entity**

```java
package uk.gov.moj.cpp.hearing.persist.entity.ha;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ha_ptph_detail")
public class PtphDetail {

    @Id
    @Column(name = "hearing_id", nullable = false)
    private UUID hearingId;

    @Column(name = "tier")
    private String tier;

    @Column(name = "list_type")
    private String listType;

    @Column(name = "key_reason")
    private String keyReason;

    @Column(name = "finalised", nullable = false)
    private boolean finalised;

    public UUID getHearingId() {
        return hearingId;
    }

    public void setHearingId(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(final String tier) {
        this.tier = tier;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(final String listType) {
        this.listType = listType;
    }

    public String getKeyReason() {
        return keyReason;
    }

    public void setKeyReason(final String keyReason) {
        this.keyReason = keyReason;
    }

    public boolean isFinalised() {
        return finalised;
    }

    public void setFinalised(final boolean finalised) {
        this.finalised = finalised;
    }
}
```

- [ ] **Step 4: Create the repository**

```java
package uk.gov.moj.cpp.hearing.repository;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;

import java.util.UUID;

import org.apache.deltaspike.data.api.EntityRepository;
import org.apache.deltaspike.data.api.Repository;

@Repository
public interface PtphDetailRepository extends EntityRepository<PtphDetail, UUID> {
}
```

- [ ] **Step 5: Create the Liquibase changeset**

`137-create-ptph-detail.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
                                       http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.3.xsd">

    <changeSet id="137" author="hearing" logicalFilePath="137-create-ptph-detail">

        <createTable tableName="ha_ptph_detail">

            <column name="hearing_id" type="UUID">
                <constraints nullable="false" primaryKey="true"/>
            </column>

            <column name="tier" type="VARCHAR(20)"/>

            <column name="list_type" type="VARCHAR(20)"/>

            <column name="key_reason" type="TEXT"/>

            <column name="finalised" type="BOOLEAN" defaultValueBoolean="false">
                <constraints nullable="false"/>
            </column>

        </createTable>

    </changeSet>

</databaseChangeLog>
```

> Confirm the highest existing changeset number before committing: `ls hearing-viewstore/hearing-viewstore-liquibase/src/main/resources/liquibase/hearing-view-store-db-changesets/ | sort | tail -3`. If a number above `136` now exists, bump this file and its `id`/`logicalFilePath` accordingly.

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -pl hearing-viewstore/hearing-viewstore-persistence -am test -Dtest=PtphDetailTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/persist/entity/ha/PtphDetail.java \
        hearing-viewstore/hearing-viewstore-persistence/src/main/java/uk/gov/moj/cpp/hearing/repository/PtphDetailRepository.java \
        hearing-viewstore/hearing-viewstore-liquibase/src/main/resources/liquibase/hearing-view-store-db-changesets/137-create-ptph-detail.xml \
        hearing-viewstore/hearing-viewstore-persistence/src/test/java/uk/gov/moj/cpp/hearing/persist/entity/ha/PtphDetailTest.java
git commit -m "feat: tier & list type view-store entity, repository and table"
```

---

## Task 6: Event listener (+ event schemas + subscription registration)

**Files:**
- Create: `hearing-event/hearing-event-listener/src/main/java/uk/gov/moj/cpp/hearing/event/listener/PtphDetailEventListener.java`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/schema/hearing.ptph-detail-saved.json`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/hearing.ptph-detail-saved.json`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/schema/hearing.ptph-detail-finalised.json`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/hearing.ptph-detail-finalised.json`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/schema/hearing.ptph-detail-deleted.json`
- Create: `hearing-event/hearing-event-listener/src/yaml/json/hearing.ptph-detail-deleted.json`
- Modify: `hearing-event/hearing-event-listener/src/yaml/subscriptions-descriptor.yaml`
- Test: `hearing-event/hearing-event-listener/src/test/java/uk/gov/moj/cpp/hearing/event/listener/PtphDetailEventListenerTest.java`

**Interfaces:**
- Consumes: `PtphDetailSaved/Finalised/Deleted` (Task 1), `PtphDetail` entity + `PtphDetailRepository` (Task 5).
- Produces: `@ServiceComponent(EVENT_LISTENER)` listener with `@Handles` for the three event names.

- [ ] **Step 1: Write the failing test**

`PtphDetailEventListenerTest.java`:
```java
package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.test.utils.framework.api.JsonObjectConvertersFactory;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import javax.json.Json;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;

@ExtendWith(MockitoExtension.class)
class PtphDetailEventListenerTest {

    @Spy
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter =
            new JsonObjectConvertersFactory().jsonObjectToObjectConverter();

    @Mock
    private PtphDetailRepository repository;

    @Captor
    private ArgumentCaptor<PtphDetail> captor;

    @InjectMocks
    private PtphDetailEventListener listener;

    @Test
    void savedUpsertsRow() {
        final UUID hearingId = randomUUID();
        when(repository.findBy(hearingId)).thenReturn(null);

        listener.ptphDetailSaved(JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-saved"),
                Json.createObjectBuilder()
                        .add("hearingId", hearingId.toString())
                        .add("tier", "TIER_2")
                        .add("listType", "TYPE_1_FIXED")
                        .add("keyReason", "reason")
                        .build()));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getTier(), is("TIER_2"));
        assertThat(captor.getValue().getKeyReason(), is("reason"));
    }

    @Test
    void finalisedSetsFlag() {
        final UUID hearingId = randomUUID();
        final PtphDetail existing = new PtphDetail();
        existing.setHearingId(hearingId);
        when(repository.findBy(hearingId)).thenReturn(existing);

        listener.ptphDetailFinalised(JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-finalised"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build()));

        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isFinalised(), is(true));
    }

    @Test
    void deletedRemovesRow() {
        final UUID hearingId = randomUUID();
        final PtphDetail existing = new PtphDetail();
        existing.setHearingId(hearingId);
        when(repository.findBy(hearingId)).thenReturn(existing);

        listener.ptphDetailDeleted(JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.ptph-detail-deleted"),
                Json.createObjectBuilder().add("hearingId", hearingId.toString()).build()));

        verify(repository).removeAndFlush(existing);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-event/hearing-event-listener -am test -Dtest=PtphDetailEventListenerTest`
Expected: FAIL — listener does not exist.

- [ ] **Step 3: Write the listener**

```java
package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(EVENT_LISTENER)
public class PtphDetailEventListener {

    private static final String HEARING_ID = "hearingId";

    private static final Logger LOGGER = LoggerFactory.getLogger(PtphDetailEventListener.class.getName());

    @Inject
    private PtphDetailRepository repository;

    @Handles("hearing.ptph-detail-saved")
    public void ptphDetailSaved(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-saved received {}", event.toObfuscatedDebugString());
        final JsonObject payload = event.payloadAsJsonObject();
        final UUID hearingId = fromString(payload.getString(HEARING_ID));

        PtphDetail entity = repository.findBy(hearingId);
        if (entity == null) {
            entity = new PtphDetail();
            entity.setHearingId(hearingId);
        }
        entity.setTier(payload.containsKey("tier") ? payload.getString("tier") : null);
        entity.setListType(payload.containsKey("listType") ? payload.getString("listType") : null);
        entity.setKeyReason(payload.containsKey("keyReason") ? payload.getString("keyReason") : null);
        repository.save(entity);
    }

    @Handles("hearing.ptph-detail-finalised")
    public void ptphDetailFinalised(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-finalised received {}", event.toObfuscatedDebugString());
        final UUID hearingId = fromString(event.payloadAsJsonObject().getString(HEARING_ID));

        final PtphDetail entity = repository.findBy(hearingId);
        if (entity != null) {
            entity.setFinalised(true);
            repository.save(entity);
        }
    }

    @Handles("hearing.ptph-detail-deleted")
    public void ptphDetailDeleted(final JsonEnvelope event) {
        LOGGER.debug("hearing.ptph-detail-deleted received {}", event.toObfuscatedDebugString());
        final UUID hearingId = fromString(event.payloadAsJsonObject().getString(HEARING_ID));

        final PtphDetail entity = repository.findBy(hearingId);
        if (entity != null) {
            repository.removeAndFlush(entity);
        }
    }
}
```

- [ ] **Step 4: Create the listener event JSON schemas + examples**

`schema/hearing.ptph-detail-saved.json`:
```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/hearing/courts/hearing.ptph-detail-saved.json",
  "type": "object",
  "properties": {
    "hearingId": { "$ref": "http://justice.gov.uk/domain/core/common/definitions.json#/definitions/uuid" },
    "tier": { "type": "string" },
    "listType": { "type": "string" },
    "keyReason": { "type": "string" }
  },
  "required": ["hearingId"],
  "additionalProperties": false
}
```
`hearing.ptph-detail-saved.json` (example):
```json
{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4", "tier": "TIER_2", "listType": "TYPE_1_FIXED", "keyReason": "reason" }
```
`schema/hearing.ptph-detail-finalised.json` and `schema/hearing.ptph-detail-deleted.json`: single required `hearingId` property, `additionalProperties: false`, `id` matching the file name. Their examples: `{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" }`.

- [ ] **Step 5: Register the three events in the listener subscriptions descriptor**

In `subscriptions-descriptor.yaml`, add under the same `events:` list that already contains `hearing.hearing-trial-type-set` (~line 198), matching the existing `schema_uri` style used by neighbouring entries:
```yaml
        - name: hearing.ptph-detail-saved
          schema_uri: http://justice.gov.uk/hearing/courts/hearing.ptph-detail-saved.json
        - name: hearing.ptph-detail-finalised
          schema_uri: http://justice.gov.uk/hearing/courts/hearing.ptph-detail-finalised.json
        - name: hearing.ptph-detail-deleted
          schema_uri: http://justice.gov.uk/hearing/courts/hearing.ptph-detail-deleted.json
```

> Use the exact `schema_uri` base that the neighbouring trial-type entry uses in THIS file (the agent saw `http://justice.gov.uk/hearing/courts/...` for trial-type; if the file's convention differs, match the file). The `schema_uri` must resolve to the schema file created in Step 4.

- [ ] **Step 6: Run test to verify it passes**

Run: `mvn -pl hearing-event/hearing-event-listener -am test -Dtest=PtphDetailEventListenerTest`
Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add hearing-event/hearing-event-listener/src/main/java/uk/gov/moj/cpp/hearing/event/listener/PtphDetailEventListener.java \
        hearing-event/hearing-event-listener/src/yaml/ \
        hearing-event/hearing-event-listener/src/test/java/uk/gov/moj/cpp/hearing/event/listener/PtphDetailEventListenerTest.java
git commit -m "feat: tier & list type event listener updating the view store"
```

---

## Task 7: Query side (response DTO, service, view, API handler, RAML)

**Files:**
- Create: `hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/response/PtphDetailResponse.java`
- Modify: `hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/service/HearingService.java`
- Modify: `hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/HearingQueryView.java`
- Modify: `hearing-query/hearing-query-api/src/main/java/uk/gov/moj/cpp/hearing/query/api/HearingQueryApi.java`
- Modify: `hearing-query/hearing-query-api/src/raml/hearing-query-api.raml`
- Create: `hearing-query/hearing-query-api/src/raml/json/schema/hearing.get-ptph-detail.json`
- Create: `hearing-query/hearing-query-api/src/raml/json/hearing.get-ptph-detail.json`
- Test: `hearing-query/hearing-query-view/src/test/java/uk/gov/moj/cpp/hearing/query/view/service/HearingServicePtphDetailTest.java`

**Interfaces:**
- Consumes: `PtphDetail` + `PtphDetailRepository` (Task 5).
- Produces:
  - `PtphDetailResponse(String tier, String listType, String keyReason, boolean finalised)` with getters.
  - `HearingService.getPtphDetail(UUID hearingId): PtphDetailResponse` (`@Transactional`) — returns response with null fields + `finalised=false` when no row exists.
  - `HearingQueryView.getPtphDetail(JsonEnvelope): Envelope<PtphDetailResponse>`.
  - `HearingQueryApi` `@Handles("hearing.get-ptph-detail")`.

- [ ] **Step 1: Write the failing test**

`HearingServicePtphDetailTest.java`:
```java
package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import uk.gov.moj.cpp.hearing.persist.entity.ha.PtphDetail;
import uk.gov.moj.cpp.hearing.query.view.response.PtphDetailResponse;
import uk.gov.moj.cpp.hearing.repository.PtphDetailRepository;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HearingServicePtphDetailTest {

    @Mock
    private PtphDetailRepository ptphDetailRepository;

    @InjectMocks
    private HearingService hearingService;

    @Test
    void returnsSavedRow() {
        final UUID hearingId = randomUUID();
        final PtphDetail entity = new PtphDetail();
        entity.setHearingId(hearingId);
        entity.setTier("TIER_2");
        entity.setListType("TYPE_1_FIXED");
        entity.setKeyReason("reason");
        entity.setFinalised(true);
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(entity);

        final PtphDetailResponse response = hearingService.getPtphDetail(hearingId);

        assertThat(response.getTier(), is("TIER_2"));
        assertThat(response.getListType(), is("TYPE_1_FIXED"));
        assertThat(response.getKeyReason(), is("reason"));
        assertThat(response.isFinalised(), is(true));
    }

    @Test
    void returnsEmptyWhenAbsent() {
        final UUID hearingId = randomUUID();
        when(ptphDetailRepository.findBy(hearingId)).thenReturn(null);

        final PtphDetailResponse response = hearingService.getPtphDetail(hearingId);

        assertThat(response.getTier(), is((Object) null));
        assertThat(response.isFinalised(), is(false));
    }
}
```

> `HearingService` has many collaborators; `@InjectMocks` will leave the others null, which is fine because these tests exercise only the new method. If Mockito's constructor injection complains, add the other `@Mock` fields the constructor needs (copy from the existing `HearingServiceTest`).

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -pl hearing-query/hearing-query-view -am test -Dtest=HearingServicePtphDetailTest`
Expected: FAIL — response class + method not defined.

- [ ] **Step 3: Create the response DTO**

```java
package uk.gov.moj.cpp.hearing.query.view.response;

public class PtphDetailResponse {

    private final String tier;
    private final String listType;
    private final String keyReason;
    private final boolean finalised;

    public PtphDetailResponse(final String tier, final String listType, final String keyReason, final boolean finalised) {
        this.tier = tier;
        this.listType = listType;
        this.keyReason = keyReason;
        this.finalised = finalised;
    }

    public String getTier() {
        return tier;
    }

    public String getListType() {
        return listType;
    }

    public String getKeyReason() {
        return keyReason;
    }

    public boolean isFinalised() {
        return finalised;
    }
}
```

- [ ] **Step 4: Add the service read method**

In `HearingService.java` add the injected repository (next to the other `@Inject` repositories, e.g. near `draftResultRepository`):
```java
    @Inject
    private PtphDetailRepository ptphDetailRepository;
```
Add imports for `PtphDetail`, `PtphDetailRepository`, `PtphDetailResponse`, and add the method:
```java
    @Transactional
    public PtphDetailResponse getPtphDetail(final UUID hearingId) {
        final PtphDetail entity = ptphDetailRepository.findBy(hearingId);
        if (entity == null) {
            return new PtphDetailResponse(null, null, null, false);
        }
        return new PtphDetailResponse(entity.getTier(), entity.getListType(), entity.getKeyReason(), entity.isFinalised());
    }
```

- [ ] **Step 5: Add the query-view method**

In `HearingQueryView.java` add (mirroring `getDraftResult`, using the existing `FIELD_HEARING_ID` constant and `envelop(...).withName(...).withMetadataFrom(...)` idiom present in the file):
```java
    public Envelope<PtphDetailResponse> getPtphDetail(final JsonEnvelope envelope) {
        final UUID hearingId = fromString(envelope.payloadAsJsonObject().getString(FIELD_HEARING_ID));
        final PtphDetailResponse response = hearingService.getPtphDetail(hearingId);

        return envelop(response)
                .withName("hearing.get-ptph-detail")
                .withMetadataFrom(envelope);
    }
```

> Use the same static `envelop`/`fromString` imports and `FIELD_HEARING_ID` constant the file already uses for `getDraftResult`. Add the `PtphDetailResponse` import.

- [ ] **Step 6: Add the query-api handler**

In `HearingQueryApi.java` add (next to `getDraftResult`, using the file's existing `getJsonEnvelope(...)` helper):
```java
    @Handles("hearing.get-ptph-detail")
    public JsonEnvelope getPtphDetail(final JsonEnvelope query) {
        return getJsonEnvelope(this.hearingQueryView.getPtphDetail(query));
    }
```

- [ ] **Step 7: Add the RAML query resource + response schema/example**

In `hearing-query-api.raml` add a new top-level resource (mirroring `/hearings/{hearingId}/draft-result`, ~line 45):
```raml
/hearings/{hearingId}/ptph-detail:
  uriParameters:
        hearingId:
          description: ID of the hearing to get the tier and list type
          type: string
  get:
    description: |
      Returns the tier and list type for a hearing.
      (mapping):
          responseType: application/vnd.hearing.get-ptph-detail+json
          name: hearing.get-ptph-detail
    responses:
          200:
              description: OK
              body:
                application/vnd.hearing.get-ptph-detail+json:
                  example: !include json/hearing.get-ptph-detail.json
                  schema: !include json/schema/hearing.get-ptph-detail.json
```
`schema/hearing.get-ptph-detail.json`:
```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/hearing/courts/hearing.get-ptph-detail.json",
  "type": "object",
  "properties": {
    "tier": { "type": ["string", "null"] },
    "listType": { "type": ["string", "null"] },
    "keyReason": { "type": ["string", "null"] },
    "finalised": { "type": "boolean" }
  },
  "required": ["finalised"],
  "additionalProperties": false
}
```
`hearing.get-ptph-detail.json` (example):
```json
{ "tier": "TIER_2", "listType": "TYPE_1_FIXED", "keyReason": "Trial fixed date required by court order", "finalised": true }
```

- [ ] **Step 8: Run test to verify it passes**

Run: `mvn -pl hearing-query/hearing-query-view -am test -Dtest=HearingServicePtphDetailTest`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/response/PtphDetailResponse.java \
        hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/service/HearingService.java \
        hearing-query/hearing-query-view/src/main/java/uk/gov/moj/cpp/hearing/query/view/HearingQueryView.java \
        hearing-query/hearing-query-api/src/main/java/uk/gov/moj/cpp/hearing/query/api/HearingQueryApi.java \
        hearing-query/hearing-query-api/src/raml/ \
        hearing-query/hearing-query-view/src/test/java/uk/gov/moj/cpp/hearing/query/view/service/HearingServicePtphDetailTest.java
git commit -m "feat: query endpoint for tier & list type"
```

---

## Task 8: Access control + full build verification

**Files:**
- Modify: the access-control descriptor for the new command/query actions (path discovered in Step 1).
- Modify: `hearing-command/hearing-command-api/src/test/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApiAccessControlTest.java` (if it enumerates allowed actions).

**Interfaces:**
- Consumes: the action names from all prior tasks.
- Produces: authorization entries so the four new actions are permitted for the same roles as `hearing.set-trial-type`.

- [ ] **Step 1: Locate the access-control mechanism**

Run:
```bash
grep -rn "hearing.set-trial-type" --include="*.yaml" --include="*.yml" --include="*.json" --include="*.java" . | grep -iv target | grep -i "access\|policy\|role\|authoris\|authoriz"
grep -rln "ACTION_NAME_SET_TRIAL_TYPE\|set-trial-type" hearing-command/hearing-command-api/src/test
```
Identify the file(s) that grant access to `hearing.set-trial-type` (the `HearingCommandApiAccessControlTest` at `hearing-command/hearing-command-api/src/test/.../HearingCommandApiAccessControlTest.java:50` references `ACTION_NAME_SET_TRIAL_TYPE`).

- [ ] **Step 2: Write/extend the failing test**

If `HearingCommandApiAccessControlTest` asserts each action is authorized, add constants + assertions for the three new command actions and the query action, following the exact pattern used for `ACTION_NAME_SET_TRIAL_TYPE`:
```java
    private static final String ACTION_NAME_SAVE_TIER_AND_LIST_TYPE = "hearing.save-ptph-detail";
    private static final String ACTION_NAME_FINALISE_TIER_AND_LIST_TYPE = "hearing.finalise-ptph-detail";
    private static final String ACTION_NAME_DELETE_TIER_AND_LIST_TYPE = "hearing.delete-ptph-detail";
```
and the corresponding assertion(s) copied from the `set-trial-type` case.

- [ ] **Step 3: Run test to verify it fails**

Run: `mvn -pl hearing-command/hearing-command-api -am test -Dtest=HearingCommandApiAccessControlTest`
Expected: FAIL — new actions not yet authorized.

- [ ] **Step 4: Add the authorization entries**

In the access-control descriptor discovered in Step 1, add entries for `hearing.save-ptph-detail`, `hearing.finalise-ptph-detail`, `hearing.delete-ptph-detail` and `hearing.get-ptph-detail`, granting the same roles/providers the trial-type action uses. Copy the `set-trial-type` block verbatim and change the action name.

- [ ] **Step 5: Run test to verify it passes**

Run: `mvn -pl hearing-command/hearing-command-api -am test -Dtest=HearingCommandApiAccessControlTest`
Expected: PASS.

- [ ] **Step 6: Full build**

Run: `mvn clean install -DskipITs`
Expected: BUILD SUCCESS across all modules (RAML generation, schema validation, unit tests all green).

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: access control for tier & list type command and query actions"
```

---

## Self-Review Notes

- **Spec coverage:** save/edit (Tasks 3–4, single upsert command), finalise with tier+list-type precondition (Task 2 delegate, Task 3–4 command), reject edits once finalised (Task 2), delete any-time clears all (Task 2), Type-1-requires-reason (Task 3 handler), dedicated view table (Task 5), listener upsert/finalise/delete (Task 6), query endpoint (Task 7), access control (Task 8), tier enum 1–7 + list-type enum (Task 1). All spec sections mapped.
- **Deferred-to-file conventions (intentional, not placeholders):** exact `schema_uri` base in the listener descriptor, the exact access-control descriptor path, and the `HearingCommandApiTest` matcher idiom — each step says "match the existing `set-trial-type`/neighbouring entry", because those strings must be read from the live file at execution time rather than guessed. The executor confirms them against the named reference.
- **Type consistency:** event fields are `String` throughout (event → listener → entity → response); enums (`Tier`, `ListType`) appear only in `SavePtphDetailCommand` and JSON schema; momento/entity store `String`; `finalised` is `boolean` everywhere; method names (`savePtphDetail`, `finalisePtphDetail`, `deletePtphDetail`, `getPtphDetail`) are identical across aggregate, handler, listener, view and API.
