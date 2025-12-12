package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.court-application-ejected")
@SuppressWarnings("squid:S2384")
public class CourtApplicationEjected implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID applicationId;

    private List<UUID> hearingIds;

    @JsonCreator
    public CourtApplicationEjected(@JsonProperty("applicationId") UUID applicationId, @JsonProperty("hearingIds") List<UUID> hearingIds) {
        this.applicationId = applicationId;
        this.hearingIds = hearingIds;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public List<UUID> getHearingIds() {
        return hearingIds;
    }

    public static CourtApplicationEjectedBuilder aCourtApplicationEjected() {
        return new uk.gov.moj.cpp.hearing.domain.event.CourtApplicationEjected.CourtApplicationEjectedBuilder();
    }


    public static final class CourtApplicationEjectedBuilder {
        private UUID applicationId;
        private List<UUID> hearingIds;

        private CourtApplicationEjectedBuilder() {
        }

        public static CourtApplicationEjectedBuilder aCourtApplicationEjected() {
            return new CourtApplicationEjectedBuilder();
        }

        public CourtApplicationEjectedBuilder withApplicationId(UUID applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        public CourtApplicationEjectedBuilder withHearingIds(List<UUID> hearingIds) {
            this.hearingIds = hearingIds;
            return this;
        }

        public CourtApplicationEjected build() {
            return new CourtApplicationEjected(applicationId, hearingIds);
        }
    }
}
