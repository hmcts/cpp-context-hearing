package uk.gov.moj.cpp.hearing.domain.event.sessiontime;

import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.event.session-time-recorded")
public class SessionTimeRecorded implements Serializable {
    private static final long serialVersionUID = -5945603472266627801L;

    private UUID courtSessionId;
    private UUID courtHouseId;
    private UUID courtRoomId;
    private LocalDate courtSessionDate;
    private CourtSession amCourtSession;
    private CourtSession pmCourtSession;


    @JsonCreator
    public SessionTimeRecorded(@JsonProperty("courtSessionId") final UUID courtSessionId,
                               @JsonProperty("courtHouseId") final UUID courtHouseId,
                               @JsonProperty("courtRoomId") final UUID courtRoomId,
                               @JsonProperty("courtSessionDate") final LocalDate courtSessionDate,
                               @JsonProperty("amCourtSession") final CourtSession amCourtSession,
                               @JsonProperty("pmCourtSession") final CourtSession pmCourtSession) {
        this.courtSessionId = courtSessionId;
        this.courtHouseId = courtHouseId;
        this.courtRoomId = courtRoomId;
        this.courtSessionDate = courtSessionDate;
        this.amCourtSession = amCourtSession;
        this.pmCourtSession = pmCourtSession;
    }

    public CourtSession getAmCourtSession() {
        return amCourtSession;
    }

    public UUID getCourtHouseId() {
        return courtHouseId;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public LocalDate getCourtSessionDate() {
        return courtSessionDate;
    }

    public UUID getCourtSessionId() {
        return courtSessionId;
    }

    public CourtSession getPmCourtSession() {
        return pmCourtSession;
    }

    public static Builder sessionTimeRecorded() {
        return new SessionTimeRecorded.Builder();
    }

    @Override
    @SuppressWarnings({"squid:S00121","squid:S1067"})
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        final SessionTimeRecorded that = (SessionTimeRecorded) obj;

        return java.util.Objects.equals(this.amCourtSession, that.amCourtSession) &&
                java.util.Objects.equals(this.courtHouseId, that.courtHouseId) &&
                java.util.Objects.equals(this.courtRoomId, that.courtRoomId) &&
                java.util.Objects.equals(this.courtSessionDate, that.courtSessionDate) &&
                java.util.Objects.equals(this.courtSessionId, that.courtSessionId) &&
                java.util.Objects.equals(this.pmCourtSession, that.pmCourtSession);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(amCourtSession, courtHouseId, courtRoomId, courtSessionDate, courtSessionId, pmCourtSession);
    }

    @Override
    public String toString() {
        return "SessionTimeRecorded{" +
                "amCourtSession='" + amCourtSession + "'," +
                "courtHouseId='" + courtHouseId + "'," +
                "courtRoomId='" + courtRoomId + "'," +
                "courtSessionDate='" + courtSessionDate + "'," +
                "courtSessionId='" + courtSessionId + "'," +
                "pmCourtSession='" + pmCourtSession + "'" +
                "}";
    }

    public SessionTimeRecorded setAmCourtSession(CourtSession amCourtSession) {
        this.amCourtSession = amCourtSession;
        return this;
    }

    public SessionTimeRecorded setCourtHouseId(UUID courtHouseId) {
        this.courtHouseId = courtHouseId;
        return this;
    }

    public SessionTimeRecorded setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
        return this;
    }

    public SessionTimeRecorded setCourtSessionDate(LocalDate courtSessionDate) {
        this.courtSessionDate = courtSessionDate;
        return this;
    }

    public SessionTimeRecorded setCourtSessionId(UUID courtSessionId) {
        this.courtSessionId = courtSessionId;
        return this;
    }

    public SessionTimeRecorded setPmCourtSession(CourtSession pmCourtSession) {
        this.pmCourtSession = pmCourtSession;
        return this;
    }

    public static class Builder {
        private CourtSession amCourtSession;

        private UUID courtHouseId;

        private UUID courtRoomId;

        private LocalDate courtSessionDate;

        private UUID courtSessionId;

        private CourtSession pmCourtSession;

        public Builder withAmCourtSession(final CourtSession amCourtSession) {
            this.amCourtSession = amCourtSession;
            return this;
        }

        public Builder withCourtHouseId(final UUID courtHouseId) {
            this.courtHouseId = courtHouseId;
            return this;
        }

        public Builder withCourtRoomId(final UUID courtRoomId) {
            this.courtRoomId = courtRoomId;
            return this;
        }

        public Builder withCourtSessionDate(final LocalDate courtSessionDate) {
            this.courtSessionDate = courtSessionDate;
            return this;
        }

        public Builder withCourtSessionId(final UUID courtSessionId) {
            this.courtSessionId = courtSessionId;
            return this;
        }

        public Builder withPmCourtSession(final CourtSession pmCourtSession) {
            this.pmCourtSession = pmCourtSession;
            return this;
        }

        public SessionTimeRecorded build() {
            return new SessionTimeRecorded(courtSessionId, courtHouseId, courtRoomId, courtSessionDate, amCourtSession,pmCourtSession);
        }
    }
}
