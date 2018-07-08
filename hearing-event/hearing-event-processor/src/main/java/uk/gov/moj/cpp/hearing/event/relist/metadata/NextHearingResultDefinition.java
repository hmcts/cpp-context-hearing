package uk.gov.moj.cpp.hearing.event.relist.metadata;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class NextHearingResultDefinition {

    private UUID id;
    private List<NextHearingPrompt> nextHearingPrompts = newArrayList();

    public NextHearingResultDefinition(final UUID id, final NextHearingPrompt nextHearingPrompt) {
        this.id = id;
        addNextHearingPrompt(nextHearingPrompt);
    }

    public List<NextHearingPrompt> getNextHearingPrompts() {
        return Collections.unmodifiableList(nextHearingPrompts);
    }

    public void addNextHearingPrompt(final NextHearingPrompt nextHearingPrompt) {
        nextHearingPrompts.add(nextHearingPrompt);
    }

    public UUID getId() {
        return id;
    }
}
