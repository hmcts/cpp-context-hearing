package uk.gov.moj.cpp.hearing.common;

import static java.util.UUID.nameUUIDFromBytes;

import java.time.LocalDate;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SessionTimeUUIDService {
    public UUID getCourtSessionId(final UUID courtHsId,
                                  final UUID courtRmId,
                                  final LocalDate courtSessionDt) {
        final String courtHouseId = courtHsId.toString();
        final String courtRoomId = courtRmId.toString();
        final String courtSessionDate = courtSessionDt.toString();
        final String courtSessionId = String.format("%s/%s/%s", courtHouseId, courtRoomId, courtSessionDate);
        return nameUUIDFromBytes(courtSessionId.getBytes());
    }
}
