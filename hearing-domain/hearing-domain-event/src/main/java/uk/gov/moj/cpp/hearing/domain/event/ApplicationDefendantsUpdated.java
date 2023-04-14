package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.domain.annotation.Event;


@Event("hearing.application-defendants-updated")
public class ApplicationDefendantsUpdated implements Serializable {

    private static final long serialVersionUID = 1L;

    private CourtApplication courtApplication;

    private List<UUID> hearingIds;

    @JsonCreator
    public ApplicationDefendantsUpdated(@JsonProperty("courtApplication") CourtApplication courtApplication, @JsonProperty("hearingIds") List<UUID> hearingIds) {
        this.courtApplication = courtApplication;
        this.hearingIds = new ArrayList<>(hearingIds);
    }

    public CourtApplication getCourtApplication() {
        return courtApplication;
    }

    public List<UUID> getHearingIds() {
        return new ArrayList<>(hearingIds);
    }
    public static ApplicationDefendantsUpdated.ApplicationDefendantsUpdatedBuilder applicationDefendantsUpdatd() {
        return new ApplicationDefendantsUpdated.ApplicationDefendantsUpdatedBuilder();
    }
    public static final class ApplicationDefendantsUpdatedBuilder {
        private CourtApplication courtApplication;
        private List<UUID> hearingIds;

        private ApplicationDefendantsUpdatedBuilder() {
        }

        public ApplicationDefendantsUpdated.ApplicationDefendantsUpdatedBuilder withCourtApplication(CourtApplication courtApplication) {
            this.courtApplication = courtApplication;
            return this;
        }

        public ApplicationDefendantsUpdated.ApplicationDefendantsUpdatedBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = new ArrayList<>(hearingIds);
            return this;
        }

        public ApplicationDefendantsUpdated build() {
            return new ApplicationDefendantsUpdated(courtApplication, hearingIds);
        }
    }

}
