package uk.gov.moj.cpp.hearing.event.model;

import static uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingFailureCode.Type.BUSINESS;
import static uk.gov.moj.cpp.hearing.event.model.ProvisionalBookingFailureCode.Type.TECHNICAL;

/**
 * Why a reservation against courtscheduler was refused or failed, as carried by the
 * {@code public.hearing.hearing-slots-provisional-booking-refused} public event (LPT-2590).
 *
 * <p>Each code has a {@link Type}. A {@link Type#BUSINESS} code is courtscheduler deliberately
 * refusing the booking, and the UI shows a message for that code. A {@link Type#TECHNICAL} code is
 * the call going wrong, and the UI sends the clerk to its standard technical error page. The
 * HTTP status and courtscheduler's own text stay in the event's {@code reason}, for the logs.
 *
 * <p>Codes are mapped from courtscheduler's HTTP answer in
 * {@link uk.gov.moj.cpp.hearing.event.service.ProvisionalBookingService#failureCodeFor}. The
 * mapping is by status alone except for 409, which has to read the message text - see
 * {@link #CONFIRMED_BOOKING_EXISTS}.
 *
 * <p>Adding a code is a public-contract change: it must also go in the {@code code} enum of
 * {@code public.hearing.hearing-slots-provisional-booking-refused.json}, or the framework drops
 * the event at publication time for failing its own schema.
 */
public enum ProvisionalBookingFailureCode {

    /**
     * courtscheduler refused because the session cannot hold the booking: it is full, overbooking
     * is not allowed, or a Crown multi-day run could not be resolved. 409 Conflict, raised from
     * {@code NoCapacityException}.
     */
    NO_CAPACITY(BUSINESS),

    /**
     * The booking id being reused already carries a CONFIRMED allocation, so courtscheduler will
     * not move its hold. 409 Conflict, raised from {@code ConfirmedBookingExistsException}.
     *
     * <p>CAVEAT: courtscheduler maps BOTH this and {@code NoCapacityException} to 409, and
     * normalises every refusal to {@code {"error":"<message>"}}. The status cannot separate them,
     * so the two are told apart by a marker substring of courtscheduler's own message. See
     * {@code ProvisionalBookingService.CONFIRMED_ALLOCATION_MARKER} - if that message is reworded
     * upstream, this code silently degrades to {@link #NO_CAPACITY}.
     */
    CONFIRMED_BOOKING_EXISTS(BUSINESS),

    /**
     * The request named a court schedule that does not exist or is not active. 422 Unprocessable
     * Entity, raised from {@code NoSessionAvailableException}. Usually a stale search result.
     */
    NO_SESSION_AVAILABLE(BUSINESS),

    /**
     * Anything else: courtscheduler rejected our request (400), fell over (500), could not be
     * reached (502/503/504 or no answer at all), answered with something that is not JSON, or
     * answered success without a booking id. None of these says anything about the session.
     *
     * <p>A non-JSON answer lands here whatever its status, including 409: courtscheduler's own
     * refusals always carry a JSON body, so a non-JSON 409 came from something in front of it and
     * is no evidence the session is full.
     */
    COURTSCHEDULER_ERROR(TECHNICAL);

    /** Whether the UI shows a message for the code, or its technical error page. */
    public enum Type {
        BUSINESS,
        TECHNICAL
    }

    private final Type type;

    ProvisionalBookingFailureCode(final Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }
}
