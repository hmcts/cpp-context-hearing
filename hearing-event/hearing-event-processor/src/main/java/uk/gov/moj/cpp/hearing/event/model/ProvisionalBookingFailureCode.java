package uk.gov.moj.cpp.hearing.event.model;

/**
 * Why a reservation against courtscheduler was refused or failed, as carried by the
 * {@code public.hearing.hearing-slots-unconfirmed-booking-failed} public event.
 *
 * <p>Two jobs. First, let a consumer decide whether the clerk may be told "this session is
 * unavailable - pick another one": only {@link #NO_CAPACITY} and {@link #NO_SESSION} mean that,
 * and cpp-ui-hearing holds exactly those two in an allowlist. Second, let a consumer that cares
 * distinguish the ways the call can fail WITHOUT parsing English out of {@code reason} - our
 * request was malformed, courtscheduler fell over, we could not reach it at all. That second job
 * is why the non-refusal half is split by cause rather than being one catch-all.
 *
 * <p>Codes are mapped from courtscheduler's HTTP answer in
 * {@link uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService#failureCodeFor}. The
 * mapping is by status alone except for 409, which has to read the message text - see
 * {@link #BOOKING_ALREADY_CONFIRMED}.
 *
 * <p>Adding a code is a public-contract change: it must also go in the {@code code} enum of
 * {@code public.hearing.hearing-slots-unconfirmed-booking-failed.json}, or the framework drops
 * the event at publication time for failing its own schema.
 */
public enum ProvisionalBookingFailureCode {

    /**
     * courtscheduler refused because the session cannot hold the booking: it is full, overbooking
     * is not allowed, or a Crown multi-day run could not be resolved. 409 Conflict, raised from
     * {@code NoCapacityException}.
     *
     * <p>The only code that genuinely means "try a different session".
     */
    NO_CAPACITY,

    /**
     * The request named a court schedule that does not exist or is not active. 422 Unprocessable
     * Entity, raised from {@code NoSessionAvailableException}.
     *
     * <p>Usually a stale search result: the session was withdrawn between the clerk searching and
     * picking. Re-searching, not re-picking, is the fix - but either way the clerk's next action
     * is to choose a different session, so the UI treats it as a refusal too.
     */
    NO_SESSION,

    /**
     * The bookingId being reused already carries a CONFIRMED allocation, so courtscheduler will
     * not move its hold. 409 Conflict, raised from {@code ConfirmedBookingExistsException} -
     * {@code ReservationService#guardAgainstConfirmedAllocation}.
     *
     * <p>Not {@link #NO_CAPACITY}: the session may be completely free, and telling the clerk to
     * pick another one sends them round a loop that cannot succeed. That exact confusion is what
     * {@code ProvisionalBookingService.bookingIdToReuse} in cpp-ui-hearing was written to avoid,
     * and this code is how a consumer recognises the case if it slips through anyway.
     *
     * <p>CAVEAT: courtscheduler maps BOTH this and {@code NoCapacityException} to 409, and
     * normalises every refusal to {@code {"error":"<message>"}}. The status cannot separate them,
     * so the two are told apart by a marker substring of courtscheduler's own message. See
     * {@code ProvisionalBookingService.CONFIRMED_ALLOCATION_MARKER} - if that message is reworded
     * upstream, this code silently degrades to {@link #NO_CAPACITY}.
     */
    BOOKING_ALREADY_CONFIRMED,

    /**
     * 400. courtscheduler's validator rejected the request we built - a missing duration on a
     * duration-based session, a malformed id. This is OUR bug, not a problem with the session the
     * clerk chose, and it will recur identically on every retry.
     */
    BAD_REQUEST,

    /**
     * 500. courtscheduler reached its own code and that code threw. The request was well formed
     * and the session may be perfectly bookable; a retry is reasonable.
     */
    INTERNAL_SERVER_ERROR,

    /**
     * 502. Something between us and courtscheduler answered instead of it - or courtscheduler's
     * own upstream call failed, which its {@code GlobalExceptionHandler} also reports as 502.
     */
    BAD_GATEWAY,

    /**
     * 503, 504, or an {@link java.io.IOException} on the call itself (connection refused, read
     * timeout, DNS failure).
     *
     * <p>Distinct from {@link #INTERNAL_SERVER_ERROR} because the request never reached
     * courtscheduler's logic at all: nothing was written, nothing was held, and retrying once the
     * service is back is guaranteed safe. A 500 carries no such guarantee.
     */
    SERVICE_UNAVAILABLE,

    /**
     * courtscheduler answered, but not in a way the contract allows: a 2xx carrying no
     * {@code bookingId}, a body that is not JSON at all (an HTML error page from a sidecar, an
     * empty body), or any status not named above.
     *
     * <p>Also the deliberate landing place for an unparseable body at a status that WOULD
     * otherwise be meaningful - including 409. courtscheduler's own refusals always carry the
     * JSON error body, so a 409 that is not JSON came from something in front of it and is no
     * evidence that the session is full. Mapping it to {@link #NO_CAPACITY} would put it in the
     * UI's refusal allowlist and blame a session that may be free.
     */
    UNEXPECTED_RESPONSE
}
