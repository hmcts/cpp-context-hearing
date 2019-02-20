package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class PleaUpdateEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(PleaUpdateEventListener.class);

    @Inject
    private OffenceRepository offenceRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private PleaJPAMapper pleaJpaMapper;

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelope) {
        LOGGER.debug("hearing.hearing-offence-plea-updated event received {}", envelope.payloadAsJsonObject());

        final PleaUpsert event = convertToObject(envelope);

        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(event.getPlea().getOffenceId(), event.getHearingId()));

        if (nonNull(offence)) {
            offence.setPlea(pleaJpaMapper.toJPA(event.getPlea()));
            offenceRepository.save(offence);
        }
    }

    private PleaUpsert convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), PleaUpsert.class);
    }
}
