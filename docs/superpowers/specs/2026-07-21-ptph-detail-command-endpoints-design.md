# Tier & List Type on a Seeding Hearing — Design

**Date:** 2026-07-21
**Service:** cpp-context-hearing
**Team:** Listing / Hearing

## Purpose

Capture, edit, finalise, and delete **Tier** and **List type** information for a
seeding hearing. This is the "Tier and list type" screen flow:

1. **Save** — a case worker selects a Tier (1–7) and, optionally, a list type
   (Type 1 fixed date, or Type 2 flexible date). Type 1 requires a free-text
   "key reason for a fixed date rather than a flexible date".
2. **Edit** — before finalisation, the saved values can be changed ("Change" links).
3. **Finalise** — locks the information. Requires both a Tier and a List type to
   be present. After finalisation the data cannot be changed.
4. **Delete** — removes all tier/list-type information, returning the hearing to a
   blank state. Allowed in any state.

The data belongs to a seeding hearing, identified by a hearing id.

## Decisions (agreed in brainstorming)

| Decision | Choice |
|---|---|
| Domain location | Extend the existing `HearingAggregate` (a seeding hearing IS a hearing, keyed by its id). Matches the `set-trial-type` precedent. |
| Tier representation | Simple domain enum `TIER_1`…`TIER_7`. No reference-data lookup. |
| List type | Enum `TYPE_1_FIXED` ("1F") / `TYPE_2_FLEXIBLE` ("2F"). Optional to save; `keyReason` required for `TYPE_1_FIXED` only. |
| Save vs edit | Single upsert command handles first save and subsequent edits. |
| Finalise precondition | Requires both Tier **and** List type to be already saved. |
| Post-finalise edits | Rejected — save/edit is refused once finalised (domain-enforced immutability). |
| Delete | Allowed in any state; clears tier, list type, reason and the finalised flag. |
| Query side | Add a GET query returning the current tier/list-type/reason/finalised. |
| View store (sub-choice A) | Dedicated table/entity `PtphDetail` (not nullable columns on `hearing`). Delete = row removal. |
| Command shape (sub-choice B) | Finalise/delete are empty-body media-type mappings on `POST /hearings/{hearingId}`, consistent with the rest of the command API. |

## Architecture

CQRS + Event Sourcing via the HMCTS Justice Services Framework. The full vertical
slice mirrors the existing `set-trial-type` feature.

```
POST /hearings/{hearingId}
  (media type → command name)
        │
        ▼
PtphDetailCommandHandler  @Handles(...)   ── command side
        │  aggregate(HearingAggregate, hearingId, ...)
        ▼
HearingAggregate → HearingPtphDetailDelegate
        │  emits domain events
        ▼
   [ PtphDetailSaved | PtphDetailFinalised | PtphDetailDeleted ]
        │  jms:topic:hearing.event
        ▼
PtphDetailEventListener  @Handles(...)     ── event side
        │  upsert / delete
        ▼
PtphDetail (JPA entity, view store)
        ▲
        │  read
GET /hearings/{hearingId}/ptph-detail    ── query side
```

## Domain model — `hearing-domain-common`

New enums:

- `Tier` — `TIER_1, TIER_2, TIER_3, TIER_4, TIER_5, TIER_6, TIER_7`.
- `ListType` — `TYPE_1_FIXED("1F")`, `TYPE_2_FLEXIBLE("2F")` (code accessor).

New command DTOs (converted from the envelope payload via `convertToObject`,
following the `TrialType` command class pattern):

- `SavePtphDetailCommand(hearingId, tier, listType, keyReason)`
- `FinalisePtphDetailCommand(hearingId)`
- `DeletePtphDetailCommand(hearingId)`

`hearingId` is supplied by the framework from the `{hearingId}` path parameter
(as with `set-trial-type`), so it does not appear in the request JSON schema.

## Domain events — `hearing-domain-event`

One event per state transition, each carrying `hearingId`:

- `PtphDetailSaved(hearingId, tier, listType, keyReason)` — `listType` and
  `keyReason` may be null (tier-only save).
- `PtphDetailFinalised(hearingId)`
- `PtphDetailDeleted(hearingId)`

Each is a plain immutable event object with `@JsonProperty`-annotated constructor,
following existing events such as `HearingTrialType` / `NextHearingStartDateRecorded`.

## Aggregate — `hearing-domain-aggregate`

New delegate `HearingPtphDetailDelegate` (mirrors `HearingTrialTypeDelegate`),
holding the aggregate momento. Wired into `HearingAggregate`:

- `Stream<Object> savePtphDetail(tier, listType, keyReason)`
  - **Reject** (throw domain/illegal-state) if current state is finalised.
  - Emit `PtphDetailSaved`.
- `Stream<Object> finalisePtphDetail()`
  - **Reject** unless both a tier and a list type are present in current state.
  - Emit `PtphDetailFinalised`. (Idempotent no-op or reject if already
    finalised — reject, to be explicit.)
- `Stream<Object> deletePtphDetail()`
  - Always permitted. Emit `PtphDetailDeleted`.

Apply methods on the delegate mutate the momento's held tier/list-type/reason/
finalised state so preconditions above can be evaluated on replay:

- `handlePtphDetailSaved` — set tier, listType, keyReason.
- `handlePtphDetailFinalised` — set finalised = true.
- `handlePtphDetailDeleted` — clear tier, listType, keyReason, finalised.

## Command handler — `hearing-command-handler`

Single class `PtphDetailCommandHandler extends AbstractCommandHandler`,
`@ServiceComponent(COMMAND_HANDLER)`, grouping three `@Handles` methods (pattern
of `ApplicantCounselCommandHandler`):

- `@Handles("hearing.command.save-ptph-detail")`
  - Convert payload → `SavePtphDetailCommand`.
  - Handler-level validation: if `listType == TYPE_1_FIXED`, `keyReason` must be
    non-blank; if `TYPE_2_FLEXIBLE`, `keyReason` must be absent/ignored.
  - `aggregate(HearingAggregate.class, hearingId, envelope, a -> a.savePtphDetail(...))`.
- `@Handles("hearing.command.finalise-ptph-detail")`
  - `aggregate(..., a -> a.finalisePtphDetail())`.
- `@Handles("hearing.command.delete-ptph-detail")`
  - `aggregate(..., a -> a.deletePtphDetail())`.

## Command API — `hearing-command-api`

RAML (`hearing-command-api.raml`), under the existing `POST /hearings/{hearingId}`
resource, add three `(mapping)` entries and their `application/...+json` bodies:

| Media type | Command name |
|---|---|
| `application/vnd.hearing.save-ptph-detail+json` | `hearing.save-ptph-detail` |
| `application/vnd.hearing.finalise-ptph-detail+json` | `hearing.finalise-ptph-detail` |
| `application/vnd.hearing.delete-ptph-detail+json` | `hearing.delete-ptph-detail` |

JSON schema + example files (`src/raml/json/` and `src/raml/json/schema/`):

- `hearing.save-ptph-detail.json` (schema): `tier` (enum, required),
  `listType` (enum, optional), `keyReason` (string, optional).
  Conditional rule: `keyReason` required when `listType == "TYPE_1_FIXED"`
  (expressed with a schema `oneOf`/`allOf`+`if` construct or enforced in-handler,
  matching how `set-trial-type` expresses its `oneOf`). `additionalProperties: false`.
- `hearing.finalise-ptph-detail.json` (schema): empty object `{}`,
  `additionalProperties: false`.
- `hearing.delete-ptph-detail.json` (schema): empty object `{}`,
  `additionalProperties: false`.

Mirror the schema/example files in `hearing-command-handler/src/raml/json/`
(and `.../schema/`) using the `hearing.command.*` naming, per the repo's two-layer
RAML convention.

## Query side

RAML (`hearing-query-api.raml`), new resource `GET /hearings/{hearingId}/ptph-detail`:

- Response media type `application/vnd.hearing.get-ptph-detail+json` → 200.
- Response body `{ tier, listType, keyReason, finalised }`; return empty payload /
  404 when nothing is saved for the hearing.

New query view method (in the relevant `*QueryView` on `hearing-query-view`) reading
the `PtphDetail` entity via its repository, plus response schema + example
files. Access is subject to the existing `validateIfUserHasAccessToHearing()` guard.

## View store — `hearing-viewstore-persistence` + `hearing-viewstore-liquibase`

New JPA entity `PtphDetail`:

| Column | Type | Notes |
|---|---|---|
| `hearing_id` | UUID | PK / FK to hearing |
| `tier` | varchar | enum name, nullable until saved |
| `list_type` | varchar | enum name, nullable |
| `key_reason` | text | nullable; only for `TYPE_1_FIXED` |
| `finalised` | boolean | default false |

New repository (JPA) with find-by-hearingId, upsert, and delete-by-hearingId.
Liquibase changeset in `hearing-viewstore-liquibase` creating table
`ptph_detail`.

Event listener `PtphDetailEventListener` (`hearing-event-listener`),
`@ServiceComponent(EVENT_LISTENER)`:

- `@Handles("hearing.events.ptph-detail-saved")` → upsert row.
- `@Handles("hearing.events.ptph-detail-finalised")` → set `finalised = true`.
- `@Handles("hearing.events.ptph-detail-deleted")` → delete row.

(Exact event public-name strings to follow the repo's existing event naming; listener
schema files added under `hearing-event-listener/src/yaml/json/` as per convention.)

## Access control

Add access-control entries for the three new command actions
(`hearing.save-ptph-detail`, `hearing.finalise-ptph-detail`,
`hearing.delete-ptph-detail`) and the new query, matching whatever
mechanism `HearingCommandApiAccessControlTest` asserts against.

## Testing

Following existing `*Test` conventions (JUnit + Mockito):

- `PtphDetailCommandHandlerTest` — each `@Handles` method; save validation
  (Type 1 requires reason; Type 2 forbids it).
- `HearingPtphDetailDelegateTest` / additions to `HearingAggregateTest` —
  save emits event; save rejected when finalised; finalise rejected without both
  tier and list type; finalise emits event; delete emits event and clears state.
- `PtphDetailEventListenerTest` — upsert / finalise / delete against the view store.
- Query view test — returns saved values; empty when none.
- Command-API test additions — media-type mappings resolve to command names.
- (Optional) an integration test `PtphDetailIT` if the team wants end-to-end
  coverage, following `ChangeNextHearingDateIT`.

## State machine summary

```
            save (tier[, listType, keyReason])
   (none) ───────────────────────────────────▶ (draft: tier ± listType)
      ▲                                              │  save (edit) ↺ (overwrite)
      │ delete                                       │
      │                              finalise  ──────┤ (requires tier AND listType)
      │                                              ▼
      └──────────────── delete ──────────────── (finalised: immutable;
                                                  save/edit rejected)
```

## Out of scope

- The Angular UI screens (this repo is the backend service only).
- Reference-data management of tiers (tiers are a fixed enum).
- Publishing tier/list-type as a public event (none requested; can be added later
  if a downstream consumer needs it).
