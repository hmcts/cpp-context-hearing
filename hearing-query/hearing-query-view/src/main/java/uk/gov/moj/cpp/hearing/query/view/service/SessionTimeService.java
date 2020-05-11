package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.UUID.fromString;

import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.moj.cpp.hearing.common.SessionTimeUUIDService;
import uk.gov.moj.cpp.hearing.persist.entity.sessiontime.SessionTime;
import uk.gov.moj.cpp.hearing.query.view.response.SessionTimeResponse;
import uk.gov.moj.cpp.hearing.repository.SessionTimeRepository;

import java.time.LocalDate;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.json.JsonObject;
import javax.ws.rs.NotFoundException;

@ApplicationScoped
public class SessionTimeService {

    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;

    @Inject
    private UtcClock utcClock;

    @Inject
    private SessionTimeUUIDService uuidService;

    @Inject
    private SessionTimeRepository sessionTimeRepository;

    public SessionTimeResponse getSessionTime(final JsonObject payload) {
        final UUID courtHouseId = fromString(payload.getString("courtHouseId"));
        final UUID courtRoomId = fromString(payload.getString("courtRoomId"));

        final LocalDate courtSessionDate = utcClock.now().toLocalDate();

        final UUID courtSessionId = uuidService.getCourtSessionId(courtHouseId, courtRoomId, courtSessionDate);

        final SessionTime sessionTime = sessionTimeRepository.findBy(courtSessionId);

        if (sessionTime == null) {
            throw new NotFoundException(format("No record found for the specified court house (%s), court room (%s) and the date (%s)",
                    courtHouseId, courtRoomId, courtSessionDate));
        }

        SessionTimeResponse.Builder builder = SessionTimeResponse.builder()
                .withCourtRoomId(sessionTime.getCourtRoomId())
                .withCourtHouseId(sessionTime.getCourtHouseId())
                .withCourtSessionId(sessionTime.getCourtSessionId())
                .withCourtSessionDate(sessionTime.getCourtSessionDate());

        if (nonNull(sessionTime.getAmCourtSession())) {
            builder = builder.withAMCourtSession(stringToJsonObjectConverter.convert(sessionTime.getAmCourtSession().toString()));
        }
        if (nonNull(sessionTime.getPmCourtSession())) {
            builder = builder.withPMCourtSession(stringToJsonObjectConverter.convert(sessionTime.getPmCourtSession().toString()));
        }
        return builder.build();
    }


}
