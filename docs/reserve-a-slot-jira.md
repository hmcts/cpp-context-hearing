# Reserve a Slot — Jira reconciliation

**Date:** 10 September 2026
**Author:** Arcadius Ahouansou
**Design doc:** https://claude.ai/code/artifact/90e6b0eb-dc8a-4697-a8d4-8d9086d283d6
**Implementation plans:** `cpp-context-listing-courtscheduler/docs/superpowers/plans/2026-09-09-reserve-a-slot-courtscheduler.md`, `cpp-context-hearing/docs/superpowers/plans/2026-09-09-reserve-a-slot-hearing.md`

## Why this document exists

The board reflects an earlier design. Three in-progress tickets (LPT-2456, LPT-2474, LPT-2457) describe a **pre-minted hearing UUID** approach that was evaluated and dropped; one ready-for-test ticket (LPT-2432) describes an endpoint that has since been **deleted**. Meanwhile the implemented design, the two defect fixes it required, and all of the front-end work have no tickets.

### The design change in one paragraph

The reservation is keyed on the **`bookingId` that courtscheduler already mints** for a provisional booking, carried through the draft in the existing `bookingReference` prompt. It is *not* keyed on a pre-minted next-hearing UUID. That removes the need for hearing to invent a hearing id, for a CoreDomain `NextHearing` change, and for any progression change at all. It works because Crown's use of `bookingReference` is not yet in production — verified on `origin/main` in both `cpp-ui-hearing` and `cpp-context-listing` — so the field's meaning can be unified across jurisdictions with nothing to stay compatible with.

### Two things to raise with people today, before ticket housekeeping

1. **LPT-2432 (Somanagouda) is READY FOR TEST but its endpoint has been deleted.** `PUT /sessions/{sessionId}/hearings/{unconfirmedHearingId}` had no caller in any repo; its logic now lives in `ReservationService`, reached through `POST /provisionalBooking`. Testing it will fail because it is gone, not because it is broken.
2. **LPT-2506 (Somanagouda) is READY FOR TEST but the purge it calls was defective.** Until the fix below, the purge deleted the `allocated_listings` row without restoring `court_schedule` capacity. A test would have looked like a pass while permanently burning one slot per expired reservation. It needs retesting after the fix.

---

---

## Jira keys

Created in project **LPT**, all parented to epic **LPT-2430**. The 31 internal refs used
throughout this document were consolidated into 19 tickets; several tickets therefore cover
more than one ref.

All 19 exist and have been verified against the board, summary by summary.

| Internal ref | Jira |
|---|---|
| `BUG-1` | LPT-2528 |
| `BUG-2` | LPT-2529 |
| `BUG-3` | LPT-2531 |
| `BUG-4` | LPT-2539 |
| `BUG-5` | LPT-2532 |
| `BUG-6` | LPT-2532 |
| `BUG-7` | LPT-2525 |
| `BUG-8` | LPT-2526 |
| `BUG-9` | LPT-2533 |
| `BUG-10` | LPT-2524 |
| `NEW-3` | LPT-2530 |
| `NEW-4` | LPT-2530 |
| `NEW-5` | LPT-2530 |
| `NEW-6` | LPT-2530 |
| `NEW-7` | LPT-2530 |
| `NEW-7a` | LPT-2530 |
| `NEW-8` | LPT-2535 |
| `NEW-9` | LPT-2537 |
| `NEW-10` | LPT-2538 |
| `NEW-11` | LPT-2539 |
| `NEW-12` | LPT-2540 |
| `NEW-12a` | LPT-2536 |
| `NEW-13` | LPT-2539 |
| `NEW-14` | LPT-2539 |
| `NEW-15` | LPT-2542 |
| `NEW-16` | LPT-2527 |
| `NEW-17` | LPT-2534 |
| `TEST-1` | LPT-2541 |
| `TEST-2` | LPT-2541 |
| `TEST-3` | LPT-2541 |
| `TEST-4` | LPT-2541 |

Tickets covering more than one ref:

| Jira | Covers |
|---|---|
| LPT-2530 | NEW-3, NEW-4, NEW-5, NEW-6, NEW-7, NEW-7a — the reservation endpoints |
| LPT-2532 | BUG-5, BUG-6 — the 403 and the test-reader gap, fixed together |
| LPT-2539 | NEW-11, NEW-13, NEW-14, BUG-4 — the front-end reserve-and-gate work |
| LPT-2541 | TEST-1, TEST-2, TEST-3, TEST-4 — coverage and access-control guards |

---

## Tickets to create — working checklist

**33 tickets to raise.** Everything below is a *proposal*; none of it exists in Jira yet. The only tickets that already exist are the `LPT-*` ones in the next section, which need **status changes, not creation**.

Nothing here is committed to any branch, so set every "done" ticket to your equivalent of *In Review / Ready for PR* rather than *Ready for Test* — see the caveat at the foot of this document.

### Create first — not yet implemented, and someone needs to pick them up

| Ref | Title | Type | Priority |
|---|---|---|---|
| **BUG-10** | A refused reservation is treated as a successful pick | Bug · [FE] | **Highest — do before STE testing.** Latent until BUG-9; now reachable |
| **NEW-16** | Let the session picker refuse sessions that are already full | Story · [FE] | Nice to have. **Owner: `@cpp/scheduling` team** |
| **BUG-7** | `hearing.replicate-shared-results` has no access-control rule | Bug · [BE] | **Not ours.** Pre-existing; raise with that feature's owners |
| **BUG-8** | Four `listing.command.*` actions have no access-control rule | Bug · [BE] | **Not ours.** Pre-existing; raise with that feature's owners |

BUG-7 and BUG-8 rest on **static evidence only** — no runtime 403 has been demonstrated, unlike BUG-5. Say so in the ticket; for courtscheduler's judiciary actions the same signal turned out to be benign.

### Create as done — implemented, uncommitted, awaiting commit and PR

**Defects found in already-merged work** — these belong to their authors, not folded silently into new work:

| Ref | Title | Repo |
|---|---|---|
| BUG-1 | Purge of expired reservations does not restore session capacity | courtscheduler |
| BUG-2 | Reservations returned as listed hearings, breaking calendar pagination | courtscheduler |
| BUG-3 | The reservation is never released on the path listing actually uses | courtscheduler + listing |
| BUG-4 | The Crown pre-share check breaks when NEW-11 lands | cpp-ui-hearing |
| BUG-5 | `courtscheduler.get.booking-status` returned 403 to every caller | courtscheduler |
| BUG-6 | `DatabaseReader` never mapped `booking_id` | courtscheduler |
| BUG-9 | Reserve-a-slot removed the only capacity check and did not replace it | courtscheduler |

**Feature work:**

| Ref | Title | Repo |
|---|---|---|
| NEW-3 | Provisional booking creates capacity-holding, expiring reservations | courtscheduler |
| NEW-4 | Resolve booking ids from reservations, with a permanent legacy fallback | courtscheduler |
| NEW-5 | Release the reservation when a booking is confirmed | courtscheduler |
| NEW-6 | Accept `duration` on the provisional booking request | courtscheduler |
| NEW-7 | Endpoint to report whether a booking still has a live hold | courtscheduler |
| NEW-7a | Booking status must tell an expired hold from an already-shared booking | courtscheduler |
| NEW-15 | Re-picking a session gives back the one the clerk abandoned | courtscheduler + hearing |
| NEW-17 | A Crown multi-day pick holds every day, not just the anchor | courtscheduler |
| NEW-8 | Carry slot `duration` through to courtscheduler | cpp-context-hearing |
| NEW-12a | Release command so the browser can give a hold back | cpp-context-hearing |
| NEW-9 | Resolve Crown's `bookingReference` as a booking id | cpp-context-listing |
| NEW-10 | Expose the pre-share reservation check to the UI | cpp-context-listing |
| NEW-11 | Reserve the session at the moment the clerk picks it | cpp-ui-hearing |
| NEW-12 | Release the hold when the clerk changes their mind | cpp-ui-hearing |
| NEW-13 | The three clerk messages | cpp-ui-hearing |
| NEW-14 | Gate the share on the hold still existing | cpp-ui-hearing |

**Test and guard work** — worth their own tickets, because each exists in response to a defect that escaped review:

| Ref | Title | Repo |
|---|---|---|
| TEST-1 | Integration coverage on the path listing actually uses | courtscheduler |
| TEST-2 | RAML↔Drools access-control guards | all three backends |
| TEST-3 | The purge test proves capacity comes back | courtscheduler |
| TEST-4 | Label the fail-open test's expected server error | cpp-context-listing |

### Do not create

| Ref | Why |
|---|---|
| **DEC-1** | Resolved. Delivered as NEW-7a. Keep in this document as the record of the decision |
| **DEC-2** | Superseded by BUG-9 — the premise was wrong. Not a product decision after all |

### Two that need a conversation, not a ticket

- **Deployment order for BUG-5 / NEW-7a / NEW-10**: courtscheduler must ship before hearing starts sending `bookingId`, because `ProvisionalSlotConverter` uses a bare `ObjectMapper` with `FAIL_ON_UNKNOWN_PROPERTIES` on. Put it in the release notes, not a ticket.
- **The always-assign rule** that NEW-17 follows — worth confirming with the Crown search-and-book team. If it is wrong, both paths change together, never just ours.

---

## Existing tickets — recommended action

| Ticket | Title (abbreviated) | Current | Recommended | Action |
|---|---|---|---|---|
| LPT-2431 | Add `expires_at` to allocated listing table | READY FOR TEST | **No change** | Keep. Foundation for everything else. Note the column ended up as `date`, not a timestamp — deliberate, the purge only cares whether today has moved past it. |
| LPT-2432 | New endpoint for reserving unconfirmed sessions | READY FOR TEST | **Close — Superseded** | The endpoint is deleted. Its logic survives in `ReservationService` and is exercised via `POST /provisionalBooking`. Link to NEW-3. Do not close as "Won't Do" — the work was used, just relocated. |
| LPT-2433 | New endpoint for purging expired reserved sessions | READY FOR TEST | **Keep, but blocked** | The endpoint is correct; its implementation was not. Block on BUG-1 and retest after. |
| LPT-2456 | Hearing to create new hearing UUID on first draft save | IN PROGRESS | **Close — Won't Do** | Abandoned design. Hearing does not mint a hearing id; courtscheduler's existing `bookingId` is the reservation key. |
| LPT-2474 | Propagating new hearing ID to progression; possible CoreDomain change | IN PROGRESS | **Close — Won't Do** | No CoreDomain change is needed. `bookingReference` already exists on `NextHearing` and already carries a booking id for magistrates. This is the single largest saving from the design change. |
| LPT-2457 | Progression to create unconfirmed hearing using hearing's uuid | IN PROGRESS | **Close — Won't Do** | Progression needs **no change**. It already passes `bookingReference` through to listing, and already stores the `bookingId → courtScheduleIds` map that makes re-share work. |
| LPT-2458 | Need to book a slot for the unconfirmed hearing | NEW | **Rewrite and split** | This is the only ticket covering the real work, and it currently spans four repos. Replace with NEW-3 … NEW-9 below. Keep as an epic if the board supports it. |
| LPT-2506 | Schedule job at 1AM to purge expired unconfirmed bookings | READY FOR TEST | **Unblocked — ready for retest** | Was blocked on BUG-1, now fixed. The patch has since been verified field by field: `baseUri` matches 11 existing shipped courtscheduler patches; `resource: /sessions` matches the RAML resource owning the purge `post:`; `mediaType` matches the RAML action name; the action has a Drools rule, so it cannot hit BUG-5's failure mode; cron `0 0 1 * * ?` is 01:00 daily. A mismatch in any of those would have failed the way BUG-5 did — silently, nightly. |

---

## New tickets — defects in already-merged work

These are in code delivered under LPT-2432/2433, so they belong to their authors rather than being folded silently into new work.

### BUG-1 — [BE] Purge of expired reservations does not restore session capacity
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

`AllocatedListingRepository.deleteExpiredReservedSessions` was a bare `DELETE FROM allocated_listings WHERE expires_at < :cutoff`. Session capacity is materialised on `court_schedule` (`available_slots` / `available_duration`), so deleting the row left the capacity consumed. Every expired reservation permanently burned a slot, silently, with nothing reporting it.

**Fix:** replaced with `releaseExpiredReservations(LocalDate)`, performing the same three-step release `releaseOldAllocatedListings` uses — delete the rows, `releaseCourtScheduleAllocatedSlotsForBookingId`, `releaseAllocatedSlotsOrDurationFromCourtSchedule`.

**AC**
- After the purge, the row is gone **and** `available_slots` is back to its pre-reservation value
- A reservation expiring *today* survives until the day rolls over (strict `<`)
- Confirmed bookings (`expires_at IS NULL`) are never touched
- A backlog from a missed run is swept in one pass

**Blocks:** LPT-2433, LPT-2506

### BUG-2 — [BE] Reservations are returned as listed hearings, breaking court calendar pagination
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

`AllocatedHearingsQueryBuilder` had no `expires_at` filter, so reservations were returned by the allocated-hearings query behind listing's `range-search` and the **cpp-ui-listing court calendar**. A reservation's `hearing_id` is a `bookingId` matching no hearing, so listing drops the row — but courtscheduler's total is passed straight through, so the pager counts rows that never render. Pages come back short and the total overstates.

**Fix:** `and al.expires_at is null` in the main `where` and in the `al2` day-count subquery.

**Deliberately not changed:** availability and capacity reads (`getAllocatedListingsByCourtScheduleId`, `getCountBasedAllocatedListing`, `totalbooked`, the `hasHearingsBooked` joins) and the three session-delete guards must keep counting reservations as booked — a hold that did not reduce availability would not be a hold.

**AC**
- A reserved session does not appear as a listed hearing in allocated-hearings queries
- Court calendar pagination totals match rendered rows
- Session availability still reflects reservations as booked

### BUG-3 — [BE] The reservation is never released on the path listing actually uses at share
**Status: implemented, uncommitted (courtscheduler `team/ras` + cpp-context-listing `team/ccsph2`)** · reviewed, approved

NEW-5 released the hold in `CourtScheduleRepositoryImpl.persistHearingSlots`. Listing's share flow never reaches that method: it posts `courtscheduler.list.hearings-in-sessions`, which lands on `SlotsUpdateService.listHearingSlots` → `updateListHearingSlots`, and that released only by the **real hearing id**. The booking-keyed reservation therefore survived the share, and the session stayed decremented **twice** — once for the hold, once for the confirmed listing — until 01:00 the next morning. The clerk sees a session losing two slots for one hearing.

Found because `allocated_listings` already carries a `booking_id` column that nothing on this path was writing.

**Fix, two halves that must ship together:**
- *courtscheduler* — `bookingId` added to the `list.hearings-in-sessions` request (optional, beside `hearingId`, not inside `courtScheduleIds[]`); `updateListHearingSlots` releases each distinct booking-keyed hold **once, before** the per-session loop, then stamps `booking_id` on the confirmed row.
- *cpp-context-listing* — `getUpdateSlotsPayload` / `listHearingSessionsAndExtractData` take a nullable `bookingReference` and send it; the key is omitted when null.

**AC**
- Capacity nets to exactly one decrement: reserve −1, release +1, list −1
- Absent `bookingId` (Crown fallback, search-and-book, rota) behaves exactly as before
- An unknown `bookingId` is a no-op, not an error
- `isCourtScheduleReleased` keeps its own meaning — it asks whether *this hearing* was previously listed, and feeds `resolveAllocatedListingSource`
- `persistHearingSlots` unchanged: its release is correct for the `update.hearing.slots` path
- The confirmed row carries its `booking_id`, which is what makes DEC-1 answerable

---

### BUG-5 — [BE] `courtscheduler.get.booking-status` returned 403 to every caller
**Status: fixed, uncommitted (courtscheduler `team/ras`)** · found by integration test, not review

NEW-7a's endpoint was declared in the RAML and implemented correctly, but had **no Drools access-control rule** — the only one of 29 actions without one. An action with no rule is refused for every caller, including the system user. So the endpoint, NEW-10's pass-through over it, and the entire pre-share gate were unreachable.

**Worse than a plain outage.** NEW-10 fails open on any non-200, answering `{status: "UNKNOWN", safeToShare: true}`. The gate would therefore have looked like it was working while silently never checking anything — nobody notices until a clerk shares into an expired hold.

**Fix:** one rule, modelled verbatim on the sibling `courtscheduler.get.provisional.booking`, using `SecurityGroupConstants.systemUserRoles()` — listing calls it service-to-service, not from a browser.

**How it was missed:** two code reviews examined logic and contracts; neither looked at access control on the courtscheduler side. Access control *was* planned and reviewed for listing in NEW-10, and the same thought was never applied to courtscheduler. Only running the endpoint end-to-end found it. **TEST-2 exists so this cannot recur.**

**AC**
- The endpoint is reachable by the same callers as `get.provisional.booking`
- The rule's action name matches the RAML byte-for-byte
- No other reserve-a-slot action lacks a rule (verified by enumeration in review)

---

### BUG-6 — [BE] `DatabaseReader` never mapped `booking_id`
**Status: fixed, uncommitted (courtscheduler `team/ras`)** · integration-test utility only, no production impact

`resultSetToAllocatedListing()` built `AllocatedListing` objects without reading the `booking_id` column, so every row it returned had a null `bookingId`. No integration test could assert the stamped booking id — which is exactly what BUG-3's test needs, since `booking_id` on a confirmed row is what distinguishes an already-shared booking from an expired one.

---

### BUG-7 — [BE] `hearing.replicate-shared-results` has no access-control rule
**Status: open — pre-existing, NOT part of reserve-a-slot, needs raising with its owners**

Found while building TEST-2's guard for hearing, and reported rather than fixed because it belongs to another feature.

The action is declared in `hearing-command-api.raml`, is reachable via `@Handles("hearing.replicate-shared-results")` at `HearingCommandApi.java:352`, and **no `.drl` in the repo mentions it**. There is no catch-all rule — all ~71 rules are keyed on a specific `hearing.*` action name. On that evidence it is refused for every caller, the same shape as BUG-5.

**Not proven at runtime.** Unlike BUG-5, no integration test has demonstrated the 403; the conclusion rests on static evidence. Worth confirming before acting.

It is allowlisted in TEST-2's hearing guard so that test passes on arrival, with a comment marking it a suspected defect rather than an intentional exemption — and TEST-2's stale-allowlist assertion will fail once a rule is added, forcing the allowlist entry to be removed.

---

### BUG-8 — [BE] Four listing command-API actions have no access-control rule
**Status: open — pre-existing, NOT part of reserve-a-slot, needs raising with its owners**

Found by extending TEST-2's measurement to listing's **command** API, which TEST-2 had not covered (it guarded the query API only). Same shape as BUG-5 and BUG-7.

These four actions are declared as REST endpoints in `listing-command-api.raml` — each with a `(mapping)` carrying a `requestType` and `name` — and are handled via `@Handles` in `ListingCommandHandler`:

- `listing.command.add-offences-for-hearing`
- `listing.command.delete-offences-for-hearing`
- `listing.command.update-offences-for-hearing`
- `listing.command.update-defendants-for-hearing`

None appears anywhere in `listing-command-api.drl` under any spelling, and that file has **no catch-all rule** — every one of its 28 rules is keyed on a specific `listing.*` action name. On that evidence all four are refused for every caller.

**Not proven at runtime.** Like BUG-7 and unlike BUG-5, this rests on static evidence; no integration test has demonstrated the 403. Worth confirming before acting — but worth acting on, because the failure mode is silent for anyone who does not happen to exercise these commands.

**Measured:** 31 RAML actions, 28 DRL rules, 4 actions with no rule (the arithmetic does not net out because some rules cover actions declared elsewhere).

---

### BUG-9 — [BE] Reserve-a-slot removed the only capacity check and did not replace it
**Status: FIXED, uncommitted (courtscheduler `team/ras`)** · 29/29 unit · **ProvisionalBookingIT 8/8** · raised by the user challenging DEC-2's framing

**Fix:** `ReservationService.reserveAll` now calls the **existing** `SessionsService.validateSessionAvailabilityListMode` before `saveBookedSlots`, throwing `NoCapacityException` (already mapped to 409 by `CourtSchedulerApi`). The rule is reused, not copied, so the per-session `isOverbookingAllowed` exemption keeps working and cannot drift.

**The subtlety that made this non-trivial.** `reserveAll` supports re-picking under the same bookingId, and the previous pick's rows are released *inside* `saveBookedSlots` — after any check. A naive check therefore counts the clerk's own hold against them and falsely rejects a legal re-pick: a 1-slot session the clerk already holds reads as `1 >= 1`. The fix skips validation for sessions the bookingId already holds, derived from the fetch `guardAgainstConfirmedAllocation` already performs. Verified end-to-end by the existing re-pick integration test.

**Overbooking was prevented before these changes.** `SessionsService.validateListModeSlotBased` compares `totalBooked >= maxSlots` and rejects with *"One or more schedules are no longer available, please reschedule your hearing"* — skipping only sessions whose `isOverbookingAllowed` flag is true. Overbooking was a deliberate, per-session, configurable exception. The UI reached that check by calling `validateSessionAvailability` at share time.

**BUG-4 removed it.** Task 1 replaced `validateSessionAvailability` with the new booking-status gate. That gate asks *"is the hold still live?"* — a better question for expiry, and the right fix for the courtScheduleId/bookingId mismatch — but it asks **nothing about capacity**. `validateSessionAvailability` is now dead code in the UI, which Task 1's report noted as tidy-up; its significance as the only capacity guard was missed.

**The reserve path has never enforced capacity, verified twice.** `getUpdatedAllocatedSlots` only resolves the schedule; `CourtScheduleCriteria.createFetchCourtScheduleEitherByidOrFiltersCriteria` filters on id / ouCode / date / room / active and never on `available_slots`; `bookSlotsWithCourtScheduleId` returns `FAILED` only when the schedule cannot be **resolved**. So `NoCapacityException` is unreachable today.

**Net effect on Crown NHCCS:** pick a session that is already full → the reservation succeeds and drives `available_slots` negative → at share the status is `RESERVED`, `safeToShare: true` → the share proceeds. Pre-change the same overbooking was blocked at share.

**Fix:** perform the `validateListModeSlotBased` comparison inside the reserve path, honouring `isOverbookingAllowed` exactly as the old check did. That makes `NoCapacityException` reachable, makes the "No session available" clerk message fire, and restores the pre-existing guarantee at a *better* point in time — the clerk learns at pick, not hours later at share.

**Also fixes the unreachable branch** the front-end review found: `SESSION_NOT_AVAILABLE` is dead today precisely because nothing can report "full".

**Correction to the record.** DEC-2 was written as *"is capacity enforced on reserve, or is overbooking allowed?"*, implying enforcement never existed. That framing was wrong and is superseded by this ticket. The real question is narrower: reserve-a-slot moved the guarantee from share-time to pick-time and did not implement it at the new point.

---

## New tickets — courtscheduler

### NEW-3 — [BE] Provisional booking creates capacity-holding, expiring reservations
**Status: implemented, uncommitted (courtscheduler `team/ras`)** · replaces the core of LPT-2458 · supersedes LPT-2432

`POST /provisionalBooking` mints one `bookingId` and reserves **every** requested session under it through `saveBookedSlots` — the same pipeline a real booking uses — stamping `expires_at` and `source = RESERVED_UNCONFIRMED`. It no longer writes `provisional_booking` rows.

**AC**
- One `bookingId` covers all N sessions; each session's capacity is decremented once
- All-or-nothing: if any session cannot be held, none is
- No `provisional_booking` row is written
- Path, media type and response (`{"bookingId": "..."}`) unchanged, so cpp-context-hearing needs no redeploy in lockstep
- `slotBased` is taken from the session, never from the caller

**Implementation note worth preserving in the ticket:** the reservation must be **one** `saveBookedSlots` call carrying all slots, not one call per slot. `saveBookedSlots` begins with a hearing-wide `releaseOldAllocatedListings(hearing_id)`, and for a reservation `hearing_id` *is* the `bookingId` — so a per-slot loop makes each reserve release the previous ones, and a three-day pick ends up holding one day. This was found in final review, not by any test.

### NEW-4 — [BE] Resolve booking ids from reservations, with a permanent legacy fallback
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

`GET /provisionalBooking?bookingIds=` resolves each id from reservations, falling back to legacy `provisional_booking` rows.

**AC**
- Fallback is **per booking id**, not all-or-nothing across the batch — one request may legitimately mix a new booking with a legacy one
- Response shape unchanged, so **cpp-context-listing needs no change on this path**
- Legacy rows resolve regardless of their `active` flag
- The fallback is permanent: draft results have no TTL, so a pre-go-live magistrates draft may be shared at any future point

### NEW-5 — [BE] Release the reservation when a booking is confirmed
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

At share, listing books the hearing under its **real hearing id** — a different key — so the pipeline's hearing-wide release never touches the reservation. `persistHearingSlots` now releases it explicitly before booking.

**AC**
- The session is decremented exactly once overall across reserve → confirm, never twice
- Release is a **no-op when nothing is found** — legacy drafts have only a `provisional_booking` row, and if this throws, every pre-go-live draft fails at share
- `deleteProvisionalBooking` is retained alongside, for legacy bookings

**Also fixed here (found in final review):** `SlotsUpdateService.update`'s booking-based branch read legacy `provisional_booking` and threw `ProvisionalSlotNotFoundException` when empty — always, once NEW-3 stopped writing those rows. It now resolves reservations first, legacy second. Without this, a reservation-backed booking could never be confirmed at all.

### NEW-6 — [BE] Accept `duration` on the provisional booking request
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

Reserving decrements capacity, and a duration-based session decrements `available_duration` by the hearing's estimated minutes. The request carried only `courtScheduleId` and `hearingStartTime`.

**AC**
- `duration` (integer minutes) is **optional** and additive — existing callers unaffected
- `isSlotBased` is **not** added; courtscheduler derives it from the session
- A duration-based session with no usable duration is **rejected with 400** rather than silently held as a zero-minute hold

### NEW-7 — [BE] Endpoint to report whether a booking still has a live hold
**Status: implemented, uncommitted (courtscheduler `team/ras`)**

`GET /provisionalBooking/status?bookingIds=` → `{"bookings":[{"bookingId":"...","live":true}]}`. Needed because sharing is asynchronous: the share command returns 202 and results are recorded before listing discovers the hold is gone, so the check must happen before the command is sent.

**AC**
- A legacy `provisional_booking` row counts as **live** — otherwise every pre-go-live magistrates draft is blocked at share with a false expiry message
- `live: false` means "no hold found"

**Known limitation, documented in the method's Javadoc — see DEC-1:** after a successful share, a reservation-backed `bookingId` reports `live: false`, indistinguishable from expiry.

---

### NEW-7a — [BE] Booking status must tell an expired hold from an already-shared booking
**Status: implemented, uncommitted (courtscheduler `team/ras`)** · resolves DEC-1 · **response-contract change to NEW-7 — needs its own retest**

NEW-7 shipped `GET /provisionalBooking/status` returning `{"bookingId", "live"}`, where `live` honestly means *"a hold exists"*. The pre-share gate needs a different question answered — *"is this draft safe to share?"* — and the two diverge on exactly one case: after a share the hold is gone, so `live: false`, but re-sharing is perfectly safe. A gate wired to `live` would refuse every re-share. That was DEC-1.

**How it is answered without tombstones.** The two row shapes are structurally disjoint, so one derived finder separates them with no `expires_at` filter at all:

| Row | `hearing_id` | `expires_at` | `booking_id` |
|---|---|---|---|
| Reservation (`ReservationService.toReservedSlot`) | the bookingId | non-null | **NULL** — never stamped |
| Confirmed, at share (`saveAllocatedListing`) | the real hearing id | NULL | **the bookingId** |

`findByBookingId` therefore returns confirmed rows only. Verified in review: `toReservedSlot` is the only production caller of `setExpiresAt` anywhere, and it never calls `setBookingId`.

**The change.** `live` becomes **`safeToShare`**, joined by a four-valued `status`. `live` is renamed rather than kept because a caller could reasonably read it alone and get DEC-1's bug straight back; nothing consumes the endpoint yet (NEW-10 and NEW-14 are unstarted), so the rename is free today and impossible later.

| `status` | Meaning | `safeToShare` |
|---|---|---|
| `RESERVED` | A reservation still holds capacity; not yet shared | `true` |
| `SHARED` | A confirmed row carries this `booking_id` — already shared, hold correctly released by BUG-3 | `true` |
| `LEGACY` | A pre-reserve-a-slot `provisional_booking` row, active or not | `true` |
| `NONE` | Nothing found — the hold expired and was purged | `false` |

**AC**
- An already-shared booking reports `SHARED`, never `NONE`
- `RESERVED` outranks `SHARED` and short-circuits: if both rows somehow existed the clerk still holds capacity, and reporting `SHARED` would wave a second share through against a live hold
- A legacy magistrates draft still reports safe — `findByBookingIdIn` deliberately does not filter on the `active` flag, so a soft-deleted row still answers
- `safeToShare == (status != NONE)`; the four values are exactly those four
- `isReservation` unchanged — it is shared with `fetchProvisionalSlots`
- `CourtSchedulerApi` needs no change; it passes the JsonObject through

**Depends on:** BUG-3, which is what makes the confirmed row carry `booking_id` in the first place.
**Unblocks:** NEW-10, which becomes a pass-through of this shape.

---

## New tickets — cpp-context-hearing

### NEW-8 — [BE] Carry slot `duration` through to courtscheduler
**Status: implemented, uncommitted (cpp-context-hearing `team/ccsph2`)** · full reactor build green

One optional `Integer duration` threaded along the existing path: command-api schema → `ProvisionalHearingSlotInfo` → `BookProvisionalHearingSlots` event → `BookProvisionalHearingSlotsProcessor` → courtscheduler.

**AC**
- Optional end to end; nullable in Java, absent from every `required` list
- Hearing is a pure **pass-through** — no defaulting, deriving, clamping or validating. courtscheduler distinguishes *absent* (400 on duration-based) from *zero* (a zero-minute hold), and collapsing that distinction would be a defect
- The event's field-by-field map reconstruction preserves it (this is the event-replay path; a field not read there is dropped with no error)
- The processor **omits** the key when null — `JsonObjectBuilder.add` throws on null
- `BookProvisionalHearingSlotsCommandHandler` needs no change; verified against the same `JsonObjectToObjectConverter` it delegates to

**Priority note:** NEW-6 rejects a duration-based reservation with no duration. Until NEW-8 ships, **every pick on a duration-based session fails with 400**. These two must go together.

### NEW-12a — [BE] Release command so the browser can give a hold back
**Status: implemented, uncommitted (cpp-context-hearing `team/ccsph2`)** · reviewed, approved · 345/345 · all three abandon paths covered

NEW-12 was written as front-end work, but there is no release path from the browser: courtscheduler has `DELETE /sessions/{bookingId}`, listing does not proxy it, and hearing had no release command — so nothing between the browser and that endpoint could reach it. This ticket supplies the missing back end.

`hearing.release-provisional-hearing-slots` → command handler → event → processor → `ProvisionalBookingService.releaseSlots(bookingId)` (`DELETE /sessions/{bookingId}`), with the subscription, event schema, Drools rule and messaging RAML that go with it.

**AC**
- Release is **best-effort and never fails the caller** — a connection error or a 404 is swallowed and logged, because "nothing to release" is a normal outcome on re-pick
- Idempotent: releasing an already-released or never-reserved booking is a no-op
- The nightly purge remains the backstop

**Blocks:** NEW-12 (the FE wiring)

---

## New tickets — cpp-context-listing

### NEW-9 — [BE] Resolve Crown's `bookingReference` as a booking id
**Status: implemented, uncommitted (cpp-context-listing `team/ccsph2n`)** · reviewed, approved · module suite and full `mvn clean install` green

`promoteCrownBookingReferenceToBookedSlot` currently treats `bookingReference` as a `courtScheduleId`. It becomes the same `getCourtSchedulesByProvisionalBookingId` resolution magistrates already uses. Its `RotaSlot` building, `isDraft` handling and fail-fast are untouched.

**AC**
- Crown and magistrates resolve one identity through their own enrichment paths
- `handleProvisionalBookingCase` unchanged
- Closes a latent inconsistency: `Hearing.java` already emits `.withBookingId(bookingReference)` on `HearingAllocatedForListingV2`, so today every downstream consumer receives a court-schedule id labelled `bookingId` for Crown hearings

### NEW-10 — [BE] Expose the pre-share reservation check to the UI
**Status: implemented, uncommitted (cpp-context-listing `team/ccsph2n`)** · reviewed, approved, no findings · full reactor build green

Surface NEW-7a on listing's query API so the results UI can gate the share synchronously. Now a thin pass-through: courtscheduler already answers the whole question, so listing forwards the shape unchanged.

**AC**
- Passes `{bookingId, safeToShare, status}` through verbatim — listing adds no interpretation of its own
- A legacy magistrates draft must **not** be reported as expired (courtscheduler's `LEGACY` covers this; listing must not collapse it)
- The gate blocks on `safeToShare == false` only

**Shape as built.** `POST /bookingStatus` → `{"bookingIds":[…]}`, answering `{"bookings":[{bookingId, safeToShare, status}]}`. POST in, GET out to courtscheduler — the same idiom as the neighbouring `courtScheduleDraftStatus`, avoiding a URL-length ceiling on a list that grows with the result lines in a draft. Access is the six UI groups used by `listing.validate.session.availability`, deliberately not the `SYSTEM_USERS` its other neighbour uses: a clerk in a browser calls this.

**Fails open, deliberately the opposite of its neighbour.** When courtscheduler cannot be reached, every requested id returns `{status: "UNKNOWN", safeToShare: true}`. `getCourtScheduleDraftStatus` fails *safe* because leaking a phantom courtroom is worse than dropping room info; here the consequences invert — failing closed would block every share in the building during a blip, for a check that is advisory. `UNKNOWN` is reported rather than hidden behind `safeToShare: true`, so the UI can say "could not check" instead of implying it checked. **courtscheduler never emits `UNKNOWN`** — it exists only in listing's schema.

**No machine check on the contract.** This module has no `*RamlConfigTest`, so nothing verifies that the RAML action name, the JAX-RS media types and the Drools rule agree. A mismatch would 404/406 at runtime with every unit test still green. Verified by eye in review; worth a config test of its own one day.

---

### BUG-10 — [FE] A refused reservation is treated as a successful pick
**Status: open — RECOMMENDED NEXT. Latent until today; BUG-9 made it reachable.**

**`public.hearing.hearing-slots-provisionally-booked` is published for BOTH outcomes.** `BookProvisionalHearingSlotsProcessor` sends that event name with `{bookingId}` on success and `{error}` on failure. The UI's `ProvisionalBookingService` waits on it via `commandSync(successEvent: ...)`, so a **refusal resolves as a success** carrying no `bookingId`.

Both pickers then map it unguarded — `map(({ bookingId }) => ... bookingReference: bookingId)` — so on refusal the clerk is redirected as though the pick worked and the draft is written with `bookingReference: undefined`, while **nothing is reserved**.

**Why it matters now.** Before BUG-9 nothing ever refused a reservation, so this was near-unreachable. BUG-9 makes a full session return 409, which becomes the `error` payload. Picking a full session now silently appears to succeed.

**Root cause of the miss (ours).** NEW-11's plan required that "a failure must simply not write the prompt", assuming `bookProvisionalHearingSlots` would error on failure. It does not: the failure is not transport-level, it is a successful event with a different payload shape. The plan's assumption was wrong and the implementation faithfully followed it.

**Fix**
- `ProvisionalBookingService.bookProvisionalHearingSlots` should treat an `error` payload as an error — map it to a thrown/errored observable rather than returning a `bookingId`-less success.
- Both pickers must then not write the prompt and not redirect on failure.
- Surface it to the clerk. BUG-9 revives `MANAGE_HEARING.SESSION_NOT_AVAILABLE`, which is the natural message and is already wired to an existing `<pdk-alert>`; this is the pick-time half of NEW-13.

**AC**
- A refused reservation writes no prompt and performs no redirect
- The clerk is told the session is no longer available, at pick time
- A successful reservation is unchanged
- Covered by a test that fails against the current unguarded `map`

**Worth considering separately:** publishing distinct event names, or a `success` discriminator, would make this class of bug impossible rather than guarded against. That is a backend contract change and its own ticket.

---

### NEW-17 — [BE] A Crown multi-day pick holds every day, not just the anchor
**Status: implemented, uncommitted (courtscheduler `team/ras`)** · `SlotsUpdateServiceTest` 93/93 · `ReservationServiceTest` 20/20 · `ProvisionalBookingIT` 8/8 · **user's design decision**

**The gap this closes.** A Crown multi-day pick reserved only its **anchor** day. Days 2..N were discovered by a consecutive-session search at share time, possibly hours later — so a five-day trial held one day, and the other four could be taken by another clerk in the meantime. That made the "reservation" misleading precisely where it mattered most: the longest, hardest-to-place listings.

**What changed.** `ReservationService.reserveAll` now detects a Crown multi-day pick (duration-based, jurisdiction CROWN, requested duration over one court day of 360 minutes; `daysNeeded = duration / 360`, the same arithmetic `crownDaysNeeded` and `validateListModeMultiDay` use) and expands it into the full run before reserving. Every day is reserved under the one bookingId, each row stamped with its own `expires_at`.

**Resolved by the same code the share path uses.** `findConsecutiveSessions(anchor, daysNeeded)` followed by the run-selection logic, which was **extracted** from `SlotsUpdateService` into a shared `ConsecutiveSessionSelector` rather than copied. This is the crux: if pick and share resolved the run differently, the reservation would hold days the share never uses while the days it does use were never held — defeating the ticket entirely. The extraction was proven behaviour-preserving (93/93 before and after, no test changes required).

**One `saveBookedSlots` call for the whole run.** Every slot shares the bookingId as its `hearing_id`, and `saveBookedSlots` opens with a hearing-wide release keyed on that id — so reserving day by day would make each day release the previous ones and a five-day pick would end up holding one day. This is the same C1 trap the original single-day reservation hit; the loop is structurally forbidden, not merely avoided.

**Capacity rule: never blocks — deliberately NOT BUG-9's refusal.** For Crown multi-day the reservation refuses only **structurally** (too few session days in the run, or non-consecutive dates); a day that is full is reserved and overbooked, logged advisorily. See the note below on why. BUG-9's capacity refusal continues to apply to **slot-based and single-day** picks.

**AC**
- A Crown multi-day pick reserves all N days, each with `expires_at`
- Pick and share resolve the identical run (shared selector, not a copy)
- Exactly one `saveBookedSlots` call carries the whole run
- A structurally impossible run throws `NoCapacityException` and reserves nothing
- Single-day Crown, and all slot-based picks, are unaffected
- The multi-day tests were verified to **fail** against an anchor-only implementation

#### Note on the "F1" label, and how much authority it carries

The always-assign behaviour this ticket follows is labelled **F1** in code comments. **F1 is a finding/defect identifier used on `team/ras`, not a product specification** — the same numbering family as `F3` (capacity drift in `CourtScheduleRepositoryImpl`) and the `C1`/`C2` labels from this feature's own review. It is **not on `origin/main`**: the entire multi-day selection machinery arrived with the Crown search-and-book work (`SPRDT-1273`, `SPRDT-1190`, `SPRDT-1224`).

It is nonetheless a deliberate, tested decision — `MultiDaySearchAndBookIT` has committed tests such as `shouldBookAllDaysWhenLaterDayHasPreExistingAllocationsThatReduceAvailability`, commented *"F1: the shortfall is advisory — the booking must proceed and overbook day 2"*.

**So the justification for this ticket's rule is not "F1 says so".** It is narrower and stronger: **the reserve must not be stricter than the share.** The share path demonstrably overbooks a full day and has integration tests locking that in; a reserve that refused would block clerks from listings the platform will happily make. If always-assign is itself wrong, that is a conversation with the Crown search-and-book team, and changing it should change **both** paths together — never just this one.

---

### NEW-16 — [FE] Let the session picker refuse sessions that are already full
**Status: open** · **Priority: NICE TO HAVE — not a show stopper, does not block the reserve-a-slot release** · **Owner: the `@cpp/scheduling` package team, not us**

> **Written to be handed over.** The `@cpp/scheduling` owners have no reason to know about reserve-a-slot, so this ticket stands on its own.

**What happens today.** `@cpp/scheduling`'s session picker shows each session's remaining units but lets a clerk select one with none left. Nothing anywhere prevents it — verified at every layer: the hearing-slots search query filters on `active` + `panel` + `session_start` with no `HAVING`; the fetch-by-ids query filters on `active` + `id`; consuming apps treat `availableDurationMins` as a search parameter, not a capacity filter; and `HearingSlotsRowComponent` binds no `disabled` to its radio/checkbox.

Telling detail from the component itself: it renders `availableSlots > 0 ? availableSlots : 0`, clamping a **negative** value. That was written defensively because over-capacity sessions were already reachable.

**Why it is worth fixing.** Letting someone pick a session and then rejecting them afterwards is the worst of both worlds — the clerk has already made a decision and communicated it. Refusing the selection up front is plainly better, and the count is already on screen to explain why.

**Asked-for change**

```
enforceOverbookingPolicy: boolean = false
```

An opt-in input, **defaulting to `false` so every existing consumer is untouched and needs no change at all**. When `true`, a session is unselectable if it has no remaining capacity **unless** its `overbookingAllowed` flag is set. Consuming apps that want the behaviour pass `true` on their pickers — one line each.

The name is deliberate: *enforce the policy*, not *disable full sessions*. A flag named the latter invites an implementation that ignores `overbookingAllowed` and breaks the sessions the platform intentionally permits overbooking on.

**Everything needed is already in the package — no upstream change required.** `types/hearingSlot.d.ts` already declares `availableSlots?: number`, `availableDuration?: number`, `allDaySplit`, the AM/PM duration fields, and at line 42 **`overbookingAllowed: boolean`** (non-optional). Both backend queries already select `is_overbooking_allowed`. So this is **one package**, with no courtscheduler, listing or DTO change. Worth spot-checking against real data that `overbookingAllowed` is genuinely populated, since the type asserts it unconditionally.

**AC**
- Default (`false`) behaviour is byte-identical to today for every existing consumer
- With `true`, a slot-based session at zero remaining is not selectable; a duration-based one with insufficient remaining duration is not selectable — including the AM/PM split cases the component already models
- A session with `overbookingAllowed: true` stays selectable at or beyond capacity
- **Disabled, not hidden.** The remaining-units count stays visible. Hiding removes information the clerk has today and makes "why is that session missing?" unanswerable
- The reason is conveyed to assistive technology, not by colour or disablement alone

**Explicitly not a replacement for server-side enforcement.** The slot list goes stale the moment it renders — a clerk reads, thinks and picks while other clerks are booking. This removes the common case; only the server check removes the race. Any consumer relying on this alone would be wrong to.

---

## New tickets — cpp-ui-hearing (no front-end tickets exist today)

Every ticket on the board is `[BE]`. The UI work is the largest remaining chunk and the only part the clerk sees.

### NEW-11 — [FE] Reserve the session at the moment the clerk picks it
**Status: implemented, uncommitted (cpp-ui-hearing `team/ccsph2`)** · **suite now green — 14/14** across both containers, including the untouched magistrates reference · ready for code review

`crown-scheduling.container.ts` and `related-hearings.container.ts` call the existing `ProvisionalBookingService` and write the returned `bookingId` into `bookingReference`, exactly as `magistrates.container.ts` already does.

**AC**
- Crown's `bookingReference` becomes the `bookingId`, not the `courtScheduleId`
- Safe because Crown's use of that field is not in production — confirmed absent from `origin/main` in both containers
- `duration` is sent (both containers already destructure it from `hearingSlotAllocations[0]`)

### NEW-12 — [FE] Release the hold when the clerk changes their mind
**Status: implemented, uncommitted (cpp-ui-hearing `team/ccsph2`)** · reviewed, approved · 345/345 · all three abandon paths covered (draft delete, delete-via-amend, reset-results)

**Scope reduced by NEW-15, but what remains is the harder half.** Re-picking is no longer this ticket's problem — it is handled server-side by reusing the bookingId. What remains is every path that removes the booking *without* a pick following it.

**Why these are worse than a re-pick.** On a re-pick the `bookingReference` prompt is still present, so the request carries it and courtscheduler wipes the old hold. On a delete, the prompt goes with the result line — afterwards **nothing anywhere points at that bookingId**. The next pick has no id to send and mints a fresh one. The hold is not merely unreleased but *unreleasable*: the only reference to it is gone. So the release must fire at the moment of deletion, while the reference is still in hand. There is no later opportunity, and the 01:00 purge is the only fallback.

**Three paths to cover**, all verified in `cpp-ui-hearing`:
- `destroyDraftResultLine` on a DRAFT line — `destroyResultLine$` in `draft-result.effects.ts` rebuilds the draft without the line and never touches the booking
- the same on a SHARED line via amend — routes through `DraftResultLineOptionsComponent.handleDestroyResultLine`, which demands an amendment reason first
- reset-results — wipes everything at once (`share-results.effects.ts`, the `isResetResults: true` saves)

**Safety property to preserve, and to state explicitly so nobody "fixes" it.** `DELETE /sessions/{id}` → `SlotsRemoveService.remove` → `releaseOldAllocatedListings(id)`, which is keyed on `hearing_id`. A reservation's `hearing_id` is its bookingId, so releasing by bookingId works. A **confirmed** booking's `hearing_id` is the real next-hearing id, so the same call can never cancel a real listing. That matters on the SHARED-line path: cancelling a listed hearing is a relisting operation, not something a draft edit may trigger.

Release the previous reservation on re-pick, result-line delete, and reset-results, via the existing `DELETE /sessions/{id}`.

**AC**
- Capacity returns immediately rather than at 01:00 — with a same-day hold, a clerk trying three sessions would otherwise hold all three for the sitting day
- The nightly purge remains the backstop for closed browsers and back-button navigation
- Release tolerates "nothing to release"

### NEW-13 — [FE] The three clerk messages
**Status: implemented, uncommitted (cpp-ui-hearing `team/ccsph2`)** · reviewed, approved · **delivered without the PDK MCP** — see note below

| When | Message |
|---|---|
| Pick, no capacity | "No session available, please try again" |
| Pick, held | "You have successfully reserved this session. You must share the result by the end of the day, otherwise you will lose the session." |
| Share, hold gone | "Your booked session has already expired. Please go back and re-book a new session." |

**AC**
- Rendered as GOV.UK notification banners ("There is a problem" / "Success")
- The pick can now **fail** where it previously always succeeded: `provisionalBooking.service.ts` types the response as `{ bookingId: string }` and registers only a `successEvent`, but the processor emits failures on that **same** event name with an `error` payload — the service must discriminate on payload shape
- Message 2's deadline is deliberately conservative: `expires_at` is the day of the pick and the sweep runs at 01:00, so the true cut-off is slightly later
- Message 1 depends on DEC-2 — without capacity enforcement it can never fire


**Delivered without the PDK MCP, and without waiving the rule.** The `crime-frontend-developer-mcp` server was unreachable throughout, and project rules make it the only permitted source of PDK structural data. No PDK element was authored: `<pdk-alert type="warning" icon="true">` was **already committed** in `manage-hearing.container.html` with its text coming from a `translate` pipe, so the clerk messages became **translation keys against an element that already exists and compiles**. Reviewer confirmed the element, its attributes and its `data-test-id` are byte-identical; the only HTML change is the interpolated key. That is reuse of repo code, not recall from training data.

**Design finding for product — the three messages collapse to one.** `safeToShare` is `status !== 'NONE'`, so a blocked share can only ever carry `NONE`. The `SESSION_NOT_AVAILABLE` branch is therefore unreachable from this gate today, and because DEC-2 means capacity is never enforced at reserve time, "no session available" can never fire either. The code is correct and the dead branch is harmless, but **the three-message design cannot be delivered by this gate alone** — it needs either DEC-2 resolved or a different trigger point. The new message's copy also needs content-design sign-off.

### NEW-14 — [FE] Gate the share on the hold still existing
**Status: implemented, uncommitted (cpp-ui-hearing `team/ccsph2`)** · reviewed, approved · folded into BUG-4's fix, since both change what drives the same gate

`share-result.container.ts` already runs `validateSessionAvailabilityAndShare` before sharing — a synchronous call to listing that treats 400 as unavailable and blocks. That hook becomes "does every booking reference on this draft still have a live hold?".

**AC**
- `session-availability.helper.ts` collects booking references from **NHCCS and NHMC** lines (today it filters to NHCCS and reads the value as a court schedule id)
- Blocked on DEC-1
- A legacy magistrates draft must not be blocked

---

### NEW-15 — [BE] Re-picking a session gives back the one the clerk abandoned
**Status: backend implemented, uncommitted (courtscheduler `team/ras` reviewed & approved; cpp-context-hearing `team/ccsph2` in progress)** · **FE half deferred**

Today a re-pick mints a fresh bookingId, so the previous reservation stays keyed on the old id, holding real capacity until 01:00. A clerk trying three sessions holds all three for the sitting day.

This never mattered before reservations existed: `provisional_booking` rows hold no capacity, so the stale rows re-picking leaves behind were inert. Reservations take that free property away, so "one next-hearing, one held session" has to become deliberate.

**The mechanism — the re-pick comes back under the same bookingId.** A reservation's `hearing_id` *is* its bookingId, and `saveBookedSlots` already opens with a hearing-wide `releaseOldAllocatedListings(hearing_id)`. So reusing the id makes the existing pipeline wipe every row of the previous pick — all of them, including a multi-day Crown hold — and take the new ones, in one transaction, with the correct three-step capacity restore. **No new release code anywhere.** `POST /provisionalBooking` gains one optional `bookingId`: supplied means *reuse this booking*, absent means *mint me one*.

A first design added a separate `replacesBookingId` field plus an explicit release. It was built and then removed: it was a second release mechanism bolted onto a pipeline that already had one.

**AC**
- Re-picking releases every row of the previous pick before taking the new sessions; capacity nets to exactly one held session per result line
- Idempotent — a retried pick releases and re-takes rather than stranding a hold
- `bookingId` optional end to end; absent means mint, and hearing is a pure pass-through that never invents or validates it
- Two result lines can never collide: each NHCC/NHMC line mints its own `bookingReference` and carries it in its own prompt
- The `ReservationService` class comment that framed a shared key as a hazard is rewritten — left alone it invites this behaviour being "fixed" back out. The neighbouring paragraph on one `saveBookedSlots` call per booking stays: reserving slot by slot would still collapse a three-day pick to one day

**Deployment order matters.** `ProvisionalSlotConverter` uses a bare `ObjectMapper`, so `FAIL_ON_UNKNOWN_PROPERTIES` is on: courtscheduler must ship before hearing starts sending the field, or every provisional booking fails.

**Known and accepted:** the endpoint now takes a caller-supplied id, so a request could name another draft's booking and release its hold. Internal service behind platform auth, ids are UUIDs — recorded rather than defended in code.

**FE half (separate ticket, deferred):** both pickers already have `resultLine` in scope, so the previous value is `resultLine.resultPrompts.find(p => p.promptRef === 'bookingReference')?.value`, sent as `bookingId`. The returned id is then the same value, so writing the prompt back is a no-op.

**Shrinks but does not retire NEW-12:** result-line delete and reset-results still strand a hold until 01:00, because no new pick follows them to carry a bookingId.

---

### BUG-4 — [FE] The Crown pre-share check will break when NEW-11 lands
**Status: fixed, uncommitted (cpp-ui-hearing `team/ccsph2`)** · reviewed, approved · **unblocks NEW-11 shipping**

`session-availability.helper.ts` (on `team/ccsph2`, not on `main`) reads the `bookingReference` prompt of a Crown `NHCCS` line **as a `courtScheduleId`** and passes it to `listingService.validateSessionAvailability`. NEW-11 changes Crown's `bookingReference` to a bookingId. Listing proxies straight through, and `SessionsService.validateListModeSchedulesFound` answers `"Court Schedule Ids not found"` for an unrecognised id → `ValidationException` → 400 → the container's `catchError` sets `hasSessionAvailabilityError` and **the share is blocked**. Every Crown NHCCS share would fail.

The two changes are individually sensible and mutually destructive.

**Fix:** that helper should stop deriving a courtScheduleId from `bookingReference` and call the booking-status check (NEW-10) instead — which is the gate it was approximating. That makes **NEW-10 a dependency of NEW-11**, not a parallel ticket.

**Second, pre-existing, worth confirming with its author:** the helper runs on *every* share, re-shares included. Today `bookingReference` really is a courtScheduleId, so a re-share after amend re-validates a session this very hearing already consumed a slot in, and `validateListModeSlotBased` compares allocations against `maxSlots` with no exemption for the hearing being shared. Not proven to fire in practice — only that nothing in the comparison excludes the caller's own booking.

---

## New tickets — test coverage and guards

These exist because two defects reached review approval today and both were found by running the code, not by reading it.

### TEST-1 — [BE] Integration coverage on the path listing actually uses
**Status: implemented, uncommitted (courtscheduler `team/ras`)** · reviewed, approved · **8/8 running against a real docker-compose stack**

`ProvisionalBookingIT`'s one end-to-end test confirmed a booking through `PUT /hearingslots` (`update.hearing.slots`) while asserting in its Javadoc that this was *"the real share path"*. It is not — listing posts `list.hearings-in-sessions`. The single integration test we had proved the release on a path production does not take, and said so in words that stopped anyone looking further. BUG-3 was found by a human noticing an unused column, not by that test.

Three tests added, all on the real path: BUG-3's release and one-net-decrement arithmetic; NEW-15's re-pick returning the abandoned session's slot; NEW-7a's `RESERVED → SHARED → NONE` transitions. The misleading sentence is corrected.

**These tests found BUG-5 and BUG-6.**

**AC**
- The BUG-3 test is proven non-vacuous: omitting `bookingId` makes it fail exactly on the hold-release assertion
- `bookingId` sits beside `hearingId`, never inside `courtScheduleIds[]`

### TEST-2 — [BE] RAML↔Drools access-control guards
**Status: implemented in all three repos, uncommitted** · **reviewed, approved, no findings** · all three pass on arrival and are each proven to bite under both `//` and `/* … */`

| Repo | RAML actions | DRL rules | Allowlisted |
|---|---|---|---|
| cpp-context-listing (query API) | 21 | 22 | none |
| cpp-context-listing-courtscheduler | 42 | 43 | 10 — all verified genuine; each has a live rule under a shorter alias or a REST-path key, never an actual absence |
| cpp-context-hearing (command API) | 69 | 71 | 1 — and it is a suspected defect, see BUG-7 |

**Two API modules remain unguarded, and extending the measurement to them is how BUG-8 was found:**

| Module | Measured | Guard status |
|---|---|---|
| cpp-context-hearing query API | 36 actions, 37 rules, **no gaps** | not yet guarded — a guard would pass on arrival with an empty allowlist |
| cpp-context-listing command API | 31 actions, 28 rules, **4 gaps** | not yet guarded — needs BUG-8's four allowlisted as suspected defects |

Finishing both completes the coverage. The pattern is now written three times and is mechanical to repeat; what is not mechanical is verifying each allowlist entry's justification, which is the part that must not be rushed — an entry whose stated reason is wrong launders a real gap.

A config test asserting every RAML action has an access-control rule. This is the automated form of the check that would have caught BUG-5.

Three traps, all found the hard way and all handled:
- **Comment-blindness.** A rule commented out with `//` still matched a naive regex, so a *disabled* rule read as present — a false negative in exactly the direction the test exists to catch. Both `//` and `/* … */` are stripped before matching.
- **Vacuous success.** If the regex silently matches nothing, "no missing rules" is trivially true forever. A non-empty guard prevents it.
- **Stale allowlists.** An exemption that outlives its reason hides the next genuine gap. A second assertion fails if an allowlisted action has since gained a rule.

Deliberately **one-directional** — every RAML action has a rule, not set equality. Rules legitimately exist with no RAML action (`listing.public.list`), so equality would fail on arrival, and a test that fails on arrival gets disabled rather than fixed.

Allowlisted, each with its reason recorded in code:
- courtscheduler: `courtscheduler.get.court_schedule` (covered by a rule named `courtscheduler.get`) and nine `judiciary.*.availability*` actions (rules keyed on REST paths such as `POST /judiciaries/availability-rules/add`)
- hearing: `hearing.replicate-shared-results` — **marked as a suspected defect, not an exemption.** See BUG-7.

### TEST-3 — [BE] The purge test proves capacity comes back
**Status: implemented, uncommitted (courtscheduler `team/ras`)** · **reviewed, approved, no findings** · 135/135 in the class against a real docker-compose stack

`CourtSchedulerIT.shouldPurgeAllocatedListingsWhoseExpiresAtHasAlreadyPassed` asserted only that rows were deleted — never that `available_slots` was restored. **BUG-1 was exactly that defect**, so the test guarding it would have passed against it.

The fix is structural, not a new assertion: the test seeded rows straight into the table, so capacity was never taken and there was nothing to restore. The reservations must be created through `POST /provisionalBooking` so the pipeline genuinely decrements capacity first.

**AC**
- Proven by sabotage: commenting out the purge's two capacity-restore calls made the test fail on the capacity assertion with `expected: <4> but was: <3>`, naming BUG-1; reverting made it pass
- Asserts an exact slot count, not a direction: capacity drops by exactly 2 before the purge and returns by exactly 1 after — the expired reservation's slot comes back, the survivor's does not
- The selective branch (an unexpired reservation survives) is preserved
- A Javadoc warns against "simplifying" it back to direct row seeding, which is what made the old test hollow

### TEST-4 — [BE] Label the fail-open test's expected server error
**Status: implemented, uncommitted (cpp-context-listing `team/ccsph2n`)** · compile-verified only — listing's IT stack (Postgres, Artemis, WildFly) is unavailable locally, so this runs in CI

`BookingStatusIT.shouldFailOpenWithUnknownWhenCourtschedulerErrors` deliberately drives courtscheduler to a 500, and the production path logs an ERROR. Its template's equivalent test carries `@ExpectedServerErrors` so log-triage tooling knows that ERROR is intended; ours did not. Annotation copied verbatim from `CourtScheduleDraftStatusIT`, not invented.

---

## Decisions that need tickets of their own

### DEC-1 — Re-share semantics for the pre-share gate
**Status: RESOLVED — option B, delivered as NEW-7a**

A gate wired straight to NEW-7 blocks **re-sharing** an already-shared result with a false *"your booked session has expired"*. After a successful share the reservation is hard-deleted and replaced by a confirmed booking under the real hearing id, so `live: false` is the honest answer to "is there a hold?" — but the wrong answer to "is this draft safe to share?".

Only the new flow is affected. Legacy magistrates drafts survive by accident, because `findByBookingIdIn` has no `active` filter and so still returns the soft-deleted row.

| Option | Where | Trade-off |
|---|---|---|
| **A (recommended)** | cpp-ui-hearing | Run the gate only when the draft has not already been shared. The UI knows the hearing's shared state; courtscheduler does not. Cheapest, keeps courtscheduler's contract honest. |
| B | cpp-context-listing | Answer the richer question by combining the hold check with progression's stored `bookingId → courtScheduleIds` map. More robust, more work. |
| C | courtscheduler | Tombstone reservations on confirm instead of hard-deleting, so `live: false` can distinguish expired from confirmed. Reopens NEW-3/NEW-5. |

**Now answerable in courtscheduler, cheaply.** `allocated_listings` carries a `booking_id` column, and since BUG-3 the confirmed row written at share time is stamped with it. So courtscheduler can distinguish the two cases without tombstones: a **reservation row** (`expires_at` non-null) means the hold is live; a **live row with the same `booking_id`** (`expires_at is null`) means this draft was already shared. Neither means expired. That is option **B/C's robustness at option A's cost**, and it makes NEW-10 a thin pass-through rather than a design problem.

It is deliberately **not** folded into BUG-3: it changes `getBookingStatus`'s response contract, which NEW-7 already shipped, so it needs its own ticket and its own retest. That ticket is **NEW-7a** above, now implemented. The table of options below is kept for the record; **B** is what shipped, at A's cost rather than B's, because `booking_id` removed the need to consult progression at all.

The design doc's section 04 currently implies the check alone is sufficient and needs updating once this is settled.

### DEC-2 — Is capacity enforced on reserve, or is overbooking allowed?
**Status: SUPERSEDED by BUG-9 — the premise was wrong**

**This decision was framed on a false premise and should be closed.** It assumed capacity was never enforced, so adding a check would be new behaviour needing a product call. In fact `validateListModeSlotBased` enforced it at share time, with a per-session `isOverbookingAllowed` opt-out, and BUG-4 removed the UI's route to it. Restoring the check is therefore a regression fix, not a new policy.

The only genuine decision left is whether a *reservation* should honour `isOverbookingAllowed` as the old share-time check did. Recommended: yes, for consistency — a session configured to permit overbooking should permit it at pick time too.

`getUpdatedAllocatedSlots` performs **no** capacity comparison — it resolves the court schedule and nothing else. So reserving into a session with `available_slots = 0` returns 200 and drives the column negative, and `NoCapacityException` is effectively unreachable. The clerk message *"No session available, please try again"* can therefore never fire.

This is a product decision about whether a reservation may oversubscribe a session, not a defect to fix silently. Note that `search-and-book` is deliberately overbooking-exempt elsewhere in courtscheduler, so there may be precedent for allowing it.

---

## Suggested sequencing

```
BUG-1 ──────────────► LPT-2433, LPT-2506 (retest)
BUG-2 ──────────────► cpp-ui-listing calendar correct
BUG-3 ──────────────► capacity nets to one decrement at share
                      (courtscheduler + listing — MUST ship together)
BUG-5 ──────────────► get.booking-status reachable at all
                      (without it NEW-7a + NEW-10 + the whole gate are dead)
BUG-6 ──────────────► TEST-1 can assert booking_id

TEST-1 ─────────────► found BUG-5 and BUG-6
TEST-2 ─────────────► prevents another BUG-5; surfaced BUG-7 (not ours)
TEST-3 ─────────────► prevents another BUG-1

NEW-3, NEW-4, NEW-5, NEW-6, NEW-7   (courtscheduler — done, uncommitted)
        │
        ├── NEW-8   (hearing — done, uncommitted; MUST ship with NEW-6)
        │
        ├── NEW-9   (listing — done, uncommitted)
        │
        ├── NEW-12a (hearing — done, uncommitted) ──► NEW-12 (FE)
        │
NEW-7a ─┴── NEW-10  (listing — unblocked) ──► NEW-14 (FE gate)
        │   (NEW-7a resolves DEC-1; depends on BUG-3)
        │
DEC-2 ─────────────────────────► NEW-13 (FE message 1)
        │
        └── NEW-11 (FE — done, review outstanding)

LPT-2506 — independent of the FE work; only needs BUG-1
```

**Nice to have, explicitly NOT blocking this release:**
- **NEW-16** — `@cpp/scheduling` opt-in `enforceOverbookingPolicy`, so the picker refuses full sessions up front. BUG-9 already closes the correctness hole server-side; NEW-16 only improves *when* the clerk finds out. Owned by the `@cpp/scheduling` team.

**Raise with other teams, explicitly out of scope for this feature — record only, do not fix:**
- **BUG-7** — `hearing.replicate-shared-results` appears to have no access-control rule.
- **BUG-8** — four `listing.command.*` offence/defendant actions appear to have no rule.
- Both rest on **static evidence only**, unlike BUG-5 which was proven at runtime. Confirm the 403 before acting: for courtscheduler's ten judiciary actions the same static signal turned out to be benign, because the rules were keyed on REST paths instead.
- The two remaining unguarded API modules (hearing query API, listing command API) are out of scope for the same reason. Their measurements are recorded under TEST-2 so whoever picks them up starts from data, not from scratch.

**Not needed, recorded so nobody plans it:**
- **cpp-context-progression** — no change at all. **Re-verified after the design changed:** `ProvisionalBookingServiceAdapter.getSlots` fetches live from courtscheduler per call and builds the `bookingId → courtScheduleIds` map in memory. Nothing is cached or persisted, so NEW-15's re-use of a bookingId across a re-pick cannot leave progression holding a stale session mapping
- **cpp-ui-listing** — no change; its court calendar is fixed by BUG-2 in courtscheduler
- **CoreDomain** — no change (this is what LPT-2474 assumed would be required)

## Caveat on statuses

**Two defects reached review approval today and neither was caught by reading code** — BUG-5 (an endpoint unreachable for every caller) and BUG-1's guard (a test that would have passed against the defect it guarded). Both were found by running things. TEST-1, TEST-2 and TEST-3 exist so that neither shape can recur. That is the single most useful thing to carry out of this work.

Everything marked *implemented, uncommitted* exists as unstaged working-tree changes in `cpp-context-listing-courtscheduler` (`team/ras`) and `cpp-context-hearing` (`team/ccsph2`). Nothing has been committed or pushed, so none of it is visible to anyone else yet. Statuses should not move to READY FOR TEST until the work is committed and raised as PRs.
