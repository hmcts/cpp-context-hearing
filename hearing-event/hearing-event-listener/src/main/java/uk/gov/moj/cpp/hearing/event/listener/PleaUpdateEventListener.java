package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.List;
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
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

@ServiceComponent(EVENT_LISTENER)
public class PleaUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PleaUpdateEventListener.class);

    private final OffenceRepository offenceRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    
    @Inject
    public PleaUpdateEventListener(final OffenceRepository offenceRepository,
                                   final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.offenceRepository = offenceRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelope) {
        LOGGER.debug("hearing.hearing-offence-plea-updated event received {}", envelope.payloadAsJsonObject());

        final PleaUpsert event = convertToObject(envelope);

        final Offence offence = offenceRepository.findBySnapshotKey(new HearingSnapshotKey(event.getOffenceId(), event.getHearingId()));

        if (offence != null) {

            offence.setPleaDate(event.getPleaDate());
            offence.setPleaValue(event.getValue());
            offenceRepository.save(offence);

            final List<Offence> inheritedOffences = offenceRepository.findByOffenceIdOriginHearingId(event.getOffenceId(), event.getHearingId());
            for (Offence inheritedOffence : inheritedOffences) {
                inheritedOffence.setPleaDate(event.getPleaDate());
                inheritedOffence.setPleaValue(event.getValue());
                offenceRepository.save(inheritedOffence);
            }
        }
    }

    private PleaUpsert convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), PleaUpsert.class);
    }
}
