package uk.gov.moj.cpp.hearing.command.sessiontime;

import java.util.Objects;
import java.util.UUID;

public class RecordSessionTime {

    private UUID courtHouseId;
    private UUID courtRoomId;
    private CourtSession amCourtSession;
    private CourtSession pmCourtSession;

    public RecordSessionTime(final UUID courtHouseId,
                             final UUID courtRoomId,
                             final CourtSession amCourtSession,
                             final CourtSession pmCourtSession) {
        this.courtHouseId = courtHouseId;
        this.courtRoomId = courtRoomId;
        this.amCourtSession = amCourtSession;
        this.pmCourtSession = pmCourtSession;
    }

    public UUID getCourtHouseId() {
        return courtHouseId;
    }

    public void setCourtHouseId(UUID courtHouseId) {
        this.courtHouseId = courtHouseId;
    }

    public UUID getCourtRoomId() {
        return courtRoomId;
    }

    public void setCourtRoomId(UUID courtRoomId) {
        this.courtRoomId = courtRoomId;
    }

    public CourtSession getAmCourtSession() {
        return amCourtSession;
    }

    public void setAmCourtSession(CourtSession amCourtSession) {
        this.amCourtSession = amCourtSession;
    }

    public CourtSession getPmCourtSession() {
        return pmCourtSession;
    }

    public void setPmCourtSession(CourtSession pmCourtSession) {
        this.pmCourtSession = pmCourtSession;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RecordSessionTime that = (RecordSessionTime) o;
        return Objects.equals(courtHouseId, that.courtHouseId) &&
                Objects.equals(courtRoomId, that.courtRoomId) &&
                Objects.equals(amCourtSession, that.amCourtSession) &&
                Objects.equals(pmCourtSession, that.pmCourtSession);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courtHouseId, courtRoomId, amCourtSession, pmCourtSession);
    }

    @Override
    public String toString() {
        return "RecordSessionTime{" +
                "courtHouseId=" + courtHouseId +
                ", courtRoomId=" + courtRoomId +
                ", amCourtSession=" + amCourtSession +
                ", pmCourtSession=" + pmCourtSession +
                '}';
    }
}
