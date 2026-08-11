# Jira Tickets — List Type & Tier

Programme: Crime Common Platform (CPP) — Modern by Default
Team: Listing / Hearing
Service: `cpp-context-hearing` (hearing tickets) + listing services (listing tickets)
Work delivering LPT-2400–2404: intended for `feature/ptph-detail`, but currently
**uncommitted in the `cpp-context-hearing` working tree with `main` checked out** — the
`feature/ptph-detail` branch itself carries no PTPH commits yet.
Status last verified against the code: **2026-07-30** (endpoints, payloads and headers
below read off the RAML, JSON schemas/examples, handlers and Drools rules in the working
tree)

| Key | Summary | Type | Status | Assignee |
|-----|---------|------|--------|----------|
| LPT-2400 | [BE] [hearing] create domain objects and enums for capturing list type and tier | Story | Done | Unassigned |
| LPT-2401 | [BE] [hearing] create endpoint for capturing list type and tier | Story | Done | Unassigned |
| LPT-2402 | [BE] [hearing] create endpoint for deleting list type and tier | Story | Done | Unassigned |
| LPT-2403 | [BE] [hearing] create endpoint for editing list type and tier | Story | Done | Unassigned |
| LPT-2404 | [BE] [hearing] create endpoint for finalizing list type and tier | Story | Done | Unassigned |
| LPT-2405 | [BE] [listing] when next hearing is being created from a seeding hearing, retrieve tier/listType info from hearing context and store it in the listing payload | Story | New | Unassigned |
| LPT-2406 | [BE] [listing] Endpoints used by court calendar need to return the list type and tier info | Story | New ¹ | Unassigned |

> **Delivery note:** LPT-2400 through LPT-2404 are **implemented** in the hearing
> context — the full CQRS slice exists for save / finalise / delete (command API →
> handler → aggregate delegate → domain events → event listener → view store), plus
> access-control rules, RAML/JSON schemas and unit tests.
> LPT-2405 and LPT-2406 are listing-side follow-ups **not** started here.
>
> ¹ LPT-2406 stays open: its hearing-context enabler (`GET
> /hearings/{hearingId}/ptph-detail` → `hearing.get-ptph-detail`) is done, but the
> court-calendar-facing listing endpoints have not been changed.

**Implemented artefacts (verified 2026-07-29):**

| Layer | Artefact |
|-------|----------|
| Domain types | `hearing-domain-common`: `Tier`, `ListType`, `SavePtphDetailCommand` |
| Domain events | `hearing-domain-event`: `PtphDetailSaved`, `PtphDetailFinalised`, `PtphDetailDeleted` |
| Aggregate | `HearingPtphDetailDelegate` + wiring in `HearingAggregate` / `HearingAggregateMomento` |
| Command API | `HearingCommandApi.savePtphDetail` / `finalisePtphDetail` / `deletePtphDetail`; 3 media types on `POST /hearings/{hearingId}` |
| Command handler | `PtphDetailCommandHandler` (`hearing.command.save/finalise/delete-ptph-detail`) |
| Event listener | `PtphDetailEventListener` (upsert on saved, flag on finalised, row removal on deleted) |
| View store | `PtphDetail` entity, `PtphDetailRepository`, Liquibase `137-create-ptph-detail.xml` |
| Query side | `GET /hearings/{hearingId}/ptph-detail` → `hearing.get-ptph-detail`, `HearingQueryView.getPtphDetail`, `PtphDetailResponse` |
| Access control | Command + query Drools rules mirroring `hearing.set-trial-type` groups |

**Endpoint summary:**

| Ticket | Method | Path | Media type (`Content-Type` / `Accept`) | Response |
|--------|--------|------|----------------------------------------|----------|
| LPT-2401 / LPT-2403 | POST | `/hearing-command-api/command/api/rest/hearing/hearings/{hearingId}` | `application/vnd.hearing.save-ptph-detail+json` | 202 Accepted, empty body |
| LPT-2404 | POST | `/hearing-command-api/command/api/rest/hearing/hearings/{hearingId}` | `application/vnd.hearing.finalise-ptph-detail+json` | 202 Accepted, empty body |
| LPT-2402 | POST | `/hearing-command-api/command/api/rest/hearing/hearings/{hearingId}` | `application/vnd.hearing.delete-ptph-detail+json` | 202 Accepted, empty body |
| LPT-2406 (enabler) | GET | `/hearing-query-api/query/api/rest/hearing/hearings/{hearingId}/ptph-detail` | `application/vnd.hearing.get-ptph-detail+json` | 200 OK + JSON body |

**Common headers.** All four endpoints go through the standard framework/API-gateway
header contract — the media type carries the operation (there is no separate action
name header), and `CJSCPPUID` carries the logged-in user id used by the Drools
access-control rules (`uk.gov.justice.services.common.http.HeaderConstants.USER_ID`).

| Header | Commands (POST) | Query (GET) |
|--------|-----------------|-------------|
| `Content-Type` | the `application/vnd.hearing.<name>+json` media type | n/a |
| `Accept` | `application/json` | `application/vnd.hearing.get-ptph-detail+json` |
| `CJSCPPUID` | user id (UUID) — required, drives access control | same |

All four are granted to the same groups as `hearing.set-trial-type`:
`Listing Officers`, `Court Clerks`, `Legal Advisers`, `Judiciary`, `Court Associate`,
`Deputies`, `DJMC`, `Judge`, `Recorders`, `Court Administrators`.

> **Schema note (worth a follow-up).** `hearing.save-ptph-detail` requires `hearingId`
> **in the request body** as well as in the path (`"required": ["hearingId","tier"]`,
> `additionalProperties: false`). `hearing.set-trial-type` — the precedent this slice
> follows — omits `hearingId` from the command-API schema and relies on the framework
> injecting the path parameter into the payload. `finalise`/`delete` follow the
> `set-trial-type` precedent (empty body). So the save call currently must repeat the
> id, and the finalise/delete calls must **not** send it (`additionalProperties: false`
> rejects it).

---

## LPT-2400 — [BE] [hearing] create domain objects and enums for capturing list type and tier

**Type:** Story · **Status:** Done · **Assignee:** Unassigned

**Description:** Create the domain value types that model tier and list type so they can
be captured, stored and emitted as events on a hearing.

**Scope of work:**
- `Tier` enum (`TIER_1`…`TIER_7`) in `hearing-domain-common`.
- `ListType` enum: `TYPE_1_FIXED` ("1F") / `TYPE_2_FLEXIBLE` ("2F").
- `SavePtphDetailCommand` command DTO.
- Domain events `PtphDetailSaved` / `PtphDetailFinalised` / `PtphDetailDeleted` (`hearing-domain-event`), `@Event("hearing.ptph-detail-saved|-finalised|-deleted")`, tier/list type carried as String.

**Acceptance criteria:**
- Enums expose the seven tiers and the two list types with their codes.
- Events are serialisable (`@JsonCreator`/`@JsonProperty`) and carry `hearingId` (+ tier/listType/keyReason on the saved event).
- Unit test covers event construction.

**Status:** ✅ **Done** — `Tier`, `ListType` and `SavePtphDetailCommand` in
`hearing-domain-common`; the three events in `hearing-domain-event` with
`PtphDetailSavedTest` covering construction.

### Endpoint detail

No HTTP endpoint — this ticket delivers domain types only. The artefacts it produces
appear on the wire as **domain events** on `jms:topic:hearing.event`, consumed by
`PtphDetailEventListener` (subscription `subscriptions-descriptor.yaml`).

| Event name | Media type | Payload |
|------------|-----------|---------|
| `hearing.ptph-detail-saved` | `application/vnd.hearing.ptph-detail-saved+json` | `hearingId`, `tier`, `listType`, `keyReason` |
| `hearing.ptph-detail-finalised` | `application/vnd.hearing.ptph-detail-finalised+json` | `hearingId` |
| `hearing.ptph-detail-deleted` | `application/vnd.hearing.ptph-detail-deleted+json` | `hearingId` |

**Sample event payloads** (`hearing-event-listener/src/yaml/json/`):

```json
// hearing.ptph-detail-saved
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_2",
  "listType": "TYPE_1_FIXED",
  "keyReason": "reason"
}
```

```json
// hearing.ptph-detail-finalised  /  hearing.ptph-detail-deleted
{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" }
```

**Enum values carried as String:** `tier` ∈ `TIER_1 … TIER_7`;
`listType` ∈ `TYPE_1_FIXED` ("1F") / `TYPE_2_FLEXIBLE` ("2F").
`tier`, `listType` and `keyReason` are all nullable on the saved event.

---

## LPT-2401 — [BE] [hearing] create endpoint for capturing list type and tier

**Type:** Story · **Status:** Done · **Assignee:** Unassigned

**Description:** Provide the command endpoint that captures a tier and (optional) list
type against a seeding hearing, persisting them to the view store.

**Scope of work (full CQRS slice for "save"):**
- `POST /hearings/{hearingId}` media type `application/vnd.hearing.save-ptph-detail+json` → `hearing.save-ptph-detail` → handler `hearing.command.save-ptph-detail`.
- `PtphDetailCommandHandler.save`, `HearingAggregate` delegate emits `PtphDetailSaved`, `PtphDetailEventListener` upserts the `ha_ptph_detail` row.
- RAML + JSON schema/example on both command-API and handler layers.

**Acceptance criteria:**
- Request accepts `{ tier, listType?, keyReason? }`; `tier` required.
- `TYPE_1_FIXED` requires a non-blank `keyReason`; `TYPE_2_FLEXIBLE` must not carry one.
- Returns 202; the values are persisted and readable via LPT-2406's query.
- Access granted to the same roles as `hearing.set-trial-type`.

**Status:** ✅ **Done** — media type + RAML mapping wired,
`HearingCommandApi.savePtphDetail` → `PtphDetailCommandHandler.savePtphDetail` →
`HearingPtphDetailDelegate.savePtphDetail` → `PtphDetailEventListener` upsert of the
`ha_ptph_detail` row. `tier` is required by the schema; the Type-1 key-reason rule is
enforced in `PtphDetailCommandHandler.resolveKeyReason`. Implemented as an upsert, so
the same command also satisfies LPT-2403 editing.

### Endpoint detail

```
POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}
```

| Item | Value |
|------|-------|
| Path parameter | `hearingId` — UUID of the seeding hearing |
| `Content-Type` | `application/vnd.hearing.save-ptph-detail+json` |
| `Accept` | `application/json` |
| `CJSCPPUID` | logged-in user id (UUID) — required for access control |
| Action name | `hearing.save-ptph-detail` → handler command `hearing.command.save-ptph-detail` |
| Response | `202 Accepted`, empty body |

**Request headers:**

```http
POST /hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4 HTTP/1.1
Host: <gateway-host>
Content-Type: application/vnd.hearing.save-ptph-detail+json
Accept: application/json
CJSCPPUID: 4a2b1e6c-1f3d-4a9e-9c11-6f0f2f9c7d31
```

**Sample payload** — Type 1 (fixed date), `keyReason` mandatory:

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_2",
  "listType": "TYPE_1_FIXED",
  "keyReason": "Trial fixed date required by court order"
}
```

**Sample payload** — Type 2 (flexible date), no `keyReason`:

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_5",
  "listType": "TYPE_2_FLEXIBLE"
}
```

**Sample payload** — tier only (list type chosen later):

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_1"
}
```

**Field rules** (`json/schema/hearing.save-ptph-detail.json`, `additionalProperties: false`):

| Field | Required | Values |
|-------|----------|--------|
| `hearingId` | yes | UUID — must match the path parameter |
| `tier` | yes | `TIER_1` … `TIER_7` |
| `listType` | no | `TYPE_1_FIXED` \| `TYPE_2_FLEXIBLE` |
| `keyReason` | conditional | required (non-blank) when `listType = TYPE_1_FIXED`; **ignored/nulled** for any other list type |

**curl:**

```bash
curl -i -X POST \
  "$GATEWAY/hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" \
  -H "Content-Type: application/vnd.hearing.save-ptph-detail+json" \
  -H "CJSCPPUID: $USER_ID" \
  -d '{"hearingId":"92ff0722-c287-11e9-9cb5-2a2ae2dbcce4","tier":"TIER_2","listType":"TYPE_1_FIXED","keyReason":"Trial fixed date required by court order"}'
```

**Internal hop** — command handler (`message://command/handler/message/hearing`),
media type `application/vnd.hearing.command.save-ptph-detail+json`, same payload shape:

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_2",
  "listType": "TYPE_1_FIXED",
  "keyReason": "Trial fixed date required by court order"
}
```

**Error behaviour:** a blank/absent `keyReason` with `listType = TYPE_1_FIXED` throws
from `PtphDetailCommandHandler.resolveKeyReason` ("keyReason is required when listType
is TYPE_1_FIXED"); saving against a finalised record is rejected by
`HearingPtphDetailDelegate.savePtphDetail`. Both surface as command-side failures, not
as a 4xx on the POST (the POST has already returned 202).

---

## LPT-2402 — [BE] [hearing] create endpoint for deleting list type and tier

**Type:** Story · **Status:** Done · **Assignee:** Unassigned

**Description:** Provide the command endpoint that deletes all captured tier/list-type
information for a hearing, returning it to a blank state.

**Scope of work:**
- `POST /hearings/{hearingId}` media type `application/vnd.hearing.delete-ptph-detail+json` → `hearing.command.delete-ptph-detail`.
- Aggregate emits `PtphDetailDeleted`; listener removes the view-store row.

**Acceptance criteria:**
- Delete is permitted in any state (draft or finalised).
- Clears tier, list type, key reason and the finalised flag.
- Returns 202; subsequent query shows nothing saved.
- Access granted to the same roles as `hearing.set-trial-type`.

**Status:** ✅ **Done** — `PtphDetailCommandHandler.deletePtphDetail` emits
`PtphDetailDeleted` with no state precondition (so it works draft or finalised); the
delegate clears tier / list type / key reason / finalised on the aggregate and the
listener removes the view-store row.

### Endpoint detail

```
POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}
```

| Item | Value |
|------|-------|
| Path parameter | `hearingId` — UUID of the seeding hearing |
| `Content-Type` | `application/vnd.hearing.delete-ptph-detail+json` |
| `Accept` | `application/json` |
| `CJSCPPUID` | logged-in user id (UUID) — required for access control |
| Action name | `hearing.delete-ptph-detail` → handler command `hearing.command.delete-ptph-detail` |
| Response | `202 Accepted`, empty body |

**Request headers:**

```http
POST /hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4 HTTP/1.1
Host: <gateway-host>
Content-Type: application/vnd.hearing.delete-ptph-detail+json
Accept: application/json
CJSCPPUID: 4a2b1e6c-1f3d-4a9e-9c11-6f0f2f9c7d31
```

**Sample payload** — empty object; the hearing is identified by the path parameter,
which the framework injects into the payload for the handler. The schema is
`additionalProperties: false` with no declared properties, so sending `hearingId` in
the body would be **rejected**:

```json
{ }
```

**curl:**

```bash
curl -i -X POST \
  "$GATEWAY/hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" \
  -H "Content-Type: application/vnd.hearing.delete-ptph-detail+json" \
  -H "CJSCPPUID: $USER_ID" \
  -d '{}'
```

**Internal hop** — `application/vnd.hearing.command.delete-ptph-detail+json`, with the
path parameter now present in the payload:

```json
{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" }
```

**Effect:** emits `hearing.ptph-detail-deleted`; `PtphDetailEventListener` removes the
`ha_ptph_detail` row, so the LPT-2406 query subsequently reports a blank record —
`HearingService.getPtphDetail` returns `PtphDetailResponse(null, null, null, false)`,
i.e. no tier / list type / key reason and `"finalised": false`.

---

## LPT-2403 — [BE] [hearing] create endpoint for editing list type and tier

**Type:** Story · **Status:** Done · **Assignee:** Unassigned

**Description:** Allow captured tier/list-type values to be changed while the record is
not yet finalised (the screen "Change" links).

**Scope of work:**
- Editing reuses the LPT-2401 capture command as an **upsert** — re-posting `hearing.save-ptph-detail` overwrites the current values.

**Acceptance criteria:**
- Re-saving replaces the previously stored tier / list type / key reason.
- Editing is **rejected once the record is finalised** (immutability — see LPT-2404).
- The Type 1 key-reason rule from LPT-2401 applies to edits too.

**Status:** ✅ **Done** — satisfied by the LPT-2401 upsert command;
immutability-after-finalise is enforced in `HearingPtphDetailDelegate.savePtphDetail`,
which rejects a save once the momento is finalised.

### Endpoint detail

**No separate endpoint** — editing re-posts the LPT-2401 save command:

```
POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}
Content-Type: application/vnd.hearing.save-ptph-detail+json
Accept: application/json
CJSCPPUID: <user id UUID>
→ 202 Accepted
```

**Sample payload** — change the tier of an already-saved (not finalised) record; the
whole record is replaced, so send every field you want to keep:

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_4",
  "listType": "TYPE_1_FIXED",
  "keyReason": "Trial fixed date required by court order"
}
```

**Sample payload** — change list type Type 1 → Type 2; omitting `keyReason` is correct
here, and any `keyReason` sent alongside `TYPE_2_FLEXIBLE` is discarded by
`resolveKeyReason`:

```json
{
  "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4",
  "tier": "TIER_4",
  "listType": "TYPE_2_FLEXIBLE"
}
```

**Semantics:** replace, not merge — the emitted `PtphDetailSaved` carries exactly the
posted values, and `PtphDetailEventListener` upserts the `ha_ptph_detail` row from
them. Omitting `listType` on an edit therefore **clears** a previously stored list type.
Once the record is finalised (LPT-2404) this command is rejected by the aggregate
delegate; delete (LPT-2402) is then the only permitted operation.

---

## LPT-2404 — [BE] [hearing] create endpoint for finalizing list type and tier

**Type:** Story · **Status:** Done · **Assignee:** Unassigned

**Description:** Provide the command endpoint that finalises (locks) the tier/list-type
information so it can no longer be edited.

**Scope of work:**
- `POST /hearings/{hearingId}` media type `application/vnd.hearing.finalise-ptph-detail+json` → `hearing.command.finalise-ptph-detail`.
- Aggregate emits `PtphDetailFinalised`; listener sets `finalised = true`.

**Acceptance criteria:**
- Finalise is **rejected unless both a tier and a list type are present**.
- Finalise is rejected if the record is already finalised.
- After finalisation, save/edit (LPT-2401/2403) is rejected; only delete (LPT-2402) is permitted.
- The `finalised` flag is exposed by the query (LPT-2406).
- Access granted to the same roles as `hearing.set-trial-type`.

**Status:** ✅ **Done** — `PtphDetailCommandHandler.finalisePtphDetail` emits
`PtphDetailFinalised`; `HearingPtphDetailDelegate.finalisePtphDetail` rejects the
command when either tier or list type is missing, and when the record is already
finalised. The listener sets `finalised = true`, exposed by `hearing.get-ptph-detail`.

### Endpoint detail

```
POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}
```

| Item | Value |
|------|-------|
| Path parameter | `hearingId` — UUID of the seeding hearing |
| `Content-Type` | `application/vnd.hearing.finalise-ptph-detail+json` |
| `Accept` | `application/json` |
| `CJSCPPUID` | logged-in user id (UUID) — required for access control |
| Action name | `hearing.finalise-ptph-detail` → handler command `hearing.command.finalise-ptph-detail` |
| Response | `202 Accepted`, empty body |

**Request headers:**

```http
POST /hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4 HTTP/1.1
Host: <gateway-host>
Content-Type: application/vnd.hearing.finalise-ptph-detail+json
Accept: application/json
CJSCPPUID: 4a2b1e6c-1f3d-4a9e-9c11-6f0f2f9c7d31
```

**Sample payload** — empty object (the hearing comes from the path parameter; the schema
is `additionalProperties: false` with no declared properties, so sending `hearingId` in
the body would be **rejected**):

```json
{ }
```

**curl:**

```bash
curl -i -X POST \
  "$GATEWAY/hearing-command-api/command/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" \
  -H "Content-Type: application/vnd.hearing.finalise-ptph-detail+json" \
  -H "CJSCPPUID: $USER_ID" \
  -d '{}'
```

**Internal hop** — `application/vnd.hearing.command.finalise-ptph-detail+json`:

```json
{ "hearingId": "92ff0722-c287-11e9-9cb5-2a2ae2dbcce4" }
```

**Preconditions (enforced in `HearingPtphDetailDelegate.finalisePtphDetail`, command-side
— the POST itself has already returned 202):**

- rejected unless **both** `tier` and `listType` are already saved;
- rejected if the record is already finalised.

**Effect:** emits `hearing.ptph-detail-finalised`; the listener sets `finalised = true`
on the `ha_ptph_detail` row, which the LPT-2406 query then reports as
`"finalised": true`. From that point save/edit is rejected and only delete is permitted.

---

## LPT-2405 — [BE] [listing] retrieve tier/listType from hearing context when creating the next hearing from a seeding hearing

**Type:** Story · **Status:** New · **Assignee:** Unassigned

**Description:** When a next hearing is created from a seeding hearing, the listing side
must retrieve the tier/list-type information from the hearing context and store it in
the listing payload, so it travels with the newly created hearing.

**Scope of work (indicative — to be designed):**
- On next-hearing creation from a seeding hearing, call the hearing-context query (LPT-2406 / `hearing.get-ptph-detail`) for the seeding hearing.
- Map tier/list type/key reason into the listing payload/model.
- Handle the case where the seeding hearing has no tier/list type (or is not finalised) — define expected behaviour.

**Acceptance criteria (to refine):**
- Next hearing created from a seeding hearing carries the seeding hearing's tier/list type.
- Behaviour defined for absent / non-finalised source data.

**Status:** ⛔ **Not started** — listing-side, separate service/repo. Depends on the
hearing-context query delivered under LPT-2406.

---

## LPT-2406 — [BE] [listing] court-calendar endpoints must return list type and tier info

**Type:** Story · **Status:** New · **Assignee:** Unassigned

**Description:** The endpoints consumed by the court calendar must return the tier and
list-type information for hearings.

**Scope of work (indicative — to be designed):**
- Hearing context already exposes `GET /hearings/{hearingId}/ptph-detail` (`hearing.get-ptph-detail`) returning `{ tier, listType, keyReason, finalised }` — delivered under this feature and available as the source of truth.
- Extend the court-calendar-facing listing endpoints to surface tier/list type in their responses (either by including the fields directly or by calling the hearing-context query).

**Acceptance criteria (to refine):**
- Court-calendar endpoints return tier and list type (and finalised flag) for each relevant hearing.
- Field naming/shape agreed with the calendar consumers.

**Status:** ◑ **Partially enabled — still open.** The hearing-context read model and
query are done (`GET /hearings/{hearingId}/ptph-detail` → `hearing.get-ptph-detail`,
`HearingQueryView.getPtphDetail`, `PtphDetailResponse`, query Drools rule). The
listing / court-calendar endpoint changes are **not** done.

### Endpoint detail — hearing-context enabler (implemented)

```
GET /hearing-query-api/query/api/rest/hearing/hearings/{hearingId}/ptph-detail
```

| Item | Value |
|------|-------|
| Path parameter | `hearingId` — UUID of the seeding hearing |
| `Accept` | `application/vnd.hearing.get-ptph-detail+json` |
| `CJSCPPUID` | logged-in user id (UUID) — required for access control |
| Action name | `hearing.get-ptph-detail` |
| Response | `200 OK`, `Content-Type: application/vnd.hearing.get-ptph-detail+json` |

**Request headers:**

```http
GET /hearing-query-api/query/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4/ptph-detail HTTP/1.1
Host: <gateway-host>
Accept: application/vnd.hearing.get-ptph-detail+json
CJSCPPUID: 4a2b1e6c-1f3d-4a9e-9c11-6f0f2f9c7d31
```

No request body (GET).

**Sample response** — finalised record:

```json
{
  "tier": "TIER_2",
  "listType": "TYPE_1_FIXED",
  "keyReason": "Trial fixed date required by court order",
  "finalised": true
}
```

**Sample response** — saved but not yet finalised, tier only:

```json
{
  "tier": "TIER_1",
  "finalised": false
}
```

**Sample response** — nothing saved (or deleted): `HearingService.getPtphDetail` returns
`PtphDetailResponse(null, null, null, false)`, so only the `finalised` flag is
meaningful:

```json
{
  "finalised": false
}
```

**Response fields** (`json/schema/hearing.get-ptph-detail.json`,
`additionalProperties: false`):

| Field | Required | Type |
|-------|----------|------|
| `tier` | no | string (`TIER_1`…`TIER_7`) or null |
| `listType` | no | string (`TYPE_1_FIXED` \| `TYPE_2_FLEXIBLE`) or null |
| `keyReason` | no | string or null |
| `finalised` | **yes** | boolean |

**curl:**

```bash
curl -s \
  "$GATEWAY/hearing-query-api/query/api/rest/hearing/hearings/92ff0722-c287-11e9-9cb5-2a2ae2dbcce4/ptph-detail" \
  -H "Accept: application/vnd.hearing.get-ptph-detail+json" \
  -H "CJSCPPUID: $USER_ID"
```

This is the query LPT-2405 and the court-calendar-facing listing endpoints are expected
to call (or mirror).
