package uk.gov.moj.cpp.hearing.domain.transformation.provisionalhearingslots.domain;

import java.util.Arrays;

public enum EventToTransform {

    BOOK_PROVISIONAL_HEARING_SLOTS("hearing.event.book-provisional-hearing-slots");

    private final String eventName;

    EventToTransform(final String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public static boolean isEventToTransform(final String eventName) {
        return Arrays.stream(values()).anyMatch(event -> event.eventName.equals(eventName));
    }
}
