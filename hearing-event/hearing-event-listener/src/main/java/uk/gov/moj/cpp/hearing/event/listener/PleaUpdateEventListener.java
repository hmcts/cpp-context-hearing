package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.PleaUpsert;
import uk.gov.moj.cpp.hearing.mapping.AllocationDecisionJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.IndicatedPleaJPAMapper;
import uk.gov.moj.cpp.hearing.mapping.PleaJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Offence;
import uk.gov.moj.cpp.hearing.repository.OffenceRepository;

import java.util.Objects;
import java.util.UUID;

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

    @Inject
    private IndicatedPleaJPAMapper indicatedPleaJPAMapper;

    @Inject
    private AllocationDecisionJPAMapper allocationDecisionJPAMapper;

    @Transactional
    @Handles("hearing.hearing-offence-plea-updated")
    public void offencePleaUpdated(final JsonEnvelope envelope) {
        LOGGER.debug("hearing.hearing-offence-plea-updated event received {}", envelope.payloadAsJsonObject());

        final PleaUpsert event = convertToObject(envelope);

        final UUID offenceId = event.getPleaModel().getOffenceId();
        final Offence offence = offenceRepository.findBy(new HearingSnapshotKey(offenceId, event.getHearingId()));

        if (nonNull(offence)) {
            if (Objects.nonNull(event.getPleaModel().getPlea())) {
                offence.setPlea(pleaJpaMapper.toJPA(event.getPleaModel().getPlea()));
            } else {
                offence.setPlea(null);
            }
            if (Objects.nonNull(event.getPleaModel().getAllocationDecision())) {
                offence.setAllocationDecision(allocationDecisionJPAMapper.toJPA(event.getPleaModel().getAllocationDecision()));
            } else {
                offence.setAllocationDecision(null);
            }
            if (Objects.nonNull(event.getPleaModel().getIndicatedPlea())) {
                offence.setIndicatedPlea(indicatedPleaJPAMapper.toJPA(event.getPleaModel().getIndicatedPlea()));
            } else {
                offence.setIndicatedPlea(null);
            }
            offenceRepository.save(offence);
        }
    }

    private PleaUpsert convertToObject(final JsonEnvelope envelop) {
        return this.jsonObjectToObjectConverter.convert(envelop.payloadAsJsonObject(), PleaUpsert.class);
    }
}
