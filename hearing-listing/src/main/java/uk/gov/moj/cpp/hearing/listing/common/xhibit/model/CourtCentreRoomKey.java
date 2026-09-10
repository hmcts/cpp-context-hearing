package uk.gov.moj.cpp.hearing.listing.common.xhibit.model;

import java.util.Objects;
import java.util.UUID;

public class CourtCentreRoomKey {

    private UUID courtCentreId;
    private UUID courtRoomId;

    public CourtCentreRoomKey(final UUID courtCentreId, final UUID courtRoomId) {
        this.courtCentreId = courtCentreId;
        this.courtRoomId = courtRoomId;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CourtCentreRoomKey that = (CourtCentreRoomKey) o;
        return courtCentreId.equals(that.courtCentreId) &&
                courtRoomId.equals(that.courtRoomId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courtCentreId, courtRoomId);
    }
}
