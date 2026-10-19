# Reserve a slot — endpoints for the front end

What the browser calls, what comes back, and the three things that have already caught people
out. Written for front-end developers working in `cpp-ui-hearing`.

A clerk picks a court session while recording a result. From the moment they pick it, the
session is genuinely **held** — real capacity is taken off it — until the result is shared. If
the result is never shared, a purge at 01:00 releases the hold and the session goes back in the
pool. That is the whole feature, and it is why these calls exist.

Contracts below come from the specs on `team/ras` (hearing's RAML, listing's RAML), not from
observed traffic.

---

## You make three calls

```
①  POST /hearings/{hearingId}/hearing-slots     hold a session      (async — see below)
②  POST /hearings/{hearingId}/hearing-slots     give it back        (fire and forget)
③  POST /bookingStatus                          is the hold alive?  (before sharing)
```

There are two more endpoints in this feature — `POST /unconfirmedBooking` and
`GET /unconfirmedBooking/status` on courtscheduler. **You never call those.** They are
service-to-service, behind ① and ③. They appear here only because their errors surface to you
through ①, and their names changed in the rename.

### What was renamed

Everything below is `main` → `team/ras`. **Only the media type on ① affects you** — the two
courtscheduler rows are service-to-service, listed because you will meet the old names in logs,
in older branches and in the API tests.

**① Hold a session** — path unchanged, media type and command name changed:

```diff
  POST /hearing-command-api/command/api/rest/hearing/hearings/{hearingId}/hearing-slots

- Content-Type: application/vnd.hearing.book-provisional-hearing-slots+json
+ Content-Type: application/vnd.hearing.book-unconfirmed-hearing-slots+json

- command name: hearing.book-provisional-hearing-slots
+ command name: hearing.book-unconfirmed-hearing-slots
```

A client still sending the old media type gets **415 Unsupported Media Type**. There is no
aliasing and no deprecation window — the old name is simply gone.

**courtscheduler** — path *and* media types changed (you do not call these):

```diff
- POST /listingcourtscheduler-api/rest/courtscheduler/provisionalBooking
+ POST /listingcourtscheduler-api/rest/courtscheduler/unconfirmedBooking

- Content-Type: application/vnd.courtscheduler.create.provisional.booking+json
+ Content-Type: application/vnd.courtscheduler.create.unconfirmed.booking+json
- Accept:       application/vnd.courtscheduler.create.provisional.booking.response+json
+ Accept:       application/vnd.courtscheduler.create.unconfirmed.booking.response+json
```

```diff
- GET /listingcourtscheduler-api/rest/courtscheduler/provisionalBooking?bookingIds=…
+ GET /listingcourtscheduler-api/rest/courtscheduler/unconfirmedBooking?bookingIds=…

- Accept: application/vnd.courtscheduler.get.provisional.booking+json
+ Accept: application/vnd.courtscheduler.get.unconfirmed.booking+json
```

Summary:

| Old | New | Who calls it |
|---|---|---|
| `vnd.hearing.book-provisional-hearing-slots+json` | `vnd.hearing.book-unconfirmed-hearing-slots+json` | **you** (①) |
| `POST /provisionalBooking` | `POST /unconfirmedBooking` | hearing's event processor |
| `GET /provisionalBooking` | `GET /unconfirmedBooking` | listing |
| — | `GET /unconfirmedBooking/status` | listing (behind ③) — **new** |
| — | `POST /bookingStatus` | **you** (③) — **new** |
| — | `vnd.hearing.release-unconfirmed-hearing-slots+json` | **you** (②) — **new** |

A courtscheduler that has not been redeployed still serves `/provisionalBooking` only, so a caller
on the new path gets a **500** — Spring renders an unmatched path as 500, not 404, which makes a
version mismatch look like a server fault. If ① starts failing right after a deploy, check that
before anything else.

### Base URL and headers

```ts
const STE = 'https://steccm02.ingress01.ste.nl.cjscp.org.uk';
// every request carries the caller identity
headers: { CJSCPPUID: '<user id>' }
```

| | path |
|---|---|
| hearing commands (①, ②) | `/hearing-command-api/command/api/rest/hearing` |
| listing queries (③) | `/listing-query-api/query/api/rest/listing` |

---

## ① Hold a session

```
POST {hearingWriteBase}/hearings/{hearingId}/hearing-slots
Content-Type: application/vnd.hearing.book-unconfirmed-hearing-slots+json
→ 202 Accepted
```

> **Renamed.** Was `application/vnd.hearing.book-provisional-hearing-slots+json`; old clients get
> a 415. See *What was renamed*.

### Request

```json
{
  "slots": [
    {
      "courtScheduleId": "8322f37a-ed45-4757-8de7-da3dc12545a1",
      "hearingStartTime": "2026-10-07T09:00:00.000Z",
      "duration": 60
    }
  ],
  "bookingId": "4eb0975d-84b4-4510-8c79-f2366e69863d"
}
```

| Field | Required | What to send |
|---|---|---|
| `slots` | **yes** | at least one |
| `slots[].courtScheduleId` | **yes** | the session the clerk picked |
| `slots[].hearingStartTime` | **yes** | ISO date-time |
| `slots[].duration` | see below | minutes |
| `bookingId` | no | the `bookingReference` already on the result line, when re-picking. Omit on a first pick and one is minted for you. |

**`duration` is not optional in practice.** The JSON schema says it is, but a *duration-based*
session is rejected without it:

> `400 duration is required and must be greater than zero for duration-based session <id>`

Slot-based sessions ignore it. The slot search tells you which kind you have (`slotBased`), so
send `duration` whenever you have it and always for a duration-based pick. Send it as a number,
or omit the key entirely — **never `null`**.

The schema is `additionalProperties: false`, and so are the two schemas behind it. An extra field
is not ignored; the command is rejected part-way down the chain, which surfaces to you as a
timeout rather than a validation error.

### Response — the part that catches people

The **202 tells you nothing about success**. The outcome arrives asynchronously on the public
event `public.hearing.hearing-slots-provisionally-booked`, and that event has two shapes:

```json
{ "bookingId": "4eb0975d-84b4-4510-8c79-f2366e69863d" }
```
```json
{ "error": "courtscheduler returned 400: {\"error\":[\"duration is required …\"]}" }
```

`CppHttp.commandSync` resolves on the **event name**, which is the same for both — so a refusal
arrives as an ordinary value, not an error. `ProvisionalBookingService` converts it:

```ts
switchMap(response =>
  response.bookingId
    ? of(response as { bookingId: string })
    : throwError(() => new BookingRefusedError(response.error || '…'))
)
```

Keep that distinction when you handle failures:

| | Means | Tell the clerk |
|---|---|---|
| `BookingRefusedError` | the backend deliberately refused — e.g. the session filled up between search and pick | "pick another session" |
| anything else | HTTP error, rejected command, timeout waiting for the event | a technical failure — **not** that the session is unavailable |

Blaming the session for a technical failure sends the clerk off to re-pick a session that was
never the problem. That has happened on STE.

On success, write the returned `bookingId` into the result line's `bookingReference` prompt, and
tell the clerk the hold dies at midnight — that is the only point in the flow where the rule is
stated.

### curl

```bash
curl -i -X POST "$STE/api/hearing-command-api/command/api/rest/hearing/hearings/$HEARING_ID/hearing-slots" \
  -H "Content-Type: application/vnd.hearing.book-unconfirmed-hearing-slots+json" \
  -H "CJSCPPUID: $UID" \
  --data-raw '{
    "slots": [
      {
        "courtScheduleId": "8322f37a-ed45-4757-8de7-da3dc12545a1",
        "hearingStartTime": "2026-10-07T09:00:00.000Z",
        "duration": 60
      }
    ]
  }'
```

You get `202` immediately. To see what actually happened you need the public event — which is why
the UI uses `commandSync` rather than `command` here.

---

## ② Give a hold back

```
POST {hearingWriteBase}/hearings/{hearingId}/hearing-slots
Content-Type: application/vnd.hearing.release-unconfirmed-hearing-slots+json
→ 202 Accepted
```

Same path as ①; the media type picks the action. **New** in this feature — there was no release
call before.

```json
{ "bookingId": "4e29c1fa-7d3b-4f21-9c88-1a2b3c4d7ac1" }
```

`bookingId` is required; `additionalProperties: false`.

**Fire and forget.** An unknown or already-released id is a no-op, never an error, and **no public
event is published** — so use `command`, not `commandSync`, or you will wait forever for an event
that never comes.

Call it when a hold is abandoned with no new pick to carry the reference forward: the clerk
deletes a booked slot, or the whole draft is discarded. You do **not** need to call it when the
clerk simply re-picks a different session under the same `bookingId` — courtscheduler releases the
old one itself as part of taking the new one.

A booking that has already been **shared** is never released by this call. Only a share may change
a confirmed booking. Check with ③ first — see `isUnconfirmedBooking` in
`results/core/helpers/provisional-booking.ts`.

```bash
curl -i -X POST "$STE/api/hearing-command-api/command/api/rest/hearing/hearings/$HEARING_ID/hearing-slots" \
  -H "Content-Type: application/vnd.hearing.release-unconfirmed-hearing-slots+json" \
  -H "CJSCPPUID: $UID" \
  --data-raw '{"bookingId":"4e29c1fa-7d3b-4f21-9c88-1a2b3c4d7ac1"}'
```

---

## ③ Is the hold still alive?

```
POST {listingReadBase}/bookingStatus
Content-Type: application/vnd.listing.query.booking.status+json
→ 200 OK
```

**New** in this feature. Note it is a **POST on the query API** — the ids go in the body, so it
goes through `api.command`, not `api.query`. That detail causes bug ③ below.

### Request

```json
{ "bookingIds": ["f8254db1-1683-483e-afb3-b87fde5a0a26"] }
```

`bookingIds` is required, `minItems: 1`, `additionalProperties: false`.

### Response

```json
{
  "bookings": [
    { "bookingId": "f8254db1-1683-483e-afb3-b87fde5a0a26", "safeToShare": true,  "status": "RESERVED" },
    { "bookingId": "9e4932f7-97b2-3010-b942-ddd2624e4dd8", "safeToShare": false, "status": "NOT_FOUND" }
  ]
}
```

| `status` | `safeToShare` | What it means for the UI |
|---|---|---|
| `RESERVED` | true | a live hold — safe to share, and safe to release if abandoned |
| `SHARED` | true | already a real listing — **do not** resend this id on a new booking (see below) |
| `NOT_FOUND` | **false** | the hold expired and was purged — block the share, tell the clerk to re-pick |
| `UNKNOWN` | true | listing could not reach courtscheduler |

**`safeToShare` is false only for `NOT_FOUND`.**

`UNKNOWN` is listing's own value; courtscheduler never emits it. It resolves to `true` because
the gate **fails open** by design — an advisory check must not block every share in the building
over a transient fault. While `UNKNOWN` persists the gate is blind and a share into a purged hold
will go through. Do not "fix" this by failing closed without a conversation.

### curl

```bash
curl -s "$STE/api/listing-query-api/query/api/rest/listing/bookingStatus" \
  -H "Content-Type: application/vnd.listing.query.booking.status+json" \
  -H "CJSCPPUID: $UID" \
  --data-raw '{"bookingIds":["f8254db1-1683-483e-afb3-b87fde5a0a26"]}'
```

---

## Three things that have already gone wrong

**① The response is an `HttpResponse`, not the body.** `CppHttp.command` is built with
`{ observe: 'response', responseType: 'text' }`, so the payload arrives as an **unparsed string on
`.body`** and the object itself has no `bookings`. It is also typed `Observable<any>`, so
returning it raw satisfies any declared type and the compiler says nothing. The pre-share gate
once read `response?.bookings`, got `undefined`, and its `?? []` turned that into "nothing
unsafe" — the correct `safeToShare: false` came back over the wire and nothing acted on it.

`ListingService.getBookingStatus` now parses once, centrally, and **throws** on a body it cannot
read rather than returning `{ bookings: [] }` — an empty list is indistinguishable from "all
fine". Each caller picks its own direction: the pre-share gate fails open, the release effects
fail closed.

**② Never resend a `SHARED` bookingId.** courtscheduler refuses to reserve against an id that
already carries a confirmed allocation. That is the amend journey — share a result, amend it,
pick a different session — and before this was handled the clerk was told *"this session is fully
booked"* about a session that was free. `ProvisionalBookingService.bookingIdToReuse` now checks
the status first and omits the id when it is `SHARED`, so a fresh one is minted.

**③ Don't gate on result short codes.** The pre-share check once collected booking references
behind an allowlist of short codes (`NHCCS`, later `NHCCS` + `NHMC`). `NHMC` was missing, so a
magistrates-only draft produced `[]`, the gate skipped the call entirely, and a clerk shared into
a purged hold — twice on STE. An allowlist fails *silently*: a missing entry looks exactly like
"nothing to check". Use the shared predicate in `provisional-booking.ts`.

---

## Where this lives in `cpp-ui-hearing`

| Thing | File |
|---|---|
| ① and ② | `results/hearing-details/allocation/services/provisionalBooking.service.ts` |
| ③ | `core/services/listing/listing.service.ts` |
| status predicates, reference extraction | `results/core/helpers/provisional-booking.ts` |
| pre-share gate | `results/share-results/session-availability.helper.ts` |
| the pickers that call ① | `results/hearing-details/allocation/containers/{magistrates,crown-scheduling}.container.ts` |

---

## On the naming

You will see **provisional** and **unconfirmed** used for the same thing. The rename was scoped
deliberately to the wire contract — REST paths, media types, command names — and stops there.
Domain events, DB columns and class names still say *provisional*, including the public event
`public.hearing.hearing-slots-provisionally-booked` that ① resolves on. That is intentional:
events are a cross-estate contract with consumers outside these repos. Don't tidy them to match.
