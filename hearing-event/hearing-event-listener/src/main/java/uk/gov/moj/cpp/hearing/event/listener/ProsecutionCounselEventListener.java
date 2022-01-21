package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingProsecutionCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingProsecutionCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00112", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class ProsecutionCounselEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProsecutionCounselEventListener.class.getName());

    public static final String HEARING_NOT_FOUND = "Hearing not found";

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingProsecutionCounselRepository hearingProsecutionCounselRepository;

    @Inject
    private HearingProsecutionCounselJPAMapper hearingProsecutionCounselJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Transactional
    @Handles("hearing.prosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope envelope) {

        final ProsecutionCounselAdded prosecutionCounselAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ProsecutionCounselAdded.class);
        final Hearing hearing = hearingRepository.findBy(prosecutionCounselAdded.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-added for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel hearingProsecutionCounsel = hearingProsecutionCounselJPAMapper.toJPA(hearing, prosecutionCounselAdded.getProsecutionCounsel());
            hearingProsecutionCounsel.setId(new HearingSnapshotKey(prosecutionCounselAdded.getProsecutionCounsel().getId(), hearing.getId()));
            hearingProsecutionCounselRepository.saveAndFlush(hearingProsecutionCounsel);
        }

    }

    @Transactional
    @Handles("hearing.prosecution-counsel-removed")
    public void prosecutionCounselRemoved(final JsonEnvelope event) {
        final ProsecutionCounselRemoved prosecutionCounselRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ProsecutionCounselRemoved.class);
        final Hearing hearing = hearingRepository.findBy(prosecutionCounselRemoved.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-removed for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error("Hearing not found");
        } else {
            final Optional<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel> hearingProsecutionCounsel =
                    hearing.getProsecutionCounsels().stream().filter(pc -> pc.getId().getId().equals(prosecutionCounselRemoved.getId()))
                            .findFirst();

            if (hearingProsecutionCounsel.isPresent()) {
                final uk.gov.moj.cpp.hearing.persist.entity.ha.HearingProsecutionCounsel softDeletePC = hearingProsecutionCounsel.get();
                softDeletePC.setDeleted(true);
                hearingProsecutionCounselRepository.saveAndFlush(softDeletePC);
            }
        }
    }

    @Transactional
    @Handles("hearing.prosecution-counsel-updated")
    public void prosecutionCounselUpdated(final JsonEnvelope envelope) {

        final ProsecutionCounselUpdated prosecutionCounselUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), ProsecutionCounselUpdated.class);
        final Hearing hearing = hearingRepository.findBy(prosecutionCounselUpdated.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.prosecution-counsel-updated for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingProsecutionCounsel hearingDefenceCounsel = hearingProsecutionCounselJPAMapper.toJPA(hearing, prosecutionCounselUpdated.getProsecutionCounsel());
            hearingDefenceCounsel.setId(new HearingSnapshotKey(prosecutionCounselUpdated.getProsecutionCounsel().getId(), hearing.getId()));
            hearingProsecutionCounselRepository.saveAndFlush(hearingDefenceCounsel);

        }

    }
}
