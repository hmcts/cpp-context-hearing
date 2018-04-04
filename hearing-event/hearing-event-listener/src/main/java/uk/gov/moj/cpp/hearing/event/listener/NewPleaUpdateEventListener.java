package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.OffencePleaUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

@ServiceComponent(EVENT_LISTENER)
public class NewPleaUpdateEventListener {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(NewPleaUpdateEventListener.class);
    
    private final OffenceRepository offenceRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;
    
    @Inject
    public NewPleaUpdateEventListener(final OffenceRepository offenceRepository,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        assert null != offenceRepository && null != jsonObjectToObjectConverter;
        this.offenceRepository = offenceRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelop) {
        final OffencePleaUpdated plea = convertToObject(envelop);
        final Offence offence = offenceRepository.findById(new HearingSnapshotKey(plea.getOffenceId(), plea.getOriginHearingId()));
        if (null != offence) {
            offence.setPleaDate(plea.getPleaDate().atStartOfDay());
            offence.setPleaValue(plea.getValue());
            offenceRepository.save(offence);
        } else {
            //TODO: GPE-3032: Should I produce some event here?
            LOGGER.error("Entity offence not found by offenceId: " + plea.getOffenceId() + " and hearingId: " + plea.getOriginHearingId());
        }
    }

    private OffencePleaUpdated convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), OffencePleaUpdated.class);
    }
}