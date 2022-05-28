package uk.gov.moj.cpp.hearing.event.listener;


import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantLegalAidStatusUpdatedForHearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.DefendantRepository;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class DefendantLegalAidStatusUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefendantAttendanceEventListener.class.getName());

    @Inject
    private DefendantRepository defendantRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.defendant-legalaid-status-updated-for-hearing")
    public void updateDefendantLegalAidStatusForHearing(final JsonEnvelope event) {

        final JsonObject payload = event.payloadAsJsonObject();
        final DefendantLegalAidStatusUpdatedForHearing defendantLegalAidStatusUpdated = jsonObjectToObjectConverter.convert(payload, DefendantLegalAidStatusUpdatedForHearing.class);

        final UUID defendantId = defendantLegalAidStatusUpdated.getDefendantId();
        final String legalAidStatus = defendantLegalAidStatusUpdated.getLegalAidStatus();
        final UUID hearingId = defendantLegalAidStatusUpdated.getHearingId();
        final Defendant defendant = defendantRepository.findBy(new HearingSnapshotKey(defendantId, hearingId));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.defendant-legalaid-status-updated-for-hearing event received for hearingId {}", hearingId);
        }


        if ("NO_VALUE".equals(legalAidStatus)) {
            defendant.setLegalaidStatus(null);
        } else {
            defendant.setLegalaidStatus(legalAidStatus);
        }
        defendantRepository.save(defendant);

    }

}
