package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.Objects.nonNull;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryAdded;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryRemoved;
import uk.gov.moj.cpp.hearing.domain.event.InterpreterIntermediaryUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingInterpreterIntermediaryJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingInterpreterIntermediary;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingInterpreterIntermediaryRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class InterpreterIntermediaryEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterpreterIntermediaryEventListener.class.getName());

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingInterpreterIntermediaryJPAMapper interpreterIntermediaryJPAMapper;

    @Inject
    private HearingInterpreterIntermediaryRepository hearingInterpreterIntermediaryRepository;


    @Transactional
    @Handles("hearing.interpreter-intermediary-added")
    public void interpreterIntermediaryAdded(final JsonEnvelope envelope) {

        final InterpreterIntermediaryAdded interpreterIntermediaryAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), InterpreterIntermediaryAdded.class);

        final UUID hearingId = interpreterIntermediaryAdded.getHearingId();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.interpreter-intermediary-added for hearingId {} ", hearingId);
        }

        final Hearing hearingEntity = hearingRepository.findBy(hearingId);

        if (nonNull(hearingEntity)) {
            final HearingInterpreterIntermediary hearingInterpreterIntermediary = interpreterIntermediaryJPAMapper.toJPA(hearingEntity, interpreterIntermediaryAdded.getInterpreterIntermediary());
            hearingInterpreterIntermediary.setId(new HearingSnapshotKey(interpreterIntermediaryAdded.getInterpreterIntermediary().getId(), hearingEntity.getId()));
            hearingInterpreterIntermediaryRepository.saveAndFlush(hearingInterpreterIntermediary);
        }
    }

    @Transactional
    @Handles("hearing.interpreter-intermediary-removed")
    public void interpreterIntermediaryRemoved(final JsonEnvelope event) {

        final InterpreterIntermediaryRemoved interpreterIntermediaryRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InterpreterIntermediaryRemoved.class);

        final Hearing hearing = hearingRepository.findBy(interpreterIntermediaryRemoved.getHearingId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.interpreter-intermediary-removed for hearingId {} ", interpreterIntermediaryRemoved.getHearingId());
        }

        if (nonNull(hearing)) {

            final Optional<HearingInterpreterIntermediary> hearingInterpreterIntermediary =
                    hearing.getHearingInterpreterIntermediaries().stream().filter(pc -> pc.getId().getId().equals(interpreterIntermediaryRemoved.getId()))
                            .findFirst();

            if (hearingInterpreterIntermediary.isPresent()) {
                final HearingInterpreterIntermediary softDeletePC = hearingInterpreterIntermediary.get();
                softDeletePC.setDeleted(true);
                hearingInterpreterIntermediaryRepository.saveAndFlush(softDeletePC);
            }
        }
    }

    @Transactional
    @Handles("hearing.interpreter-intermediary-updated")
    public void interpreterIntermediaryUpdated(final JsonEnvelope envelope) {

        final InterpreterIntermediaryUpdated interpreterIntermediaryUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), InterpreterIntermediaryUpdated.class);

        final Hearing hearing = hearingRepository.findBy(interpreterIntermediaryUpdated.getHearingId());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.interpreter-intermediary-updated for hearingId {} ", interpreterIntermediaryUpdated.getHearingId());
        }

        if (nonNull(hearing)) {
            final HearingInterpreterIntermediary hearingInterpreterIntermediary = interpreterIntermediaryJPAMapper.toJPA(hearing, interpreterIntermediaryUpdated.getInterpreterIntermediary());
            hearingInterpreterIntermediary.setId(new HearingSnapshotKey(interpreterIntermediaryUpdated.getInterpreterIntermediary().getId(), hearing.getId()));
            hearingInterpreterIntermediaryRepository.saveAndFlush(hearingInterpreterIntermediary);
        }

    }
}
