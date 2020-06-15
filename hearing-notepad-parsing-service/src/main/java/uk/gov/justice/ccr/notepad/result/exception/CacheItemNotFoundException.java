package uk.gov.justice.ccr.notepad.result.exception;

public class CacheItemNotFoundException extends RuntimeException {
    public CacheItemNotFoundException(String message) {
        super(message);
    }
}
