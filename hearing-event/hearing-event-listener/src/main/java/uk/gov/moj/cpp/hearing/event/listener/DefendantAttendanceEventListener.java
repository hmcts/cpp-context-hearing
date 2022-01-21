package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefendantAttendance;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantAttendanceRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2201"})
@ServiceComponent(EVENT_LISTENER)
public class DefendantAttendanceEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantAttendanceEventListener.class.getName());

    @Inject
    private DefendantAttendanceRepository defendantAttendanceRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.defendant-attendance-updated")
    public void updateDefendantAttendance(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();

        final DefendantAttendanceUpdated defendantAttendanceUpdated = jsonObjectToObjectConverter.convert(payload, DefendantAttendanceUpdated.class);

        DefendantAttendance defendantAttendance = defendantAttendanceRepository.findByHearingIdDefendantIdAndDate(defendantAttendanceUpdated.getHearingId(), defendantAttendanceUpdated.getDefendantId(), defendantAttendanceUpdated.getAttendanceDay().getDay());

        LOGGER.debug("hearing.defendant-attendance-updated event received for hearingId {} and defendantId {}", defendantAttendanceUpdated.getHearingId(), defendantAttendanceUpdated.getDefendantId());

        if (null == defendantAttendance) {
            defendantAttendance = new DefendantAttendance();
            defendantAttendance.setId(new HearingSnapshotKey(UUID.randomUUID(), defendantAttendanceUpdated.getHearingId()));
            defendantAttendance.setDefendantId(defendantAttendanceUpdated.getDefendantId());
            defendantAttendance.setDay(defendantAttendanceUpdated.getAttendanceDay().getDay());
        }
        defendantAttendance.setAttendanceType(defendantAttendanceUpdated.getAttendanceDay().getAttendanceType());
        defendantAttendanceRepository.save(defendantAttendance);
    }
}
