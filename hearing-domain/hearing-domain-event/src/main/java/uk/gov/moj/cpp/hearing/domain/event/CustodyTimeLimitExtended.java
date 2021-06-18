package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Event("hearing.event.custody-time-limit-extended")
public class CustodyTimeLimitExtended implements Serializable {

    private static final long serialVersionUID = 2676771339826701314L;

    private UUID hearingId;
    private UUID offenceId;
    private LocalDate extendedTimeLimit;

    public CustodyTimeLimitExtended(final UUID hearingId, final UUID offenceId, final LocalDate extendedTimeLimit) {
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.extendedTimeLimit = extendedTimeLimit;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public LocalDate getExtendedTimeLimit() {
        return extendedTimeLimit;
    }

    public static Builder custodyTimeLimitExtended() {
        return new Builder();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CustodyTimeLimitExtended that = (CustodyTimeLimitExtended) o;
        return Objects.equals(hearingId, that.hearingId) &&
                Objects.equals(offenceId, that.offenceId) &&
                Objects.equals(extendedTimeLimit, that.extendedTimeLimit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hearingId, offenceId, extendedTimeLimit);
    }

    @Override
    public String toString() {
        return "CustodyTimeLimitExtended{" +
                "hearingId=" + hearingId +
                ", offenceId=" + offenceId +
                ", extendedTimeLimit=" + extendedTimeLimit +
                '}';
    }

    public static class Builder {
        private UUID hearingId;
        private UUID offenceId;
        private LocalDate extendedTimeLimit;

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withOffenceId(final UUID offenceId) {
            this.offenceId = offenceId;
            return this;
        }

        public Builder withExtendedTimeLimit(final LocalDate extendedTimeLimit) {
            this.extendedTimeLimit = extendedTimeLimit;
            return this;
        }

        public CustodyTimeLimitExtended build() {
            return new CustodyTimeLimitExtended(hearingId, offenceId, extendedTimeLimit);
        }
    }
}
