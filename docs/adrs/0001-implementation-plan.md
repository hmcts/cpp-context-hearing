# Implementation plan — ADR-RESERVE-A-SLOT-001

Companion to `0001-reservation-scope.md`. Covers what to build, in what order, and what already
built needs undoing.

Status: draft, follows the ADR's own status (proposed, not yet agreed).

---

## Shape of the work

Mostly UI. No schema change and no new table. Courtscheduler gains one optional field on booking;
listing does not change; progression changes only to make an existing silent failure loud.

| # | Item | Repo | Size |
|---|---|---|---|
| 1 | Make refusals observable | cpp-ui-hearing | small |
| 2 | Gate reads the last share, deletes `isSettledShare` | cpp-ui-hearing | small–medium |
| 3 | Always mint with `supersedes`, delete `bookingIdToReuse` | cpp-ui-hearing, cpp-context-hearing, courtscheduler | small–medium |
| 4 | Make the transformer's drop loud | cpp-context-progression | small |
| 5 | Run the detector | read-only SQL | small |

2 and 3 fix LPT-2600 and **must ship together**: 2 is unsound while ids are reused (see 3). 4 is independent. 5 sizes the backlog and can run any
time; with 2 deployed, stuck hearings recover on their next share attempt with no data fix.

---

## 1. Make refusals observable  *(do first)*

The gate currently refuses with no request sent and nothing logged. Emit a log line, and ideally a
metric, on every refusal: hearing id, hearing day, result line id, booking reference, and what
courtscheduler returned. Independent of everything else, and it makes the rest measurable.

## 2. The gate reads the last share

`share-result.container.ts` calls `getBookingReferencesToCheck(draftResult)` before sharing.

- Fetch the last shared result for the hearing day — `fetchSharedResult` / `hearing.get-share-result-v2`,
  which the UI already uses — and collect the booking-reference prompt values from its result lines.
- `getBookingReferencesToCheck` takes that set and excludes any reference in it. Keep the existing
  exclusion for lines attached to an existing hearing (`existingHearingId`).
- Delete `isSettledShare`, including the `amendmentReason` clause.
- **Legacy hearings** — shared before shared result lines were stored — return no result lines.
  Fall back to checking every reference, which is today's behaviour.
- When a checked reference is absent, block the share, name the line, ask the clerk to pick a
  session again, and log it (item 1).

Tests (`session-availability.helper.spec.ts`):

| Test | Expectation |
|---|---|
| LPT-2600 red test (uncommitted), built from the real draft | `62984736` is in the last share → not checked; `88f2f534` (fresh pick) → checked. Goes green. |
| `'still checks a shared line that has since been amended'` | reword: the amended line's reference is **not** in the last share (re-picked) → checked. Stays green. |
| `'skips a line that was shared and not touched since'` | reword: its reference is in the last share → not checked. |
| new: legacy hearing, empty shared result | every reference checked |
| new: same reference in the last share and on an unshared line | not checked — the reference was recorded, wherever it sits now |
| existing `existingHearingId`, NHCCS/NHMC, Crown and empty-draft tests | unchanged |

Verify first: a shared result line's booking-reference prompt carries the same value as the draft
line's. Check against a real `get-share-result-v2` payload on STE02 before writing the extraction.

## 3. Always mint, with `supersedes`  *(prerequisite of 2)*

Why it cannot wait: progression never re-fetches a reference it already has on record. A reused id
that was in an earlier share and is re-picked to a new session is listed against its **old**
session, and the new hold is stranded. `bookingIdToReuse` reuses exactly those ids, because a
shared-then-released reference answers `NOT_FOUND`. Item 2 would skip checking them too, since they
are in the last share. Traced, not reproduced; see the ADR.

- **UI** (`provisionalBooking.service.ts`, committed in `7594dbea9`): delete `bookingIdToReuse` and
  its `booking-status` pre-call. Mint on every pick. Send the line's previous `bookingReference`, if
  any, as `supersedes`.
- **Hearing**: thread `supersedes` through the `book-unconfirmed-hearing-slots` command chain,
  alongside `bookingId` (the chain LPT-2535/2536 already carries it through). Optional field.
- **Courtscheduler** (`bookProvisionalSlots` / `reserveAll`): when `supersedes` is present, call
  `releaseReservationsForBooking(supersedes)` in the same transaction before reserving. That method
  touches only rows with `expires_at` set, so a superseded id whose booking was shared keeps its
  confirmed listing. Today's release-under-the-same-id stays for the multi-day case.
- **UI tidy-up**: the `BOOKING_ALREADY_CONFIRMED` branch and its comment go — a fresh id cannot be
  already confirmed.

Tests: re-pick sends a new id and `supersedes` the old one; the old unconfirmed hold is released in
the same transaction; a superseded *shared* id's confirmed listing is untouched; a failed reserve
leaves the old hold in place (all-or-nothing).

Optional confirmation before building: reproduce the old-session listing on STE02 — share, let the
merge release a reference, amend that line, re-pick a different session, share, and see which
session the next hearing lands in.

## 4. Make the transformer's drop loud

`HearingToHearingListingNeedsTransformer` lines 203–209 and 286–288, and
`HearingListingNeedsTransformer` 146–148, log a `WARN` and `return` when a reference resolves
neither from progression's record nor from courtscheduler. The next hearing is never listed and
nobody is told.

After item 2 this is reached only by a path that bypasses the gate — most plausibly a hold that
expires between the gate's check and progression's lookup. A reference dropped this way is then in
hearing's shared lines but not in progression's record, so later gates treat it as settled and
never re-check it. This is the only place it can be caught.

Minimum: log at `ERROR` with hearing, hearing day, offence or application, and reference. Better:
raise an event the results UI or a dashboard can surface. Decide which with the listing team.

## 5. Run the detector

Read-only. Find hearings whose next share the current gate would refuse:

1. latest `hearing.draft-result-saved-v2` per stream; lines with both `sharedDate` and
   `amendmentReason`, extract their `bookingReference`
2. left-join against `allocated_listings.booking_id`
3. unmatched → a hearing the current gate blocks; item 2 unblocks it

Never run against a live environment. It sizes the problem and tells us how urgent 1–3 are.

---

## To undo

| What | Where | Action | When |
|---|---|---|---|
| `isSettledShare` predicate | `session-availability.helper.ts` (committed, `1091694b2`) | **Delete.** | item 2 |
| LPT-2600 red unit test | `session-availability.helper.spec.ts` (uncommitted) | **Keep** as the regression test; give it a last-share input. | item 2 |
| `bookingIdToReuse` | `provisionalBooking.service.ts` (committed, `7594dbea9`) | **Delete.** Always mint, send `supersedes`. | item 3 |
| `BOOKING_ALREADY_CONFIRMED` handling in the UI | `provisionalBooking.service.ts` | **Delete.** Cannot arise with fresh ids. The backend code can stay. | item 3 |
| Earlier plan's "A3 tactical patch" and Track B (status column / release log) | not built | **Nothing to undo.** Superseded before any code was written. | — |

### Not to undo

- **The carry-over** (progression `416cf352c`, `baf2f541f`; listing's
  `carriedCourtScheduleIdsFor`). The ADR's rule already in action at the listing boundary.
- **`SurplusReservationReleaseService`** (restored by `b2838b095`). Deleting the row is fine once
  nothing treats the row as the pointer.
- **The merge.** Correct, and older than reserve-a-slot.
- **The LPT-2583 fix** (keep the live reference through the merge).
- **The purge** and **`purge-orphaned-reservations.sql`**.
- **The public event split** (8 failure codes, separate failure event). Different concern.
- **Serenity LPT-2582/2583/2599 coverage.**

---

## Separate tickets

- **Purge cutoff is day-granular.** `releaseExpiredReservations(LocalDate.now(UTC))` compares
  `expires_at < today` against a value stamped as today's date, so a hold taken at 09:00 Monday is
  not swept until Tuesday. The Sunday/Monday no-run is a business requirement and out of scope.
- **`source = 'RESERVED_UNCONFIRMED'`** does double duty as provenance and state.
- **Release log** — deferred. Build only if a diagnostic or capacity-reconciliation need appears,
  and never as a resolution source (see the ADR).

---

## Loose ends from this week

- **27 stranded `RESERVED_UNCONFIRMED` holds** on STE02 with no `hearing_id`, consuming capacity.
  `purge-orphaned-reservations.sql` clears them; needs a write connection.
- **`@LPT-2600` Serenity scenario does not reproduce the defect.** Twelve attempts; the unit test
  does it in six seconds. Retag it as the amend-and-reshare journey coverage it is, or delete it.
- **`amend-and-reshare-must-allow-a-new-session.feature.v2-daythree`** — a working backup file,
  delete before committing.
- **Three `tmp/ras-new-pub-event` branches** committed locally and never pushed.
- **Code review** of the public-event split changes, requested but not yet done.
- **Stale `~/.m2` artefact**: `hearing-query-api-17.104.202-ras-SNAPSHOT-raml.jar` installed under
  version `17.104.203`.
- **`hearing-viewstore-persistence` test sources do not compile** (`uk.gov.justice.core.courts`
  resolution). Pre-existing, unrelated.
- **`BookProvisionalHearingSlotsIT` has never run** — it needs a local WildFly on :8080.
