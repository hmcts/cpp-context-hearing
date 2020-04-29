package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.compute-outstanding-fines-requested")
public class OutstandingFinesQueried {

    private UUID courtCentreId;
    private List<UUID> courtRoomIds;
    private LocalDate hearingDate;

    public OutstandingFinesQueried() {
    }

    @JsonCreator
    public OutstandingFinesQueried(@JsonProperty(value = "courtCentreId", required = true) final UUID courtCentreId,
                                   @JsonProperty(value = "courtRoomIds", required = true) final List<UUID> courtRoomIds,
                                   @JsonProperty(value = "hearingDate", required = true) final LocalDate hearingDate) {
        this.courtCentreId = courtCentreId;
        this.courtRoomIds = courtRoomIds;
        this.hearingDate = hearingDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final OutstandingFinesQueried copy) {
        Builder builder = new Builder();
        builder.courtCentreId = copy.getCourtCentreId();
        builder.courtRoomIds = copy.getCourtRoomIds();
        builder.hearingDate = copy.getHearingDate();
        return builder;
    }

    public UUID getCourtCentreId() {
        return courtCentreId;
    }

    public List<UUID> getCourtRoomIds() {
        return courtRoomIds;
    }

    public LocalDate getHearingDate() {
        return hearingDate;
    }


    public static final class Builder {
        private UUID courtCentreId;
        private List<UUID> courtRoomIds;
        private LocalDate hearingDate;

        private Builder() {
        }

        public Builder withCourtCentreId(final UUID courtCentreId) {
            this.courtCentreId = courtCentreId;
            return this;
        }

        public Builder withCourtRoomIds(final List<UUID> courtRoomIds) {
            this.courtRoomIds = courtRoomIds;
            return this;
        }

        public Builder withHearingDate(final LocalDate hearingDate) {
            this.hearingDate = hearingDate;
            return this;
        }

        public OutstandingFinesQueried build() {
            return new OutstandingFinesQueried(courtCentreId, courtRoomIds, hearingDate);
        }
    }
}
