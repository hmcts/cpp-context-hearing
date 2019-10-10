package uk.gov.moj.cpp.hearing.event.delegates.exception;

public class InvalidDateFormatException  extends RuntimeException {

    public InvalidDateFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
