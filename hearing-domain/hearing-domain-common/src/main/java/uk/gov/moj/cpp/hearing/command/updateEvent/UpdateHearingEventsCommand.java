package uk.gov.moj.cpp.hearing.command.updateEvent;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateHearingEventsCommand {

    private final UUID hearingId;
    private final List<HearingEvent> hearingEvents;

    @JsonCreator
    public UpdateHearingEventsCommand(@JsonProperty("hearingId") final UUID hearingId,
                                      @JsonProperty("hearingEvents") final List<HearingEvent> hearingEvents) {
        super();
        this.hearingId = hearingId;
        this.hearingEvents = hearingEvents;
    }

    public static Builder builder() {
        return new Builder();
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public List<HearingEvent> getHearingEvents() {
        return hearingEvents;
    }

    public static class Builder {

        private UUID hearingId;
        private List<HearingEvent> hearingEvents;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withHearingEvents(final List<HearingEvent> hearingEvents) {
            this.hearingEvents = hearingEvents;
            return this;
        }

        public UpdateHearingEventsCommand build() {
            return new UpdateHearingEventsCommand(hearingId, hearingEvents);
        }
    }
}