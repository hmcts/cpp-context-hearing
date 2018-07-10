package uk.gov.moj.cpp.hearing.event.nows;

public class InvalidNotificationException extends Exception {
    public InvalidNotificationException(String message) {
        super(message);
    }

    public InvalidNotificationException(String message, Exception ex) {
        super(message, ex);
    }
}
