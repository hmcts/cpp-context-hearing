package uk.gov.moj.cpp.hearing.event.exception;

public class BacsNotFoundException extends RuntimeException {

    public BacsNotFoundException(String message) {
        super(message);
    }
}
