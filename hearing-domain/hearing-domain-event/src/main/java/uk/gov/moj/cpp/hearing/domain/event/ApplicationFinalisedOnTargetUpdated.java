package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.events.application-finalised-on-target-updated")
@SuppressWarnings("squid:S2384")
public class ApplicationFinalisedOnTargetUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID hearingId;

    private LocalDate hearingDay;

    private boolean applicationFinalised;

    @JsonCreator
    public ApplicationFinalisedOnTargetUpdated(@JsonProperty("id") final UUID id, @JsonProperty("hearingId") final UUID hearingId, @JsonProperty("hearingDay") final LocalDate hearingDay, @JsonProperty("applicationFinalised") final boolean applicationFinalised) {
        this.id = id;
        this.hearingId = hearingId;
        this.hearingDay = hearingDay;
        this.applicationFinalised = applicationFinalised;
    }

    public UUID getId() {
        return id;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public LocalDate getHearingDay() {
        return hearingDay;
    }

    public boolean isApplicationFinalised() {
        return applicationFinalised;
    }

    public static ApplicationFinalisedOnTargetUpdatedBuilder builder() {
        return new ApplicationFinalisedOnTargetUpdatedBuilder();
    }

    public static final class ApplicationFinalisedOnTargetUpdatedBuilder {
        private UUID id;

        private UUID hearingId;

        private LocalDate hearingDay;

        private boolean applicationFinalised;

        private ApplicationFinalisedOnTargetUpdatedBuilder() {
        }

        public ApplicationFinalisedOnTargetUpdatedBuilder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public ApplicationFinalisedOnTargetUpdatedBuilder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApplicationFinalisedOnTargetUpdatedBuilder withHearingDay(final LocalDate hearingDay) {
            this.hearingDay = hearingDay;
            return this;
        }

        public ApplicationFinalisedOnTargetUpdatedBuilder withApplicationFinalised(final boolean applicationFinalised) {
            this.applicationFinalised = applicationFinalised;
            return this;
        }

        public ApplicationFinalisedOnTargetUpdated build() {
            return new ApplicationFinalisedOnTargetUpdated(id, hearingId, hearingDay, applicationFinalised);
        }
    }
}
