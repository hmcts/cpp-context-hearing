package uk.gov.moj.cpp.hearing.common.exception;

public class ReferenceDataNotFoundException extends RuntimeException {
    public ReferenceDataNotFoundException(final String message) {
        super(message);
    }
}