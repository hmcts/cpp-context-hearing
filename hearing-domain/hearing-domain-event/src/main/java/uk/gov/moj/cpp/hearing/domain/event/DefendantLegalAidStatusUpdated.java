package uk.gov.moj.cpp.hearing.domain.event;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Event("hearing.defendant-legalaid-status-updated")
public class DefendantLegalAidStatusUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID defendantId;

    private String legalAidStatus;

    private List<UUID> hearingIds;

    @JsonCreator
    public DefendantLegalAidStatusUpdated(@JsonProperty("defendantId") UUID defendantId,
                                          @JsonProperty("legalAidStatus") String legalAidStatus , @JsonProperty("hearingIds") List<UUID> hearingIds) {
        this.defendantId = defendantId;
        this.legalAidStatus = legalAidStatus;
        this.hearingIds = hearingIds;
    }


    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public String getLegalAidStatus() {
        return legalAidStatus;
    }


    public static DefendantLegalAidStatusUpdated.DefendantLegalAidStatusUpdatedBuilder defendantLegalAidStatusUpdatedBuilder() {
        return new uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdated.DefendantLegalAidStatusUpdatedBuilder();
    }
    public static final class DefendantLegalAidStatusUpdatedBuilder {
        private UUID defendantId;

        private String legalAidStatus;

        private List<UUID> hearingIds;

        private DefendantLegalAidStatusUpdatedBuilder() {
        }

        public DefendantLegalAidStatusUpdated.DefendantLegalAidStatusUpdatedBuilder withDefendantId(UUID defendantId) {
            this.defendantId = defendantId;
            return this;
        }


        public DefendantLegalAidStatusUpdated.DefendantLegalAidStatusUpdatedBuilder withLegalAidStatus(String legalAidStatus) {
            this.legalAidStatus = legalAidStatus;
            return this;
        }

        public DefendantLegalAidStatusUpdated.DefendantLegalAidStatusUpdatedBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = hearingIds;
            return this;
        }

        public DefendantLegalAidStatusUpdated build() {
            return new DefendantLegalAidStatusUpdated(defendantId, legalAidStatus, hearingIds);
        }
    }
}
