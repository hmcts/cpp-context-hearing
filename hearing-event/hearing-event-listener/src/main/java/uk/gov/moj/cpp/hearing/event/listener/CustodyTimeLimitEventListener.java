package uk.gov.moj.cpp.hearing.event.listener;


import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitClockStopped;
import uk.gov.moj.cpp.hearing.domain.event.CustodyTimeLimitExtended;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.UUID;

import javax.inject.Inject;

@ServiceComponent(EVENT_LISTENER)
public class CustodyTimeLimitEventListener {

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.event.custody-time-limit-clock-stopped")
    public void custodyTimeLimitClockStopped(final JsonEnvelope event) {

        final CustodyTimeLimitClockStopped custodyTimeLimitClockStopped = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CustodyTimeLimitClockStopped.class);
        final UUID hearingId = custodyTimeLimitClockStopped.getHearingId();

        custodyTimeLimitClockStopped.getOffenceIds().forEach(offenceId ->
        {
            final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceId, hearingId));
            offence.setCtlTimeLimit(null);
            offence.setCtlDaysSpent(null);
            offence.setCtlClockStopped(true);
            offenceRepository.save(offence);
        });

    }

    @Handles("hearing.event.custody-time-limit-extended")
    public void custodyTimeLimitExtended(final JsonEnvelope event) {

        final CustodyTimeLimitExtended custodyTimeLimitExtended = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), CustodyTimeLimitExtended.class);
        final UUID hearingId = custodyTimeLimitExtended.getHearingId();
        final UUID offenceId = custodyTimeLimitExtended.getOffenceId();

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceId, hearingId));
        if (offence != null) {
            offence.setCtlTimeLimit(custodyTimeLimitExtended.getExtendedTimeLimit());
            offence.setCtlExtended(true);
            offenceRepository.save(offence);
        }
    }

}
