package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.associate-hearing-id-with-offence")
public final class AssociateHearingIdWithOffence {

    private final UUID offenceId;

    private final UUID hearingId;

    private AssociateHearingIdWithOffence(final UUID offenceId, final UUID hearingId) {
        this.offenceId = offenceId;
        this.hearingId = hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private UUID offenceId;

        private UUID hearingId;

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public AssociateHearingIdWithOffence build() {
            return new AssociateHearingIdWithOffence(offenceId, hearingId);
        }
    }
}
