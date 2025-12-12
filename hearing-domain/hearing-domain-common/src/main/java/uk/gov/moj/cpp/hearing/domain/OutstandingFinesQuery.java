package uk.gov.moj.cpp.hearing.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class OutstandingFinesQuery {

    private UUID courtCentreId;
    private List<UUID> courtRoomIds;
    private LocalDate hearingDate;

    public OutstandingFinesQuery() {
    }

    public OutstandingFinesQuery(final UUID courtCentreId, final List<UUID> courtRoomIds, final LocalDate hearingDate) {
        this.courtCentreId = courtCentreId;
        this.courtRoomIds = courtRoomIds;
        this.hearingDate = hearingDate;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static Builder newBuilder(final OutstandingFinesQuery copy) {
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

        public OutstandingFinesQuery build() {
            return new OutstandingFinesQuery(courtCentreId, courtRoomIds, hearingDate);
        }
    }
}
