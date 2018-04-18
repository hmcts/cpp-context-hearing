package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.handler.exception.HandlerExecutionException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingOffencePleaUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

@ServiceComponent(EVENT_LISTENER)
public class NewModelPleaUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NewModelPleaUpdateEventListener.class);

    private final OffenceRepository offenceRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    
    @Inject
    public NewModelPleaUpdateEventListener(final OffenceRepository offenceRepository,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        assert null != offenceRepository && null != jsonObjectToObjectConverter;
        this.offenceRepository = offenceRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelop) {
        LOGGER.info("update plea: " + envelop.toString() );

        final HearingOffencePleaUpdated event = convertToObject(envelop);
        Optional.ofNullable(
                offenceRepository.findBySnapshotKey(new HearingSnapshotKey(event.getOffenceId(), event.getHearingId())))
                .map(offence -> {
                    offence.setPleaDate(event.getPleaDate());
                    offence.setPleaValue(event.getValue());
                    offenceRepository.saveAndFlush(offence);
                    return offence;
                }).orElseThrow(() -> new HandlerExecutionException("Offence not found by offenceId: " + event.getOffenceId() + " and hearingId: " + event.getHearingId(), null));
        final Offence offence = offenceRepository.findByOffenceIdOriginHearingId(event.getOffenceId(), event.getHearingId());
        if (null != offence) {
            offence.setPleaDate(event.getPleaDate());
            offence.setPleaValue(event.getValue());
            offenceRepository.saveAndFlush(offence);
        }
    }

    private HearingOffencePleaUpdated convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), HearingOffencePleaUpdated.class);
    }
}
