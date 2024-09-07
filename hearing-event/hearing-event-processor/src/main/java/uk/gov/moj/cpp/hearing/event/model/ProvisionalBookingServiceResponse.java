package uk.gov.moj.cpp.hearing.event.model;

import org.apache.commons.lang3.StringUtils;

public class ProvisionalBookingServiceResponse {
    private final String bookingId;
    private final String errorMessage;

    private ProvisionalBookingServiceResponse(String bookingId, String errorMessage) {
        this.bookingId = bookingId;
        this.errorMessage = errorMessage;
    }

    public static ProvisionalBookingServiceResponse normal(String bookingId) {
        return new ProvisionalBookingServiceResponse(bookingId, null);
    }

    public static ProvisionalBookingServiceResponse error(String errorMessage) {
        return new ProvisionalBookingServiceResponse(null, errorMessage);
    }


    public String getBookingId() {
        return bookingId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean hasError() {
        return StringUtils.isEmpty(bookingId);
    }
}
