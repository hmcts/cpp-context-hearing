package uk.gov.moj.cpp.hearing.xhibit.exception;

public class GenerationFailedException extends RuntimeException {
    public GenerationFailedException(final String message) {
        super(message);
    }

    public GenerationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
