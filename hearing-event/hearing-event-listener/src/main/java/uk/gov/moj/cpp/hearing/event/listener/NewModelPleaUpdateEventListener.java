package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

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
        final HearingOffencePleaUpdated plea = convertToObject(envelop);
        final HearingSnapshotKey snapshotKey = new HearingSnapshotKey(plea.getOffenceId(), plea.getHearingId());
        final Offence offence = offenceRepository.findBySnapshotKey(snapshotKey);
        Optional.ofNullable(offence).map(o -> {
            o.setPleaDate(plea.getPleaDate());
            o.setPleaValue(plea.getValue());
            offenceRepository.save(o);
            return o;
        }).orElseThrow(() -> new HandlerExecutionException("Entity offence not found by: " + snapshotKey, null));
    }

    private HearingOffencePleaUpdated convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), HearingOffencePleaUpdated.class);
    }
}