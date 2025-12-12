package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.UUID;

@Event("hearing.defendant-legalaid-status-updated-for-hearing")
public class DefendantLegalAidStatusUpdatedForHearing implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID defendantId;

    private final UUID hearingId;

    private final String legalAidStatus;

    @JsonCreator
    public DefendantLegalAidStatusUpdatedForHearing(@JsonProperty("defendantId")final UUID defendantId,
                                                    @JsonProperty("hearingId")final UUID hearingId,
                                                    @JsonProperty("legalAidStatus") final String legalAidStatus) {
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.legalAidStatus = legalAidStatus;
    }


    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public String getLegalAidStatus() {
        return legalAidStatus;
    }

    public static Builder defendantLegalaidStatusUpdatedForHearing() {
        return new uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing.Builder();
    }

    public static class Builder {

        private UUID defendantId;

        private UUID hearingId;

        private String legalAidStatus;



        public Builder withDefendantId(final UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }

        public Builder withHearingId(final UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public Builder withLegalAidStatus(final String legalAidStatus) {
            this.legalAidStatus = legalAidStatus;
            return this;
        }

        public DefendantLegalAidStatusUpdatedForHearing build() {
            return new DefendantLegalAidStatusUpdatedForHearing(defendantId, hearingId, legalAidStatus);
        }
    }
}
