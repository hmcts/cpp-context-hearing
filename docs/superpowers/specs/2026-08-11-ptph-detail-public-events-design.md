# PTPH detail — public events

**Date:** 2026-08-11
**Service:** `cpp-context-hearing`
**Related:** LPT-2400–2404 (the command/query slice, implemented), LPT-2405 (listing inherits the values)

## Problem

The three PTPH commands — save, finalise and delete — emit private domain events that stay
inside the hearing context. A caller that issues one of these commands gets `202 Accepted`
immediately, before the aggregate has run, so it has no way to learn whether the change
actually took effect short of polling `GET /hearings/{hearingId}/ptph-detail`.

Publishing a public event per command closes that loop, following the
command → private event → public event pattern already used throughout this context.

## Scope

Three new public events, published by a new event processor, mirroring the three existing
private events one-for-one.

Out of scope:

- No change to the commands, the aggregate, the private events or the view store.
- No enrichment of payloads (see Decisions).
- No new consumer. This publishes the events; whether listing subscribes to them instead of
  querying is a separate decision for LPT-2405.

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Event count | One public event per private event (three) | Mirrors the private events and the established pattern; each command's outcome is separately observable |
| Payload | Verbatim pass-through of the private event | Matches every existing processor. No transformation to keep in sync, no new failure mode |
| Processor class | One `PtphDetailEventProcessor` with three `@Handles` | Grouping related events in one processor is idiomatic here (`HearingEventProcessor` handles 18) |
| Schema location | In-repo, `hearing-event-processor/src/yaml/json/schema/` | Public event schemas live in this repo, so no `criminal-court-public-model` release is needed |
| Schema id host | `http://cpp.moj.gov.uk/hearing/json/schema/event/…` | The dominant convention: 80 of 103 existing public schemas use it |
| Error handling | None beyond the framework | Every existing processor is a bare re-envelope with no `try`/`catch`; failures become redelivery and ultimately the DLQ |

### Why not enrich the finalised event

`hearing.ptph-detail-finalised` carries only `hearingId`, so a consumer learning that a record
became authoritative must query for the values. Enriching it was considered and rejected for
now: the values would have to come either from the aggregate — changing `PtphDetailFinalised`,
its delegate, its listener and their tests — or from a view-store read inside the processor,
which races the listener writing that same row. Neither cost is justified until a consumer
actually needs it. If listing later prefers reacting to the event over querying, that is a
contained follow-up.

## Current pattern

```
command API ──▶ handler ──▶ aggregate ──▶ private event ──▶ event listener ──▶ view store
                                                │
                                                └──▶ event processor ──▶ public event
```

`DefenceCounselEventProcessor` is the reference implementation:

```java
@ServiceComponent(EVENT_PROCESSOR)
public class DefenceCounselEventProcessor {
    @Handles("hearing.defence-counsel-added")
    public void publishPublicDefenceCounselAddedEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper
                .withMetadataFrom(event, "public.hearing.defence-counsel-added")
                .apply(event.payloadAsJsonObject()));
    }
}
```

## Events

| Private event | New public event | Payload |
|---|---|---|
| `hearing.ptph-detail-saved` | `public.hearing.ptph-detail-saved` | `hearingId`, `tier`, `listType`, `keyReason` |
| `hearing.ptph-detail-finalised` | `public.hearing.ptph-detail-finalised` | `hearingId` |
| `hearing.ptph-detail-deleted` | `public.hearing.ptph-detail-deleted` | `hearingId` |

Only `hearingId` is required on all three; `tier`, `listType` and `keyReason` are optional on
the saved event, matching the private schema, because a tier-only save is valid and
`keyReason` is discarded for a flexible list type.

## Components

### `PtphDetailEventProcessor` (new)

`hearing-event/hearing-event-processor/src/main/java/uk/gov/moj/cpp/hearing/event/PtphDetailEventProcessor.java`

Three `@Handles` methods, one per private event, each a single re-envelope-and-send following
the reference implementation above.

### Schemas (new)

Public, in `hearing-event-processor/src/yaml/json/schema/` with examples alongside in
`src/yaml/json/`:

- `public.hearing.ptph-detail-saved.json`
- `public.hearing.ptph-detail-finalised.json`
- `public.hearing.ptph-detail-deleted.json`

All three are `additionalProperties: false` with `hearingId` required, `$ref`-ing
`http://justice.gov.uk/core/courts/courtsDefinitions.json#/definitions/uuid` — the uuid
definition used by 93 of the module's public schemas, against 39 for the alternative. Note
this differs from the private schemas, which use
`http://justice.gov.uk/domain/core/common/definitions.json#/definitions/uuid`; each side keeps
its own local convention.

Private copies, same directories: the event-processor module keeps its own copies of the
private schemas it subscribes to (110 of them today), so the three
`hearing.ptph-detail-*.json` schemas must be copied across from `hearing-event-listener`.
Their ids stay `http://justice.gov.uk/hearing/courts/hearing.ptph-detail-*.json`.

### Descriptors (modified)

- `hearing-event-processor/src/yaml/subscriptions-descriptor.yaml` — subscribe to the three
  private events. They are currently subscribed only by the event listener.
- `hearing-event-processor/src/yaml/public-publications-descriptor.yaml` — declare the three
  public events with their schema uris.

## Testing

| Test | Asserts |
|---|---|
| `PtphDetailEventProcessorTest` (new) | For each of the three events: the sender receives an envelope named `public.hearing.ptph-detail-*` carrying the inbound payload unchanged. Mirrors `DefenceCounselEventProcessorTest` |
| `PtphDetailIT` (extended) | Each command results in its public event reaching the public topic, using the `listenFor(...).withFilter(...)` pattern from `DefenceCounselIT` |

The integration assertions wrap the existing command calls rather than adding new scenarios:

```java
try (EventListener publicSaved = listenFor("public.hearing.ptph-detail-saved")
        .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))))) {
    savePtphDetail(getRequestSpec(), hearingId, ptphDetail(...));
    publicSaved.waitFor();
}
```

## Build note

The RAML/JSON generation in this repo does not reliably regenerate into a dirty `target`.
Build with `mvn clean install`; a non-clean build silently produces artefacts missing newly
declared media types and events, which surfaces at runtime as `415` on the commands rather
than as a build failure.

## Definition of done

- Each of the three commands results in its public event on the public topic.
- Public payloads are byte-identical to the private ones.
- No change to commands, aggregate, private events or view store.
- `mvn clean install` green, and `PtphDetailIT` green including the new public-event assertions.
