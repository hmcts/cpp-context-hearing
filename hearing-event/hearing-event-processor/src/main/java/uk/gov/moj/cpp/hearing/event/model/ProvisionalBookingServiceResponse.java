package uk.gov.moj.cpp.hearing.event.model;

import org.apache.commons.lang3.StringUtils;

/**
 * The outcome of one call to courtscheduler's {@code POST /unconfirmedBooking}: either a
 * bookingId, or a refusal carrying courtscheduler's HTTP status as is and the free text behind it.
 *
 * <p>Hearing does not interpret the status. Which kind of refusal it was, and what to tell the
 * clerk, is left to the consumer of the public failure event.
 */
public class ProvisionalBookingServiceResponse {
    private final String bookingId;
    private final Integer status;
    private final String errorMessage;

    private ProvisionalBookingServiceResponse(final String bookingId,
                                              final Integer status,
                                              final String errorMessage) {
        this.bookingId = bookingId;
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public static ProvisionalBookingServiceResponse normal(final String bookingId) {
        return new ProvisionalBookingServiceResponse(bookingId, null, null);
    }

    /**
     * @param status courtscheduler's HTTP status, or null when it never answered (connection
     *               refused, timeout) and there is no status to report
     */
    public static ProvisionalBookingServiceResponse error(final Integer status,
                                                          final String errorMessage) {
        return new ProvisionalBookingServiceResponse(null, status, errorMessage);
    }

    public String getBookingId() {
        return bookingId;
    }

    /** courtscheduler's HTTP status on an error; null on success or when it never answered. */
    public Integer getStatus() {
        return status;
    }

    /**
     * Free text for the operator log and for the {@code message} field of the failure event. It
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
