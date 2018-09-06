package uk.gov.moj.cpp.hearing.eventlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PublicHearingEventLogged {

    private Case legalCase;

    private HearingEventDefinition hearingEventDefinition;

    private HearingEvent hearingEvent;

    private Hearing hearing;

    @JsonCreator
    public PublicHearingEventLogged(@JsonProperty("case") final Case legalCase, 
            @JsonProperty("hearingEventDefinition") final HearingEventDefinition hearingEventDefinition, 
            @JsonProperty("hearingEvent") final HearingEvent hearingEvent, 
            @JsonProperty("hearing") final Hearing hearing) {
        this.legalCase = legalCase;
        this.hearingEventDefinition = hearingEventDefinition;
        this.hearingEvent = hearingEvent;
        this.hearing = hearing;
    }

    public Case getCase() {
        return legalCase;
    }

    public HearingEventDefinition getHearingEventDefinition() {
        return hearingEventDefinition;
    }

    public HearingEvent getHearingEvent() {
        return hearingEvent;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public static class Builder {
        private Case.Builder legalCase;

        private HearingEventDefinition.Builder hearingEventDefinition;

        private HearingEvent.Builder hearingEvent;

        private Hearing.Builder hearing;

        public Builder withCase(Case.Builder legalCase) {
            this.legalCase = legalCase;
            return this;
        }

        public Builder withHearingEventDefinition(HearingEventDefinition.Builder hearingEventDefinition) {
            this.hearingEventDefinition = hearingEventDefinition;
            return this;
        }

        public Builder withHearingEvent(HearingEvent.Builder hearingEvent) {
            this.hearingEvent = hearingEvent;
            return this;
        }

        public Builder withHearing(Hearing.Builder hearing) {
            this.hearing = hearing;
            return this;
        }

        public PublicHearingEventLogged build() {
            return new PublicHearingEventLogged(
                    legalCase.build(),
                    hearingEventDefinition.build(),
                    hearingEvent.build(),
                    hearing.build()
            );
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}