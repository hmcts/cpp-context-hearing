package uk.gov.moj.cpp.hearing.domain.transformation.judicialresult.domain;

import java.util.Arrays;

public enum EventToTransform {

    RESULTS_SHARED("hearing.results-shared"),
    PENDING_NOWS_REQUESTED("hearing.events.pending-nows-requested"),
    NOWS_REQUESTED("hearing.events.nows-requested"),
    HEARING_INITIATED("hearing.events.initiated"),
    HEARING_EXTENDED("hearing.events.hearing-extended"),
    APPLICATION_DETAIL_CHANGED("hearing.events.application-detail-changed"),
    CASE_DEFENDANTS_UPDATED_FOR_HEARING("hearing.case-defendants-updated-for-hearing"),
    CASE_DEFENDANTS_UPDATED("hearing.case-defendants-updated");

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
