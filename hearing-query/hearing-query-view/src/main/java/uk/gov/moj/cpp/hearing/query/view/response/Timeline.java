package uk.gov.moj.cpp.hearing.query.view.response;

import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;

import java.util.List;

public class Timeline {

    private final List<TimelineHearingSummary> hearingSummaries;

    public Timeline(final List<TimelineHearingSummary> hearingSummaries) {
        hearingSummaries.sort(comparing(TimelineHearingSummary::getHearingDate).reversed());
        this.hearingSummaries = unmodifiableList(hearingSummaries);
    }

    public List<TimelineHearingSummary> getHearingSummaries() {
        return unmodifiableList(hearingSummaries);
    }
}