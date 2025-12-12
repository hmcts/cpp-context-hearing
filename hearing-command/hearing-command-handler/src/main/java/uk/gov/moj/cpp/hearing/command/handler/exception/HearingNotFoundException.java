package uk.gov.moj.cpp.hearing.command.handler.exception;

public class HearingNotFoundException extends RuntimeException {
    public HearingNotFoundException(final String message) {
        super(message);
    }
}
