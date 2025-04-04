package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

@Event("hearing.event.court-application-hearing-deleted")
public class CourtApplicationHearingDeleted {
    private static final long serialVersionUID = 1057257114242384475L;

    private final UUID hearingId;

    @JsonCreator
    public CourtApplicationHearingDeleted(final UUID hearingId) {
        this.hearingId = hearingId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public static Builder courtApplicationHearingDeleted() {
        return new uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted.Builder();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted that = (uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted) obj;

        return java.util.Objects.equals(this.hearingId, that.hearingId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(hearingId);}

    public static class Builder {
        private UUID hearingId;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withValuesFrom(final CourtApplicationHearingDeleted courtApplicationHearingDeleted) {
            this.hearingId = courtApplicationHearingDeleted.getHearingId();
            return this;
        }

        public CourtApplicationHearingDeleted build() {
            return new uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted(hearingId);
        }
    }
}
