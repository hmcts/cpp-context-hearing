package uk.gov.moj.cpp.hearing.event.exception;

public class UserIdNotFoundException extends RuntimeException {

    public UserIdNotFoundException(String message) {
        super(message);
    }
}
