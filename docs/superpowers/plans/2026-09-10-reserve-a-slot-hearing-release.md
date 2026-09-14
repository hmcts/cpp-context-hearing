# Reserve a Slot — cpp-context-hearing release command (NEW-12a)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Give the browser a way to release a reservation, by adding a hearing command that proxies courtscheduler's existing `DELETE /sessions/{bookingId}`.

**Architecture:** courtscheduler already releases correctly — `DELETE /sessions/{hearingId}` → `SlotsRemoveService.remove` → `releaseOldAllocatedListings`, the full three-step release that restores capacity. It accepts any string, so a `bookingId` works. But nothing between the browser and that endpoint can reach it: listing does not proxy it and hearing has no release command. This plan adds the missing link, mirroring the existing `book-provisional-hearing-slots` path exactly: command → aggregate → event → processor → HTTP.

**Tech Stack:** Java 17, **Maven**, CDI, HMCTS Justice Services Framework (RAML-first, event-sourced), JUnit, Mockito.

**Spec:** https://claude.ai/code/artifact/90e6b0eb-dc8a-4697-a8d4-8d9086d283d6
**Ticket:** NEW-12a in `cpp-context-hearing/docs/reserve-a-slot-jira.md`

**Repo:** `cpp-context-hearing`, branch `team/ccsph2`

## Why this exists

With a same-day hold and no release, a clerk who tries three sessions before settling holds all three for the rest of the sitting day. The nightly purge is the backstop, not the mechanism.

## Design decisions already made

**Fire-and-forget, no public event.** The book path emits `public.hearing.hearing-slots-provisionally-booked` because the UI must wait for the minted `bookingId`. Release returns nothing the caller needs, and there is no ordering hazard: a re-pick reserves under a *new* `bookingId`, so the old release and the new reserve are independent. The UI fires it and proceeds. If a release is lost, the 01:00 purge reclaims the capacity — the accepted backstop. Do not add a public event.

**Second media type on the existing resource, not a new path.** `POST /hearings/{hearingId}/hearing-slots` gains `application/vnd.hearing.release-provisional-hearing-slots+json`. This is the framework's convention — commands are dispatched by media type, and `/hearings/{hearingId}` already carries thirty of them.

**New handler and processor classes, not additions to the Book ones.** `BookProvisionalHearingSlotsCommandHandler` and `BookProvisionalHearingSlotsProcessor` are named for what they do; hanging a release off them would make both names lies.

## Global Constraints

- Build tool is **Maven**. Never Gradle.
- The event **must** be registered in `subscriptions-descriptor.yaml` with a matching schema file. An event published without that entry is never delivered, and nothing fails loudly — the processor simply never runs.
- The release must be tolerant: releasing a `bookingId` with no reservation is a **no-op, not an error**. Legacy magistrates drafts have no reservation, and courtscheduler already treats absence as a no-op.
- Do not change the existing `hearing.book-provisional-hearing-slots` command, its schema, its event, or its processor.
- `hearingId` is the aggregate key; `bookingId` is what gets released. Both go in the payload.

**Build and test commands**

```bash
mvn -q -pl hearing-domain/hearing-domain-event,hearing-domain/hearing-domain-aggregate -am test
mvn -q -pl hearing-command/hearing-command-handler -am test
mvn -q -pl hearing-event/hearing-event-processor -am test
mvn -q clean install -DskipTests
```
The last one runs RAML-driven code generation — a malformed schema surfaces there and nowhere earlier. Run it.

---

### Task 1: Add the release command, end to end

One task: the RAML that admits it, the command that carries it, the event that records it, the subscription that delivers it, and the HTTP call that performs it. Splitting would leave states where a command exists with no target, or a client method with no caller — neither of which a reviewer could accept or reject on its own.

**Files:**
- Modify: `hearing-command/hearing-command-api/src/raml/hearing-command-api.raml` (the `/hearings/{hearingId}/hearing-slots` post, ~line 382)
- Create: `hearing-command/hearing-command-api/src/raml/json/schema/hearing.release-provisional-hearing-slots.json`
- Create: `hearing-command/hearing-command-api/src/raml/json/hearing.release-provisional-hearing-slots.json`
- Modify: `hearing-command/hearing-command-api/src/main/java/uk/gov/moj/cpp/hearing/command/api/HearingCommandApi.java`
- Create: `hearing-command/hearing-command-handler/src/main/java/uk/gov/moj/cpp/hearing/command/handler/ReleaseProvisionalHearingSlotsCommandHandler.java`
- Create: `hearing-command/hearing-command-handler/src/raml/json/schema/hearing.command.release-provisional-hearing-slots.json`
- Create: `hearing-domain/hearing-domain-event/src/main/java/uk/gov/moj/cpp/hearing/domain/event/ReleaseProvisionalHearingSlots.java`
- Modify: `hearing-domain/hearing-domain-aggregate/src/main/java/uk/gov/moj/cpp/hearing/domain/aggregate/HearingAggregate.java`
- Modify: `hearing-event/hearing-event-processor/src/yaml/subscriptions-descriptor.yaml`
- Create: `hearing-event/hearing-event-processor/src/yaml/json/schema/hearing.event.release-provisional-hearing-slots.json`
- Create: `hearing-event/hearing-event-processor/src/main/java/uk/gov/moj/cpp/hearing/event/ReleaseProvisionalHearingSlotsProcessor.java`
- Modify: `hearing-event/hearing-event-processor/src/main/java/uk/gov/moj/cpp/hearing/event/service/ProvisionalBookingService.java`
- Test: `hearing-event/hearing-event-processor/src/test/java/uk/gov/moj/cpp/hearing/event/ReleaseProvisionalHearingSlotsProcessorTest.java`
- Test: `hearing-command/hearing-command-handler/src/test/java/uk/gov/moj/cpp/hearing/command/handler/ReleaseProvisionalHearingSlotsCommandHandlerTest.java`

**Interfaces:**
- Consumes: courtscheduler `DELETE {courtscheduler.base.url}/sessions/{bookingId}` → 202. Already exists, already performs the full three-step release, already a no-op when nothing matches.
- Produces, for NEW-12b (the UI ticket) to call:
  - `POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}/hearing-slots`
  - media type `application/vnd.hearing.release-provisional-hearing-slots+json`
  - body `{ "bookingId": "<uuid>" }`
  - response `202 Accepted`, no body, no public event

**Read the Book path first.** Every piece here mirrors one: `HearingCommandApi.bookProvisionalHearingSlots` (~line 263), `BookProvisionalHearingSlotsCommandHandler`, `HearingAggregate.bookProvisionalHearingSlots` (~line 1260), `BookProvisionalHearingSlots`, `BookProvisionalHearingSlotsProcessor`, and `ProvisionalBookingService.bookSlots`. Follow their conventions rather than inventing new ones.

- [ ] **Step 1: Write the failing processor test**

Create `ReleaseProvisionalHearingSlotsProcessorTest`, modelled on `BookProvisionalHearingSlotsProcessorTest` (read it — reuse its envelope construction and mock style):

```java
    @Test
    public void shouldReleaseTheBookingAgainstCourtScheduler() {
        final UUID hearingId = randomUUID();
        final String bookingId = randomUUID().toString();

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("bookingId", bookingId)
                .build();
        final JsonEnvelope event = JsonEnvelope.envelopeFrom(
                metadataWithRandomUUID("hearing.event.release-provisional-hearing-slots"), payload);

        processor.handleReleaseProvisionalHearingSlots(event);

        verify(provisionalBookingService, times(1)).releaseSlots(bookingId);
    }
```

- [ ] **Step 2: Write the failing command-handler test**

Create `ReleaseProvisionalHearingSlotsCommandHandlerTest`, modelled on the Book handler's test:

```java
    @Test
    public void shouldRaiseReleaseEventForTheBooking() throws EventStreamException {
        final UUID hearingId = randomUUID();
        final String bookingId = randomUUID().toString();

        final JsonObject payload = createObjectBuilder()
                .add("hearingId", hearingId.toString())
                .add("bookingId", bookingId)
                .build();

        handler.releaseProvisionalHearingSlots(envelopeFrom(
                metadataWithRandomUUID("hearing.command.release-provisional-hearing-slots"), payload));

        // assert a ReleaseProvisionalHearingSlots event reached the stream, carrying both ids
    }
```

Complete the assertion using whatever mechanism the Book handler's test uses to capture the emitted event — copy it rather than inventing one.

- [ ] **Step 3: Run both to verify they fail**

```bash
mvn -q -pl hearing-event/hearing-event-processor -am test -Dtest=ReleaseProvisionalHearingSlotsProcessorTest
mvn -q -pl hearing-command/hearing-command-handler -am test -Dtest=ReleaseProvisionalHearingSlotsCommandHandlerTest
```
Expected: compilation failure — none of the classes exist yet.

- [ ] **Step 4: Create the event**

`ReleaseProvisionalHearingSlots` in `hearing-domain-event`, following `BookProvisionalHearingSlots`'s annotation and `@JsonCreator` conventions but far simpler — two fields, no slot-list reconstruction:

```java
@Event("hearing.event.release-provisional-hearing-slots")
public class ReleaseProvisionalHearingSlots implements Serializable {

    private final UUID hearingId;
    private final String bookingId;

    @JsonCreator
    public ReleaseProvisionalHearingSlots(@JsonProperty("hearingId") final UUID hearingId,
                                          @JsonProperty("bookingId") final String bookingId) {
        this.hearingId = hearingId;
        this.bookingId = bookingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getBookingId() {
        return bookingId;
    }
}
```

Add a builder only if the aggregate's conventions require one — `BookProvisionalHearingSlots` has one, so match whichever the aggregate call site reads more naturally.

- [ ] **Step 5: Add the aggregate method**

Beside `bookProvisionalHearingSlots` in `HearingAggregate`:

```java
    public Stream<Object> releaseProvisionalHearingSlots(final UUID hearingId, final String bookingId) {
        return apply(Stream.of(new ReleaseProvisionalHearingSlots(hearingId, bookingId)));
    }
```

**Note:** `BookProvisionalHearingSlots` is deliberately **not** registered in the aggregate's `when(...)` match block — it mutates no aggregate state. Follow that precedent and do not register the release event either. Confirm on replay that an unmatched event is tolerated, as it evidently is for Book today.

- [ ] **Step 6: Add the command handler**

```java
@ServiceComponent(COMMAND_HANDLER)
public class ReleaseProvisionalHearingSlotsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseProvisionalHearingSlotsCommandHandler.class.getName());

    @Handles("hearing.command.release-provisional-hearing-slots")
    public void releaseProvisionalHearingSlots(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.release-provisional-hearing-slots received {}", envelope.toObfuscatedDebugString());
        }
        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString("hearingId"));
        final String bookingId = envelope.payloadAsJsonObject().getString("bookingId");

        aggregate(HearingAggregate.class, hearingId, envelope,
                hearingAggregate -> hearingAggregate.releaseProvisionalHearingSlots(hearingId, bookingId));
    }
}
```

- [ ] **Step 7: Forward the command from the API**

Beside `bookProvisionalHearingSlots` in `HearingCommandApi`:

```java
    @Handles("hearing.release-provisional-hearing-slots")
    public void releaseProvisionalHearingSlots(final JsonEnvelope envelope) {
        sendEnvelopeWithName(envelope, "hearing.command.release-provisional-hearing-slots");
    }
```

- [ ] **Step 8: Add the DELETE to the HTTP client**

In `hearing.event.service.ProvisionalBookingService`, beside `bookSlots`:

```java
    private static final String SESSIONS = "/sessions/";

    /**
     * Releases the hold taken at slot-pick time. courtscheduler's DELETE performs the full
     * three-step release, restoring the session's capacity, and is a no-op when the booking has
     * no reservation — which is the normal case for a pre-go-live magistrates draft.
     */
    public void releaseSlots(final String bookingId) {
        final UUID systemUserId = systemUserProvider.getContextSystemUserId()
                .orElseThrow(() -> new IllegalStateException("contextSystemUserId missing!!!"));

        try {
            final HttpDelete httpDelete = new HttpDelete(new URL(baseUri + SESSIONS + bookingId).toString());
            httpDelete.addHeader(CJS_CPP_UID, systemUserId.toString());

            final HttpResponse httpResponse = HttpClientBuilder.create().build().execute(httpDelete);
            final int status = httpResponse.getStatusLine().getStatusCode();
            if (status == Response.Status.ACCEPTED.getStatusCode() || status == Response.Status.OK.getStatusCode()) {
                LOGGER.info("released provisional booking {}", bookingId);
            } else {
                LOGGER.error("release of provisional booking {} failed with status {}", bookingId, status);
            }
        } catch (IOException ex) {
            LOGGER.error("release of provisional booking {} failed", bookingId, ex);
        }
    }
```

It swallows failures deliberately: a lost release costs capacity until 01:00, which the purge reclaims, whereas propagating would fail a command the clerk cannot act on. Import `org.apache.http.client.methods.HttpDelete`.

- [ ] **Step 9: Add the processor**

```java
@ServiceComponent(EVENT_PROCESSOR)
public class ReleaseProvisionalHearingSlotsProcessor {

    private final ProvisionalBookingService provisionalBookingService;

    @Inject
    public ReleaseProvisionalHearingSlotsProcessor(final ProvisionalBookingService provisionalBookingService) {
        this.provisionalBookingService = provisionalBookingService;
    }

    @Handles("hearing.event.release-provisional-hearing-slots")
    public void handleReleaseProvisionalHearingSlots(final JsonEnvelope event) {
        final String bookingId = event.payloadAsJsonObject().getString("bookingId");
        provisionalBookingService.releaseSlots(bookingId);
    }
}
```

- [ ] **Step 10: Register the event for delivery**

This is the step that silently breaks everything if missed. In `hearing-event/hearing-event-processor/src/yaml/subscriptions-descriptor.yaml`, beside the Book entry:

```yaml
        # Hearing Release Provisionally Booked Slots
        - name: hearing.event.release-provisional-hearing-slots
          schema_uri: http://justice.gov.uk/json/schemas/hearing/hearing.event.release-provisional-hearing-slots.json
```

and create the matching schema at `hearing-event/hearing-event-processor/src/yaml/json/schema/hearing.event.release-provisional-hearing-slots.json`:

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/json/schemas/hearing/hearing.event.release-provisional-hearing-slots.json",
  "type": "object",
  "properties": {
    "hearingId": {
      "$ref": "http://justice.gov.uk/domain/core/common/definitions.json#/definitions/uuid"
    },
    "bookingId": {
      "type": "string"
    }
  },
  "required": ["hearingId", "bookingId"]
}
```

- [ ] **Step 11: Add the RAML media type and its schemas**

In `hearing-command-api.raml`, under the existing `/hearings/{hearingId}/hearing-slots` `post`, add a second mapping and body entry alongside the Book one:

```yaml
        (mapping):
            requestType: application/vnd.hearing.release-provisional-hearing-slots+json
            name: hearing.release-provisional-hearing-slots
```

```yaml
        application/vnd.hearing.release-provisional-hearing-slots+json:
            example:
             !include json/hearing.release-provisional-hearing-slots.json
            schema:
             !include json/schema/hearing.release-provisional-hearing-slots.json
```

Schema (`json/schema/hearing.release-provisional-hearing-slots.json`) — note `hearingId` comes from the URI, so the body carries only `bookingId`:

```json
{
  "$schema": "http://json-schema.org/draft-04/schema#",
  "id": "http://justice.gov.uk/json/schemas/hearing/hearing.release-provisional-hearing-slots.json",
  "type": "object",
  "properties": {
    "bookingId": {
      "description": "The booking whose reservation is to be released.",
      "type": "string"
    }
  },
  "required": ["bookingId"],
  "additionalProperties": false
}
```

Example (`json/hearing.release-provisional-hearing-slots.json`):

```json
{
  "bookingId": "4e29c1fa-7d3b-4f21-9c88-1a2b3c4d7ac1"
}
```

Add the command-handler-side schema at `hearing-command-handler/src/raml/json/schema/hearing.command.release-provisional-hearing-slots.json` with the same two properties, this time including `hearingId` — match the shape of the Book command's handler-side schema, which the framework populates from the URI parameter.

- [ ] **Step 12: Run the tests**

```bash
mvn -q -pl hearing-domain/hearing-domain-event,hearing-domain/hearing-domain-aggregate -am test
mvn -q -pl hearing-command/hearing-command-handler -am test
mvn -q -pl hearing-event/hearing-event-processor -am test
```
Expected: PASS, including both new tests and every pre-existing test in those modules.

- [ ] **Step 13: Full build**

```bash
mvn -q clean install -DskipTests
```
Expected: SUCCESS. RAML code generation runs here; a malformed schema or a mis-nested media type surfaces at this step.

- [ ] **Step 14: Commit**

*Skipped when the controller has instructed no commits — otherwise:*

```bash
git add hearing-command hearing-domain hearing-event
git commit -m "feat: add a command to release a provisional booking

courtscheduler could already release a hold, but nothing between the browser
and that endpoint could reach it. Mirrors the book-provisional path:
command, aggregate, event, processor, HTTP DELETE. Fire and forget — a lost
release is reclaimed by the nightly purge."
```

---

## Deliberately not in scope

- **The UI wiring (NEW-12b)** — a separate ticket, and blocked on private-registry access in this environment.
- **A public event.** See the design decision above: nothing the caller needs comes back, and there is no ordering hazard.
- **Any change to the Book path.** It is the reference, not the subject.
- **Making the release synchronous or failure-propagating.** A lost release costs capacity until 01:00, which the purge reclaims; failing the clerk's action would be worse.
