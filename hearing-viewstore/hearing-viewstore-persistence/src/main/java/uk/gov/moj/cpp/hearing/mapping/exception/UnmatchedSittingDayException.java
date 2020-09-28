package uk.gov.moj.cpp.hearing.mapping.exception;

public class UnmatchedSittingDayException extends RuntimeException{
    public UnmatchedSittingDayException(final String message) {
        super(message);
    }
}
