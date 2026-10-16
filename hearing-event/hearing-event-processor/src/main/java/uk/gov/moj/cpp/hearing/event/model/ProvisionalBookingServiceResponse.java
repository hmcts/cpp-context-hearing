package uk.gov.moj.cpp.hearing.event.model;

import org.apache.commons.lang3.StringUtils;

/**
 * The outcome of one call to courtscheduler's {@code POST /unconfirmedBooking}: either a
 * bookingId, or a refusal carrying BOTH a machine-readable {@link ProvisionalBookingFailureCode}
 * and the free text behind it.
 *
 * <p>The code exists because the two now go to different public events with different payloads,
 * and the event processor is the wrong place to re-derive "which kind of failure was this" — only
 * the service that made the HTTP call still has the status code and courtscheduler's own message.
 */
public class ProvisionalBookingServiceResponse {
    private final String bookingId;
    private final ProvisionalBookingFailureCode errorCode;
    private final String errorMessage;

    private ProvisionalBookingServiceResponse(final String bookingId,
                                              final ProvisionalBookingFailureCode errorCode,
                                              final String errorMessage) {
        this.bookingId = bookingId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public static ProvisionalBookingServiceResponse normal(final String bookingId) {
        return new ProvisionalBookingServiceResponse(bookingId, null, null);
    }

    public static ProvisionalBookingServiceResponse error(final ProvisionalBookingFailureCode errorCode,
                                                          final String errorMessage) {
        return new ProvisionalBookingServiceResponse(null, errorCode, errorMessage);
    }

    public String getBookingId() {
        return bookingId;
    }

    /** Never null when {@link #hasError()} is true. */
    public ProvisionalBookingFailureCode getErrorCode() {
        return errorCode;
    }

    /**
     * Free text for the operator log and for the {@code reason} field of the failure event. It
     * deliberately carries courtscheduler's status code as well as its message: a bare message
     * made a 400 (our bug) indistinguishable from a 502 (theirs) in the WildFly log.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return StringUtils.isEmpty(bookingId);
    }
}
