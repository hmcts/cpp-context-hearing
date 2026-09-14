# Reserve a Slot — cpp-context-hearing Implementation Plan (Plan 2 of 5)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Carry the hearing's estimated `duration` from the slot-pick request through to courtscheduler, so a reservation on a duration-based session holds the right number of minutes.

**Architecture:** `hearing.book-provisional-hearing-slots` already carries `courtScheduleId` and `hearingStartTime` per slot and reaches courtscheduler's `POST /provisionalBooking` through an aggregate event and an event processor. This plan threads one optional `Integer duration` along that existing path. No new command, no new endpoint, no behaviour of its own — hearing stays a pass-through.

**Tech Stack:** Java 17, **Maven**, CDI (`@Inject`), HMCTS Justice Services Framework, JUnit, Mockito.

**Spec:** https://claude.ai/code/artifact/90e6b0eb-dc8a-4697-a8d4-8d9086d283d6 ("Reserve a Slot")

**Repo:** `cpp-context-hearing`, branch `team/ccsph2`

## Why this is now urgent rather than cosmetic

Plan 1 shipped a change to courtscheduler that **rejects** a reservation on a duration-based session when no usable duration is supplied — `ReservationService` throws a `ValidationException`, mapped to `400 BAD_REQUEST`. Before that fix the request silently held zero minutes, which was worse but invisible. Until this plan lands, **every pick on a duration-based session will fail with a 400.** Slot-based sessions are unaffected.

## Global Constraints

- Build tool is **Maven**. Never Gradle. `mvn` from the repo root.
- `duration` is **optional** end to end: absent from every `required` list, nullable in Java. A slot-based session needs no duration and existing callers must keep working unchanged.
- `duration` is the hearing's estimated length in **minutes**, as an integer.
- Hearing is a **pass-through**. It must not default, derive, clamp or validate the value — courtscheduler owns that decision and already rejects the cases it cares about. Passing `null` through as absent is correct behaviour, not a gap.
- Do not change the path, media type or response of `POST /hearings/{hearingId}/hearing-slots`, and do not change `public.hearing.hearing-slots-provisionally-booked`.
- The command-api slot item schema has no `additionalProperties: false`, so adding a property is backward compatible. Do not add one.

**Build and test commands**

```bash
mvn -q -pl hearing-domain/hearing-domain-common,hearing-domain/hearing-domain-event -am test
mvn -q -pl hearing-event/hearing-event-processor -am test
mvn -q clean install -DskipTests
```

---

### Task 1: Thread `duration` from the command through to the courtscheduler payload

The whole change is one field along one existing path, so it is one task: the schema that admits it, the domain type that carries it, the event that must not drop it, and the processor that sends it. Splitting these would produce intermediate states where the field exists but goes nowhere, which a reviewer could not meaningfully accept or reject in isolation.

**Files:**
- Modify: `hearing-domain/hearing-domain-common/src/main/java/uk/gov/moj/cpp/hearing/command/bookprovisional/ProvisionalHearingSlotInfo.java`
- Modify: `hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/BookProvisionalHearingSlots.java`
- Modify: `hearing-command/hearing-command-api/src/raml/json/schema/hearing.book-provisional-hearing-slots.json`
- Modify: `hearing-command/hearing-command-api/src/raml/json/hearing.book-provisional-hearing-slots.json`
- Modify: `hearing-event/hearing-event-processor/src/main/java/uk/gov/moj/cpp/hearing/event/BookProvisionalHearingSlotsProcessor.java`
- Test: `hearing-event/hearing-event-processor/src/test/java/uk/gov/moj/cpp/hearing/event/BookProvisionalHearingSlotsProcessorTest.java`

**Interfaces:**
- Consumes: nothing from earlier plans at the code level. It targets courtscheduler's `POST /provisionalBooking`, whose request schema gained an optional integer `duration` per slot in Plan 1 Task 4.
- Produces: `ProvisionalHearingSlotInfo.getDuration()` → `Integer` (nullable), `setDuration(Integer)` returning `this` to match the existing fluent style. The outbound courtscheduler payload gains `duration` per slot **only when non-null**.

**The two places this can silently break, both covered below:**
1. `BookProvisionalHearingSlots.addSlotInfoFromMap` rebuilds each slot field by field from a `Map` rather than deserialising the whole object. A new field not added there is dropped on the way out of the event store, and nothing fails — the slot simply arrives at the processor with a null duration.
2. `JsonObjectBuilder.add(String, ...)` throws on a null value, so the processor must omit the key rather than add null.

- [ ] **Step 1: Write the failing processor test**

Add to `BookProvisionalHearingSlotsProcessorTest` (match the class's existing setup and mock style — read it first):

```java
    @Test
    public void shouldSendDurationToCourtSchedulerWhenPresent() {
        final UUID hearingId = randomUUID();
        final UUID courtScheduleId = randomUUID();
        final BookProvisionalHearingSlots event = BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(hearingId)
                .withSlots(List.of(new ProvisionalHearingSlotInfo()
                        .setCourtScheduleId(courtScheduleId)
                        .setHearingStartTime(ZonedDateTime.parse("2026-10-14T10:00:00.000Z"))
                        .setDuration(90)))
                .build();

        processorHandles(event);

        final JsonObject sentSlot = capturedProvisionalSlots().getJsonObject(0);
        assertThat(sentSlot.getInt("duration"), is(90));
    }

    @Test
    public void shouldOmitDurationEntirelyWhenNull() {
        final UUID hearingId = randomUUID();
        final UUID courtScheduleId = randomUUID();
        final BookProvisionalHearingSlots event = BookProvisionalHearingSlots.bookProvisionalHearingSlots()
                .withHearingId(hearingId)
                .withSlots(List.of(new ProvisionalHearingSlotInfo()
                        .setCourtScheduleId(courtScheduleId)
                        .setHearingStartTime(ZonedDateTime.parse("2026-10-14T10:00:00.000Z"))))
                .build();

        processorHandles(event);

        final JsonObject sentSlot = capturedProvisionalSlots().getJsonObject(0);
        assertThat(sentSlot.containsKey("duration"), is(false));
    }
```

`processorHandles(...)` and `capturedProvisionalSlots()` stand for however the existing tests in that class invoke the processor and capture the payload passed to `ProvisionalBookingService.bookSlots`. Reuse the existing helpers or inline the same construction the neighbouring tests use — do not invent a new harness.

The second test matters as much as the first: `JsonObjectBuilder.add` throws `NullPointerException` on a null value, so a naive implementation fails loudly here rather than sending a null.

- [ ] **Step 2: Write the failing event round-trip test**

The event's `Map`-based reconstruction is the silent-drop risk. Add a test in the `hearing-domain-event` module beside the existing `BookProvisionalHearingSlots` tests (create the class if none exists):

```java
    @Test
    public void shouldPreserveDurationWhenSlotsArriveAsMaps() {
        final UUID courtScheduleId = randomUUID();
        final Map<String, Object> slotAsMap = Map.of(
                "courtScheduleId", courtScheduleId.toString(),
                "hearingStartTime", "2026-10-14T10:00:00.000Z",
                "duration", 90);

        final BookProvisionalHearingSlots event = new BookProvisionalHearingSlots(
                randomUUID(), List.of(slotAsMap), null, null, null);

        assertThat(event.getSlots().get(0).getDuration(), is(90));
    }

    @Test
    public void shouldTolerateSlotMapsWithNoDuration() {
        final UUID courtScheduleId = randomUUID();
        final Map<String, Object> slotAsMap = Map.of(
                "courtScheduleId", courtScheduleId.toString(),
                "hearingStartTime", "2026-10-14T10:00:00.000Z");

        final BookProvisionalHearingSlots event = new BookProvisionalHearingSlots(
                randomUUID(), List.of(slotAsMap), null, null, null);

        assertThat(event.getSlots().get(0).getDuration(), is(nullValue()));
    }
```

This is the path a replayed event takes, so it is not hypothetical.

- [ ] **Step 3: Run both test sets to verify they fail**

```bash
mvn -q -pl hearing-domain/hearing-domain-event -am test -Dtest=BookProvisionalHearingSlotsTest
mvn -q -pl hearing-event/hearing-event-processor -am test -Dtest=BookProvisionalHearingSlotsProcessorTest
```
Expected: compilation failure on `setDuration` / `getDuration` — the methods do not exist yet.

- [ ] **Step 4: Add the field to `ProvisionalHearingSlotInfo`**

Alongside `hearingStartTime`, keeping the existing fluent setter style:

```java
    private Integer duration;

    public Integer getDuration() {
        return duration;
    }

    public ProvisionalHearingSlotInfo setDuration(final Integer duration) {
        this.duration = duration;
        return this;
    }
```

`Integer`, not `int` — absent must stay distinguishable from zero, because courtscheduler treats a missing duration on a duration-based session as a 400 and zero as a zero-minute hold.

- [ ] **Step 5: Carry it through the event's map reconstruction**

In `BookProvisionalHearingSlots.addSlotInfoFromMap`, read and set it:

```java
    private void addSlotInfoFromMap(final Map<String, Object> slot) {
        final Object hearingStartTimeObject = slot.get("hearingStartTime");
        final Object durationObject = slot.get("duration");
        final UUID courtScheduleIdUUID = UUID.fromString(slot.get("courtScheduleId").toString());
        final ZonedDateTime hearingStartTime = nonNull(hearingStartTimeObject) ? ZonedDateTime.parse(hearingStartTimeObject.toString()) : null;
        // Slots arrive here as raw maps on event replay, rebuilt field by field — a field not
        // read here is silently dropped, with no deserialisation error to notice.
        final Integer duration = nonNull(durationObject) ? Integer.valueOf(durationObject.toString()) : null;
        final ProvisionalHearingSlotInfo provisionalHearingSlotInfo = new ProvisionalHearingSlotInfo()
                .setCourtScheduleId(courtScheduleIdUUID)
                .setHearingStartTime(hearingStartTime)
                .setDuration(duration);
        slots.add(provisionalHearingSlotInfo);
    }
```

`Integer.valueOf(o.toString())` rather than a cast, because JSON numbers may arrive as `Integer`, `Long` or `BigDecimal` depending on the deserialiser. If the existing code in this repo has a convention for that, follow it instead.

- [ ] **Step 6: Send it from the processor, omitting null**

In `BookProvisionalHearingSlotsProcessor.handleBookProvisionalHearingSlots`, the slot builder becomes:

```java
        bookProvisionalHearingSlots.getSlots().forEach(
                bookProvisionalHearingSlotsCommand -> {
                    final String hearingStartTimeStr = Objects.nonNull(bookProvisionalHearingSlotsCommand.getHearingStartTime()) ?
                            bookProvisionalHearingSlotsCommand.getHearingStartTime().format(DATE_TIME_FORMATTER) : StringUtils.EMPTY;
                    final JsonObjectBuilder slotBuilder = createObjectBuilder()
                            .add("courtScheduleId", bookProvisionalHearingSlotsCommand.getCourtScheduleId().toString())
                            .add("hearingStartTime", hearingStartTimeStr);
                    // Omit rather than send null: JsonObjectBuilder.add rejects nulls, and
                    // courtscheduler treats an absent duration as "slot-based, none needed".
                    if (Objects.nonNull(bookProvisionalHearingSlotsCommand.getDuration())) {
                        slotBuilder.add("duration", bookProvisionalHearingSlotsCommand.getDuration());
                    }
                    arrayBuilder.add(slotBuilder.build());
                }
        );
```

Add the `jakarta.json.JsonObjectBuilder` (or `javax.json.JsonObjectBuilder` — match the file's existing imports) import.

- [ ] **Step 7: Add `duration` to the command-api schema and example**

In `hearing-command/hearing-command-api/src/raml/json/schema/hearing.book-provisional-hearing-slots.json`, inside the `slots` item `properties`:

```json
          "duration": {
            "description": "Hearing duration in minutes. Required by courtscheduler for duration-based sessions, which reject a reservation without it; ignored for slot-based sessions.",
            "type": "integer"
          }
```

Leave `required` as `["courtScheduleId", "hearingStartTime"]` — do **not** add `duration` to it. Add `"duration": 60` to each slot in the example file `hearing-command/hearing-command-api/src/raml/json/hearing.book-provisional-hearing-slots.json`.

- [ ] **Step 8: Run the tests to verify they pass**

```bash
mvn -q -pl hearing-domain/hearing-domain-common,hearing-domain/hearing-domain-event -am test
mvn -q -pl hearing-event/hearing-event-processor -am test
```
Expected: PASS, including all four new tests.

- [ ] **Step 9: Full build**

```bash
mvn -q clean install -DskipTests
```
Expected: SUCCESS. The RAML-driven code generation runs here, so a malformed schema edit surfaces at this step rather than earlier.

- [ ] **Step 10: Commit**

*Skipped when the controller has instructed no commits — otherwise:*

```bash
git add hearing-domain hearing-command hearing-event
git commit -m "feat: carry slot duration through to courtscheduler

A reservation on a duration-based session decrements available_duration by
the hearing's estimated minutes, and courtscheduler now rejects a duration-
based reservation that arrives without one. Optional end to end, so
slot-based picks are unaffected."
```

---

## Deliberately not in scope

**The command-handler-side schema is malformed and is not being fixed here.** `hearing-command/hearing-command-handler/src/raml/json/schema/hearing.command.book-provisional-hearing-slots.json` declares its `slots` item properties without a `properties` wrapper and puts `minItems` inside `items`, so it validates nothing about a slot. That is pre-existing, unrelated to this change, and fixing it could start rejecting payloads that currently pass. Raise it separately.

**`BookProvisionalHearingSlotsCommandHandler` needs no change.** It builds each slot with `convertToObject(slotsArray.getJsonObject(i), ProvisionalHearingSlotInfo.class)`, so a new field on that class is picked up by the deserialiser automatically. Confirm this holds when you run the tests rather than assuming it.

## Follow-on plans

| Plan | Repo | Depends on |
|---|---|---|
| 3 | `cpp-context-listing` — Crown resolves `bookingReference` as a booking id; expose the pre-share check | Plan 1 Tasks 6, 8 |
| 4 | `cpp-ui-hearing` — Crown and related-hearings reserve at pick; release on re-pick/delete/reset; the three clerk messages; the pre-share gate | Plans 2, 3 |
| 5 | `cpp.static-data.patches` — register the 01:00 purge job | Plan 1 Task 1 |

**Carried forward into Plans 3 and 4:** after a successful share a reservation-backed bookingId reports `live: false` from courtscheduler's `getBookingStatus`, indistinguishable from expiry. A pre-share gate must therefore establish separately that the draft has not already been shared, or re-sharing is blocked with a false expiry message. Documented in that method's Javadoc.
