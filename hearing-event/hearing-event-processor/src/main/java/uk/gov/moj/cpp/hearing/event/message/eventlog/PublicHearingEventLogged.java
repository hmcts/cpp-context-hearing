package uk.gov.moj.cpp.hearing.event.message.eventlog;

public class PublicHearingEventLogged {

    private Case legalCase;

    private HearingEventDefinition hearingEventDefinition;

    private HearingEvent hearingEvent;

    private Hearing hearing;

    public PublicHearingEventLogged(Case legalCase, HearingEventDefinition hearingEventDefinition, HearingEvent hearingEvent, Hearing hearing) {
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