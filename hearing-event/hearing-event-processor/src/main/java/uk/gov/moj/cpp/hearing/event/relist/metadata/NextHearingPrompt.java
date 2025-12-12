package uk.gov.moj.cpp.hearing.event.relist.metadata;

import java.util.UUID;

public class NextHearingPrompt {
    private UUID id;
    private String promptReference;

    public NextHearingPrompt(final UUID id, final String promptReference) {
        this.id = id;
        this.promptReference = promptReference;
    }

    public String getPromptReference() {
        return promptReference;
    }

    public UUID getId() {
        return id;
    }
}
