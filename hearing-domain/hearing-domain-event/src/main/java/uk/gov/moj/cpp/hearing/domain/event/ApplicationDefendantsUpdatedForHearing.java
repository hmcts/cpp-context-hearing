package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.UUID;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.domain.annotation.Event;

@Event("hearing.application-defendants-updated-for-hearing")
public class ApplicationDefendantsUpdatedForHearing implements Serializable {
    private static final long serialVersionUID = 1L;

    private CourtApplication courtApplication;

    private UUID hearingId;

    @JsonCreator
    public ApplicationDefendantsUpdatedForHearing(@JsonProperty("courtApplication") CourtApplication courtApplication, @JsonProperty("hearingId") UUID hearingId) {
        this.courtApplication = courtApplication;
        this.hearingId = hearingId;
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public UUID getHearingId() {
        return hearingId;
    }
    public static ApplicationDefendantsUpdatedForHearing.ApplicationDefendantsUpdatedForHearingBuilder applicationDefendantsUpdatedForHearing() {
        return new ApplicationDefendantsUpdatedForHearing.ApplicationDefendantsUpdatedForHearingBuilder();
    }
    public static final class ApplicationDefendantsUpdatedForHearingBuilder {
        private CourtApplication courtApplication;
        private UUID hearingId;

        private ApplicationDefendantsUpdatedForHearingBuilder() {
        }

        public ApplicationDefendantsUpdatedForHearing.ApplicationDefendantsUpdatedForHearingBuilder withCourtApplication(CourtApplication courtApplication) {
            this.courtApplication = courtApplication;
            return this;
        }

        public ApplicationDefendantsUpdatedForHearing.ApplicationDefendantsUpdatedForHearingBuilder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public ApplicationDefendantsUpdatedForHearing build() {
            return new ApplicationDefendantsUpdatedForHearing(courtApplication, hearingId);
        }
    }
}
