package uk.gov.moj.cpp.hearing.query.view.response;

import java.time.LocalDate;
import java.util.UUID;

import javax.json.JsonObject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class SessionTimeResponse {

    private final UUID courtSessionId;
    private final LocalDate courtSessionDate;
    private final UUID courtHouseId;
    private final UUID courtRoomId;
    private final JsonObject amCourtSession;
    private final JsonObject pmCourtSession;

    @JsonCreator
    public SessionTimeResponse(@JsonProperty(value = "courtSessionId") final UUID courtSessionId,
                               @JsonProperty(value = "courtSessionDate") final LocalDate courtSessionDate,
                               @JsonProperty(value = "courtHouseId") final UUID courtHouseId,
                               @JsonProperty(value = "courtRoomId") final UUID courtRoomId,
                               @JsonProperty(value = "amCourtSession") final JsonObject amCourtSession,
                               @JsonProperty(value = "pmCourtSession") final JsonObject pmCourtSession) {
        this.courtSessionId = courtSessionId;
        this.courtSessionDate = courtSessionDate;
        this.courtHouseId = courtHouseId;
        this.courtRoomId = courtRoomId;
        this.amCourtSession = amCourtSession;
        this.pmCourtSession = pmCourtSession;
    }

    public SessionTimeResponse(Builder builder) {
        this.courtSessionId = builder.courtSessionId;
        this.courtSessionDate = builder.courtSessionDate;
        this.courtHouseId = builder.courtHouseId;
        this.courtRoomId = builder.courtRoomId;
        this.amCourtSession = builder.amCourtSession;
        this.pmCourtSession = builder.pmCourtSession;
    }

    public static SessionTimeResponse.Builder builder() {
        return new SessionTimeResponse.Builder();
    }

    public UUID getCourtSessionId() {
        return courtSessionId;
    }

    public LocalDate getCourtSessionDate() {
        return courtSessionDate;
    }

    public UUID getCourtHouseId() {
        return courtHouseId;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public JsonObject getAmCourtSession() {
        return amCourtSession;
    }

    public JsonObject getPmCourtSession() {
        return pmCourtSession;
    }

    public static class Builder {

        private UUID courtSessionId;
        private LocalDate courtSessionDate;
        private UUID courtHouseId;
        private UUID courtRoomId;
        private JsonObject amCourtSession;
        private JsonObject pmCourtSession;

        public Builder withCourtSessionId(final UUID courtSessionId) {
            this.courtSessionId = courtSessionId;
            return this;
        }

        public Builder withCourtSessionDate(final LocalDate courtSessionDate) {
            this.courtSessionDate = courtSessionDate;
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

        public Builder withAMCourtSession(final JsonObject amCourtSession) {
            this.amCourtSession = amCourtSession;
            return this;
        }

        public Builder withPMCourtSession(final JsonObject pmCourtSession) {
            this.pmCourtSession = pmCourtSession;
            return this;
        }

        public SessionTimeResponse build() {
            return new SessionTimeResponse(this);
        }
    }
}
