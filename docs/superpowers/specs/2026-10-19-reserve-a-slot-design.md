# Reserve-a-slot on top of `team/ccsph2` — design

Date: 2026-10-19 · Status: draft for approval · Supersedes the direction in
`docs/adrs/0001-reservation-scope.md` (that ADR fixed `team/ras` in place; this design rebuilds
from `team/ccsph2` instead).

Repos: `cpp-ui-hearing`, `cpp-context-hearing`, `cpp-context-progression`, `cpp-context-listing`,
`cpp-context-listing-courtscheduler` (plus `cpp-apitests`). All are released together.

---

## 1. Intent

### What is live, and what is not

| layer | status | how much freedom |
|---|---|---|
| `main` — magistrates provisional booking | in production for six years | **protect**: add to it, never change it |
| `team/ccsph2` (`team/ccsph2n` in listing) — Crown booking | built, not live | can be reshaped |
| `team/ras` — reserve-a-slot as first built | built, not live | reference only; this design replaces it |

### How `main` works today (verified in code)

- **Pick.** The UI (`magistrates.container.ts`) immediately sends `book-provisional-hearing-slots`.
  Courtscheduler's `bookProvisionalSlots` mints a fresh `randomUUID()` booking id on every call
  and writes one `provisional_booking` row per session with `active = true`. **No
  `allocated_listings` row is written, so no capacity is held.**
- **Draft.** The booking id goes into the line's `bookingReference` prompt. Every draft change,
  including this one and line deletion, ends in `saveDraftResult` and is saved to the server at once.
- **Share.** Progression resolves each booking reference to its session(s) via courtscheduler's
  `fetchProvisionalSlots` (`provisional_booking` by booking id, no `active` filter). Next hearings
  that resolve to the same session(s) are **merged** into one listing need. Courtscheduler's
  `persistHearingSlots` writes the `allocated_listings` row, decrements the session's counters and
  calls `deleteProvisionalBooking`, which sets **`active = false`** for that request's booking.
  Booking ids merged away keep `active = true`.
- **Re-share.** `releaseOldAllocatedListings(hearingId)` deletes the hearing's listing rows,
  restores capacity and sets their `provisional_booking` rows back to `active = true`. The
  re-listing in the same call sets `active = false` again.

So on `main`, `provisional_booking` is the only record of where a booking goes, from pick
onwards, and it survives every release.

### The problem

A pick holds no capacity. Between a clerk's pick and their share, other clerks can fill the same
session. Reserve-a-slot must:

1. **hold capacity from the moment of the pick**, for magistrates and Crown alike;
2. **give it back if the results are not shared by the end of the day**;
3. do so as a small addition to `team/ccsph2`, leaving `main`'s behaviour intact.

### Why not continue from `team/ras`

`team/ras` changed how a booking's session is found (reservation rows first, and it stopped
writing `provisional_booking`). Deleting a released hold then deleted the pointer, and a chain of
fixes followed: surplus release, a carry-over in progression and listing, a pre-share gate that
guessed from missing rows, booking-id reuse. Four defects came out of it (LPT-2582, 2583, 2599,
2600). Unpicking that across five repos is harder than not adding it. Starting from
`team/ccsph2` means `git diff team/ccsph2` *is* this design and can be reviewed against it.

---

## 2. Goals, non-goals, anti-goals

**Goals**
- Capacity is held from pick to share for both courts.
- Unshared holds are released at end of day; the clerk is then told their session expired.
- Releases triggered by draft edits happen when the edit is saved.
- One flow for magistrates and Crown.
- The UI-facing names the UI devs already use on `team/ras` keep working.

**Non-goals**
- Removing or renaming `provisional_booking`. Its only cost is a confusing name. The API already
  says "unconfirmed"; the table is documented, not migrated.
- Changing how a booking's session is found, or how re-share works.
- Grouping picks at pick time to anticipate the share's merge (see §3.4 for why it cannot work).
- Re-checking capacity at share.

**Anti-goals — any of these makes the work a failure even if it "works"**
- Any change to how `main` finds a booking's session or re-shares a hearing.
- A next hearing listed with no capacity behind it (overbooking).
- "Your session has expired" shown for a booking whose next hearing was listed (LPT-2599/2600).
- A next hearing silently not listed.

---

## 3. Design

### 3.1 Invariants

1. **The share never increases the capacity consumed.** Whatever the picks hold (N), the
   listings after the share consume at most N. (Agreed in place of "exactly N".)
2. **`provisional_booking` is the only source for "which session does booking X go to".** Holds
   are never read for that purpose.
3. **Every booking is in exactly one recorded state**, answerable without inference:

| state | recorded as |
|---|---|
| HELD | a live hold row for the booking |
| LISTED | `provisional_booking.active = false` |
| EXPIRED | `provisional_booking.active = true` and no hold row |
| UNKNOWN | no `provisional_booking` row |

### 3.2 Pick — both courts

The UI sends `book-unconfirmed-hearing-slots` (ras's name). Hearing forwards it to courtscheduler
as today. Courtscheduler, **in one transaction**:

1. mints a new booking id (always, as on `main`; a booking id sent by the client is ignored);
2. checks the session has room (`validateSessionAvailabilityListMode`, as ras's `reserveAll`
   does); if not, refuses with `NO_CAPACITY` and writes nothing;
3. writes the `provisional_booking` row(s), `active = true`, exactly as `main` does;
4. writes the hold(s) and decrements the session counters, exactly as a share would.

A multi-session pick (Crown multi-day) is one booking id with one `provisional_booking` row and
one hold per session. `main`'s `bookProvisionalSlots` already loops over several slots under one
id.

### 3.3 Share

Unchanged, except that `persistHearingSlots`, when a hold exists for the request's booking and
session, **turns the hold into the listing** (sets the hearing id, clears the expiry) and does
**not** decrement the counters again. Without a hold (bookings made before this release, or Crown
next hearings not booked through this flow), it behaves exactly as today.

### 3.4 Merged-away bookings

When the share merges several bookings into one listing need, only one booking is listed. The
others must not keep a hold (that would over-hold until end of day) and must not later read as
EXPIRED (that is LPT-2599/2600).

- Progression already gathers the merged parties onto the listing need. It also carries the
  **merged-away booking references** on it.
- Listing passes them through to courtscheduler's book call.
- Courtscheduler, **in the same transaction that writes the listing**, deletes their holds,
  restores capacity, and sets their `provisional_booking` rows `active = false`.

This replaces ras's `SurplusReservationReleaseService`, a separate fire-and-forget HTTP call that
deleted rows the lookup depended on.

*Why picks cannot be grouped up front instead:* at pick time courtscheduler does not know the
source hearing (hearing's `BookProvisionalHearingSlotsProcessor` sends only the slots), the next
hearing does not exist yet, and the share's merge key also depends on hearing type (cases) and a
different rule for applications. A guessed group that is wrong in one direction overbooks.

### 3.5 Release on save

When a saved draft no longer refers to a booking that the previous saved draft referred to, that
booking's **hold** is released: hold deleted, capacity restored, `provisional_booking` left
`active = true`. Re-pick and line deletion are the same case. A booking already LISTED has no hold;
nothing is released and `main`'s re-share deals with its listing.

With auto-save on `main`, this fires seconds after a delete or re-pick. The UI already calls ras's
`release-unconfirmed-hearing-slots`; keep that call. The builder must check when it fires against
this rule (see §6).

### 3.6 End-of-day release

ras's purge endpoint (`purge-expired-reserved-sessions`) and its scheduled job release holds whose
expiry date is before today: hold deleted, capacity restored, `provisional_booking` left
`active = true`. Holds are stamped with the pick date, and the job runs at 01:00 Tuesday–Saturday.
That schedule is a business requirement: picks made on Saturday or Sunday are released on
Tuesday.

### 3.7 Expiry check before share

Before sharing, the UI asks for the status of every booking on the draft (ras's booking-status
endpoint, same response shape: `{bookings: [{bookingId, safeToShare, status}]}`). Courtscheduler
answers from the recorded state in §3.1:

| state | `status` | `safeToShare` | clerk sees |
|---|---|---|---|
| HELD | `RESERVED` | true | shares |
| LISTED | `SHARED` | true | shares |
| EXPIRED | `EXPIRED` *(new value)* | false | "Your session has expired, please select another session" |
| UNKNOWN | `NOT_FOUND` | false | same message |

No line flags (`sharedDate`, `amendmentReason`), no inference from missing rows. ras's
`isSettledShare` and `bookingIdToReuse` do not come across.

### 3.8 Crown

Crown follows §3.2–3.7 exactly. ccsph2's Crown-specific handling is removed:

- the UI books a Crown pick through `book-unconfirmed-hearing-slots` instead of writing the
  session id into `bookingReference`;
- the share-time `validateSessionAvailability` call for `NHCCS` lines goes (it would count the
  line's own hold);
- progression's "a Crown reference is the session id" branch in
  `HearingToHearingListingNeedsTransformer` and listing's Crown fallback that relies on it go;
  an unresolved Crown booking is treated like any other unresolved booking.

ccsph2 is not live, so no stored data depends on the old Crown behaviour.

### 3.9 Unchanged from `main` (stated so a reviewer can check it)

`fetchProvisionalSlots` and every lookup of a booking's session; `releaseOldAllocatedListings`
and the re-share sequence; the merge in progression; `provisional_booking`'s schema and `active`
semantics for listed bookings; capacity queries.

---

## 4. Where the hold lives (my call)

**A hold is an `allocated_listings` row with no hearing behind it and an expiry date.**

Capacity is read from `allocated_listings` in ~7 places on `team/ccsph2` (availability aggregate,
slot-search totals, count per start time, AD-split totals, total duration) plus the session
counters. A hold stored there is counted by all of them **by default**; storing it anywhere else
means adding it to each, and missing one overbooks silently. The other readers:

| readers of `allocated_listings` | holds should be | default with a hearing-less row |
|---|---|---|
| capacity (~7) | counted | counted ✓ |
| "session has bookings" — delete protection, `hasHearingsBooked` (~9) | counted (a held session is in use) | counted ✓ — *confirm with product* |
| keyed on `hearing_id` (~5) — `findByHearingId`, multi-day shrink, source update | excluded | excluded ✓ |
| hearing lists and feeds (~3) — `AllocatedHearingsQueryBuilder`, `findByUpdatedOn…`, `findByCourtScheduleId` callers | excluded | **must add an exclusion** |

**Left to the builder:** `AllocatedListing.hearingId` is `nullable = false` at entity level (the
column itself is nullable). Either relax the entity and audit every Java consumer for null, or
give holds a non-hearing sentinel. Constraint either way: no hearing-keyed or hearing-listing
reader may ever see a hold, and a hold must be found by booking id. A hold is identified by its
expiry being set; `source` stays provenance only (no `RESERVED_UNCONFIRMED`-style state in it).
Changeset 052 (`expires_at`, `date`) comes across from ras; changeset 053 does not.

---

## 5. External contract

The UI devs already use ras's names, and `cpp-apitests` already uses
`book-unconfirmed-hearing-slots`. They are kept. Released together, so no aliases for the old
names.

| contract | name kept from ras | behaviour behind it |
|---|---|---|
| hearing command (UI) | `book-unconfirmed-hearing-slots` | §3.2 |
| hearing command (UI) | `release-unconfirmed-hearing-slots` | §3.5 |
| booking status (UI, via listing) | courtscheduler `/unconfirmedBooking/status` | §3.7, adds `EXPIRED` |
| courtscheduler (service-to-service) | `/unconfirmedBooking`, `create/get.unconfirmed.booking` | §3.2; lookup unchanged |
| courtscheduler | `purge-expired-reserved-sessions` | §3.6 |
| public event | `public.hearing.hearing-slots-provisionally-booked` (unchanged name) | success of §3.2 |
| progression → listing → courtscheduler | **new optional field**: merged-away booking references | §3.4 |

Also outside the five repos: the notification service and `cpp-aks-deploy` subscribe to
`public.hearing.hearing-slots-provisionally-booked`; nothing changes for them unless the separate
failure event (`tmp/ras-new-pub-event`) goes ahead. `cpp.context.rotasl` has its own
`/provisionalBooking`; it is not a caller.

---

## 6. Things to verify before or while building

1. **Merge duration.** When merged next hearings have different durations, the share appears to
   use the first one's. Holds per booking are unaffected, but the listing created from a hold must
   charge the same duration the hold charged, or capacity drifts.
2. **Applications path.** `HearingListingNeedsTransformer` (applications) merges on sessions only,
   cases also on hearing type. §3.4's merged-away references must be produced by both.
3. **Crown counting.** How a Crown hold charges duration-based and AD-split sessions, so pick and
   share charge the same counters. ras's rule skipped the pick-time capacity check for a Crown
   multi-day run because the share-time selector never rejects for capacity; decide whether that
   still holds.
4. **`release-unconfirmed-hearing-slots` timing in the UI** (`draft-result.effects.ts` on ras)
   against §3.5's "on the save that drops the booking".
5. **Bookings made before this release** have `provisional_booking` rows and no hold. Under §3.7
   they read as EXPIRED. Decide whether pre-release `active = true` bookings should read as
   HELD-equivalent (no hold expected) — likely needed for magistrates drafts in flight at go-live.
6. **"Session has bookings" semantics** with holds (§4): confirm a held session should count as
   in use for rota deletion and `hasHearingsBooked`.

---

## 7. Starting point and what crosses over from `team/ras`

Branch fresh from `team/ccsph2` (`team/ccsph2n` in listing) in each repo. `team/ras` has 69
non-version commits not in that base (UI 19, hearing 13, progression 12, listing 11,
courtscheduler 14); in hearing, ras also lacks 4 commits that `team/ccsph2` has. The first task is
to mark each one port, adapt or drop. Starting classification:

| repo | port / adapt | drop |
|---|---|---|
| UI | the pick calling `book-unconfirmed-hearing-slots`; refusal handling (`4683ccd1c`); release on draft change (`69feea64f`, `4c23ba928`), adapted to §3.5; Crown booking with a real booking id (`1c6cda30e`, `55bb44564`, `09f671e91`); expiry message; LPT-2554 and the two small fixes (`479a63cb6`, `ead1fec8c`) after inspection | `bookingIdToReuse` (`7594dbea9`); the `isSettledShare` gate (`1f1c5f3e5` and the gate parts of `782c4071f`/`4c8610471`/`eca7deecb`) |
| hearing | renamed commands and the release command (from `e2f506e1a6`); `ReusableInformationMainConverter` (`d2b048a9a9`) after inspection; the public-event split from `tmp/ras-new-pub-event` | carrying `bookingId` through the chain (`8cb87a82fa`, `67bed3d40d`, `d38255ace9`, `1543a6c74d`); docs and Jira files; pipeline and pom bumps |
| progression | — (new work: §3.4 merged-away references; §3.8 Crown branch removal) | carry-over (`416cf352c`, `baf2f541f`); `SurplusReservationReleaseService` (`4c20a31e9`…`b2838b095`); `fa7b4238c` unless inspection shows §3.4 needs it |
| listing | booking-status exposure for the UI; renamed courtscheduler calls and stub data (`247481c19b`); §3.4 pass-through (new) | carry-over fallback (`c3ef4c5e4e`, `f5f86940d6`); Crown fallback reliance (`8f4b5f24ce`) after inspection |
| courtscheduler | renamed endpoints; capacity check at reserve; purge endpoint and job (`17829e3fd`); `expires_at` changeset 052 (`f966701fe`); reservation error mapping (`5d2396427`); booking-status endpoint, logic rewritten to §3.7 | reservation-first `fetchProvisionalSlots`; no longer writing `provisional_booking`; changeset 053 (`913834923`); booking-id reuse in `reserveAll` |

Also cross over: the Serenity journeys for LPT-2582, 2583, 2599 and 2600 (offence-scoped
locators), and the real-payload unit fixture from hearing `faef00c5`.

---

## 8. Done means

- Every anti-goal in §2 has a test that would fail if it happened.
- The ported Serenity journeys (LPT-2582, 2583, 2599, 2600) pass against the new build on STE02,
  with no stranded holds afterwards.
- `git diff team/ccsph2` contains nothing outside §3–§5 and the crossed-over items in §7.
- Invariant tests: N before share ≥ N after (including a merged pair); every booking reads exactly
  one §3.1 state through pick, share, re-share, release on save and end-of-day release.
