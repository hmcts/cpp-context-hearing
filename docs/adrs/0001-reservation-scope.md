---
Title: ADR-RESERVE-A-SLOT-001 — Decide whether a booking can be placed from the record of the share, not from courtscheduler's rows
Date: 2026-10-17
Deciders: Listing / Hearing team (proposed, not yet agreed)
Technical Story: https://tools.hmcts.net/jira/browse/LPT-2600
---

## Status:

proposed

## Context:

Reserve-a-slot holds court capacity from the moment a clerk picks a slot until the results are
shared. Four defects have come out of it in as many weeks — LPT-2582, LPT-2583, LPT-2599 and
LPT-2600. They share one cause: a consumer asks courtscheduler "does this booking still exist?"
when the question it needs answered is "can this booking still be placed?". Those were the same
question before reserve-a-slot. They no longer are.

### A booking reference is a pointer and a hold

A `bookingReference` on a result line does two things:

- **a pointer** — which session this line's next hearing is for. Needed until the share has
  listed it, and on every re-share after.
- **a hold** — capacity reserved in courtscheduler's `allocated_listings`. Ephemeral: it is
  released when it is surplus, when it expires, when the hearing is vacated or shrunk.

Releasing the hold deletes the row. Any consumer that treats the row as the pointer then loses the
pointer too.

### The pointer already exists, and predates reserve-a-slot

Progression records every reference it resolves at share time, with the court schedule ids it
resolved to, on the hearing's aggregate:

- `HearingResultedEventProcessor.sendStoreBookingReferencesCommand` →
  `HearingAggregate.storeBookingReferencesWithCourtScheduleIds` →
  `BookingReferencesAndCourtScheduleIdsStored`, keyed by hearing day.
- Each share stores the **combined** map — everything remembered from earlier shares plus the
  fresh lookups — so the record accumulates across re-shares of the same hearing day.
- On every later share, `getCombinedBookingReferencesAndCourtScheduleIds` takes remembered
  references from that record and asks courtscheduler **only** for references it has not seen.

This is on `main` and in the initial migrated commit. It was built to fix an older race — slots
freed before they were queried — and it is event-sourced, scoped to the hearing and hearing day,
and untouched by anything courtscheduler deletes.

Reserve-a-slot then added the **carry-over** (progression `416cf352c`, `baf2f541f`, 2026-10-08).
A re-share vacates the previous hearing's confirmed rows before listing enriches the next hearing,
so listing's courtscheduler lookup came back empty and the hearing was dropped. Progression now
replays its remembered entries in `list-next-hearings` as `bookingReferencesWithCourtScheduleIds`,
and listing falls back to them (`CourtScheduleEnrichmentService.carriedCourtScheduleIdsFor`). Only
remembered references are carried. A hold that expired before it was ever shared has no entry, so
it is never listed into a slot nobody holds.

So the pointer and the hold are already separated: **progression holds the pointer, courtscheduler
holds the hold.** The carry-over uses the pointer correctly at the listing boundary. The defects
come from consumers that do not use it.

### What happens to a reference courtscheduler no longer has

When a share reaches progression's transformer, a reference resolves one of two ways:

| reference | in courtscheduler? | in progression's record? | outcome |
|---|---|---|---|
| live hold, never shared | yes | no | resolved fresh, listed |
| shared, then released as surplus (LPT-2599, LPT-2600) | no | **yes** | resolved from the record, listed |
| shared, then vacated by a re-share | no | **yes** | resolved from the record; listing falls back to the carry-over |
| re-picked after an amend, hold expired before sharing | no | no | **dropped silently** |
| never shared, hold expired | no | no | **dropped silently** |

"Dropped silently" is `HearingToHearingListingNeedsTransformer` lines 203–209 (cases), 286–288
(applications) and `HearingListingNeedsTransformer` 146–148: a `WARN` log and `return`. The next
hearing is never listed and the clerk is not told. Crown is the exception; its reference is the
court schedule id.

That last pair is the only case the pre-share gate exists to catch. Everything else is placeable.

### Why the gate fails

The gate (`session-availability.helper.ts`) decides which references to check with courtscheduler,
and refuses the share if any is absent. It uses the *line's* `sharedDate` as a stand-in for "this
reference was shared":

```typescript
const isSettledShare = line => !!line?.sharedDate && !line?.amendmentReason;
```

The stand-in breaks on amend + re-pick: the line keeps its old `sharedDate` but carries a new,
never-shared reference. The `!amendmentReason` clause exists to catch that, and the existing test
documents it (`'still checks a shared line that has since been amended'`). But the clause also
catches a line that was amended and *not* re-picked, whose reference was shared and is in
progression's record. That is LPT-2600.

A line-level flag cannot tell those two apart. Only the reference itself can.

### Evidence

Observed on STE02 on 2026-10-16 (hearing `faef00c5`), reconstructed from the courtscheduler
access log, the hearing event store and `allocated_listings`:

```
11:07:34  results-shared-v3                      four lines shared, all get a sharedDate
11:07:38  DELETE /sessions/62984736        202   the merge releases the loser
11:08:45  hearing.event.amended                  an amendment marks that released line
11:09:31  book-provisional-hearing-slots         a NEW line takes a fresh hold
          booking-status(62984736, …) × 11       over four hours, every one NOT_FOUND
```

`62984736` was in the 11:07:34 share, so progression had it on record. A share would have placed
it. The gate asked courtscheduler instead, got `NOT_FOUND` eleven times, and refused every share
for four hours — sending no request and logging nothing. It was found because a clerk
screenshotted the banner.

### The gate never protected capacity

The obvious objection to letting more shares through is overbooking. The gate does not prevent it
and never did. `ReservationService`'s header records that the UI's share-time capacity check was
*replaced* by the hold-expiry check "that says nothing about capacity", and that capacity is now
enforced at pick time, in `reserveAll` via `validateSessionAvailabilityListMode`. The share path
does not call it. The gate's only job is the silent-drop case above.

## Decision:

**Whether a reference can still be placed is decided by the record of the share that resolved it.
Courtscheduler's rows answer only whether a hold is live, and that matters only for a reference
that has never been shared.**

### 1. The gate checks only references that were not in a previous share

Before sharing, the gate fetches the last shared result for the hearing day — the UI already does
this (`fetchSharedResult` → `hearing.get-share-result-v2`), and shared result lines carry their
prompts — and collects the booking references in it.

- A reference **in** that set was resolved and recorded by progression. Do not check it.
- A reference **not** in that set is a fresh pick. Check it with courtscheduler. If it is absent,
  block the share, name the line, and ask the clerk to pick a session again.

`isSettledShare` and its `amendmentReason` clause are deleted. LPT-2600 shares; amend + re-pick is
still checked; both existing intents hold.

Hearing's shared result lines and progression's record both come from the same share, but they
are not identical. Progression records only the references it managed to resolve, so the shared
lines are a superset. They differ in exactly one case: a reference that was shared but dropped
silently because its hold had already gone. The gate treats that reference as settled and does not
re-check it on a re-share. That is not a regression, because its next hearing was lost at the
earlier share. But it means the gate cannot rescue it, and the only place to catch it is the drop
itself. That is why making the drop loud is part of the plan, not an optional extra.

### 2. Always mint a new booking id — a prerequisite of rule 1

Delete `bookingIdToReuse`. Mint on every pick, as `team/ccsph2` does, and send the previous id as
`supersedes` so courtscheduler releases its unconfirmed hold in the same transaction.

This is not a tidy-up. Progression's record takes precedence over courtscheduler: references
already on record are never re-fetched (`getCombinedBookingReferencesAndCourtScheduleIds` asks only
for references not on record, and remembered entries overwrite fresh ones). So a **reused** id that
was in an earlier share, then re-picked to a different session, is listed against its **old**
session, and the new hold is stranded until the purge. `bookingIdToReuse` reuses exactly those ids:
a shared-then-released reference answers `NOT_FOUND`, not `SHARED`, so it is reused. That is the
path once described as LPT-2600 "healing itself" when the clerk re-picks; the re-pick is in fact
ignored. With always-mint, every pick is a reference progression has never seen, so it is
resolved fresh, and rule 1 checks it.

`supersedes` keeps what reuse was buying. Today `reserveAll` releases unconfirmed rows under the
*same* id before reserving, which is how a re-pick frees the old hold atomically. Releasing under the
superseded id instead uses the same `releaseReservationsForBooking`, which touches only rows with
`expires_at` set, so a superseded id whose booking was already shared keeps its confirmed listing.
The `BOOKING_ALREADY_CONFIRMED` refusal then cannot arise: a fresh id is never already confirmed.

### 3. Keep the carry-over, the surplus release and the merge as they are

The carry-over is the same rule applied at the listing boundary. `SurplusReservationReleaseService`
gives back capacity the merge no longer needs; deleting the row is fine once nothing treats the row
as the pointer. The merge is correct, and older than reserve-a-slot.

### Supporting decision

**Make refusals observable.** Log, and ideally count, every share the gate refuses: hearing,
hearing day, line, reference, what courtscheduler returned. The LPT-2600 refusals sent nothing and
logged nothing.

### Deliberately not decided

**Reserving per session rather than per result line** would stop the surplus existing at all. With
this decision it is not needed for correctness. Its only benefit is capacity accuracy in the
pick-to-share window, where two holds are consumed for what becomes one hearing until the merge
gives one back. It also needs a client-side "am I the only line holding this?" predicate, which is
the kind of guess this decision removes. Measure how often sessions run near capacity in that
window before building it.

## Alternatives considered:

**Stop blocking on an absent booking altogether.** Rejected. A fresh pick whose hold expired is not
in progression's record; the share would send it and the transformer would drop the next hearing
with a `WARN` and nothing else. A refused share is recoverable. A next hearing that silently never
gets listed is not.

**Drop only the `amendmentReason` clause from `isSettledShare`.** Rejected. It fixes LPT-2600 by
skipping every line that has ever been shared, including an amended, re-picked line whose fresh
reference has expired — the silent-drop case. It swaps one wrong guess for another.

**A `status` column on `allocated_listings`, with soft-delete.** Rejected on blast radius. Leaving
released rows in the live table makes every query that reads `expires_at` as state wrong: ~13
definite changes and ~5 to audit; the partial unique index from changeset 053 must be rewritten or
a re-pick of the same session throws; capacity is counted both by a derived SQL sum and by
denormalised counters on `court_schedule`, and both would need the filter and then have to stay in
step. Two sites would gain new defects rather than need a filter: the nightly purge
(`findExpiredReservedSessions`) would re-find released rows, credit capacity again and overwrite
their reason every night, and `releaseReservationsForBooking` would double-credit on a retry. It
also builds a second pointer when one already exists.

**An append-only `booking_release_log` in courtscheduler.** Deferred, not rejected. It records *why*
a row went (surplus, expired, vacated, shrunk), which is useful for diagnostics and for
reconciling capacity after the fact. It is not needed for any decision above. It must not replace
the carry-over or back `fetchProvisionalSlots`: it would resolve expired holds too, so listing would
list a never-shared next hearing into a slot nobody holds unless every release site stamped the
right reason and every reader filtered on it. That is inference from server state again, and a
second record of a fact progression already owns. Build it if a diagnostic need appears.

**An `archived_bookings` table mirroring `allocated_listings`.** Rejected for the same reasons as
the log, with the added cost of mirroring every future column.

**Return to the CCSPH2 model: provisional bookings, no capacity hold.** Rejected. It removes the
double-booking protection reserve-a-slot was built to provide.

**Resurrect `provisional_booking` as the pointer.** Rejected. `statusOf()` had that fallback and it
was removed because a `provisional_booking` row "proves only that a booking was once recorded,
never that a session is still held". It is also still written: releases flip `active` back to true
(`CourtScheduleRepositoryImpl:1947`) and there is a second writer at `:2443`. Progression's record
already is the pointer.

## Consequences:

### Pros:

- No schema change and no new table. The gate fix is UI-only; always-mint adds one optional
  field (`supersedes`) along the chain `bookingId` already travels.
- Removes a predicate (`isSettledShare`) and a reuse rule (`bookingIdToReuse`) instead of adding a
  fifth guess.
- One rule across the system: the carry-over already applies it at listing, the gate applies it in
  the UI.
- Hearings already stuck like `faef00c5` recover on their next share attempt with no data fix,
  because the fix is in how the gate reads, not in what is stored.
- Nothing that works today changes in progression or listing. Courtscheduler gains only the
  `supersedes` release on booking, reusing an existing, already-safe method.

### Cons:

- The gate gains a fetch of the last shared result before sharing. It is one query the UI already
  knows how to make, per share attempt.
- Both records are per hearing day. A reference shared under one hearing day is not on record for
  another. Results are shared per hearing day, so this matches how amendments work, but it is an
  assumption to verify.
- Legacy hearings shared before shared result lines were stored return no result lines. The gate
  must fall back to checking every reference for those, which is today's behaviour.
- The silent drop in progression's transformer remains for any path that reaches it without the
  gate — including a hold that expires between the gate's check and progression's lookup — and a
  reference dropped that way is then treated as settled by later gates. It must be made loud (see
  the implementation plan); this decision narrows how often it is reached but does not remove it.

### Unverified:

- That a reused, previously shared booking id re-picked to a new session is listed against its old
  session. Traced from progression's code (record takes precedence, remembered references not
  re-fetched), not reproduced. If confirmed it is a live defect today, independent of LPT-2600,
  and always-mint fixes it.
- That a shared result line's booking-reference prompt carries the same value as the draft line's.
  The interface has `prompts`; the value has not been checked against a real payload.
- How many hearings are currently stuck. The detector has been written but not run against a live
  environment.
- Whether LPT-2582's duplicate-hearing behaviour is a reserve-a-slot regression or pre-existing in
  CCSPH2. The merge-key asymmetry it is attributed to exists identically on `main`.
- `releaseExpiredReservations(LocalDate.now(UTC))` compares `expires_at < today` against a value
  stamped as today's date, so a hold taken at 09:00 Monday is not swept until Tuesday's run, later
  still across the Sunday/Monday no-run, which is a business requirement. Day granularity against a
  timestamp is a separate defect for its own ticket.
