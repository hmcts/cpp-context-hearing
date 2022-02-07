package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingDefenceCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDefenceCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingDefenceCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00112", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class DefenceCounselEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingCaseNoteSavedEventListener.class.getName());
    public static final String HEARING_NOT_FOUND = "Hearing not found";

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingDefenceCounselRepository hearingDefenceCounselRepository;

    @Inject
    private HearingDefenceCounselJPAMapper hearingDefenceCounselJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Transactional
    @Handles("hearing.defence-counsel-added")
    public void defenceCounselAdded(final JsonEnvelope envelope) {

        final DefenceCounselAdded defenceCounselAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DefenceCounselAdded.class);
        final Hearing hearing = hearingRepository.findBy(defenceCounselAdded.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Defence Counsel Added for hearingId {} ", defenceCounselAdded.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingDefenceCounsel hearingDefenceCounsel = hearingDefenceCounselJPAMapper.toJPA(hearing, defenceCounselAdded.getDefenceCounsel());
            hearingDefenceCounsel.setId(new HearingSnapshotKey(defenceCounselAdded.getDefenceCounsel().getId(), hearing.getId()));
            hearingDefenceCounselRepository.saveAndFlush(hearingDefenceCounsel);
        }

    }

    @Transactional
    @Handles("hearing.defence-counsel-removed")
    public void defenceCounselRemoved(final JsonEnvelope event) {
        final DefenceCounselRemoved defenceCounselRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DefenceCounselRemoved.class);
        final Hearing hearing = hearingRepository.findBy(defenceCounselRemoved.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Defence Counsel Removed for hearingId {} ", defenceCounselRemoved.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final Optional<HearingDefenceCounsel> hearingDefenceCounsel =
                    hearing.getDefenceCounsels().stream().filter(pc -> pc.getId().getId().equals(defenceCounselRemoved.getId()))
                            .findFirst();

            if (hearingDefenceCounsel.isPresent()) {
                final HearingDefenceCounsel softDeletePC = hearingDefenceCounsel.get();
                softDeletePC.setDeleted(true);
                hearingDefenceCounselRepository.saveAndFlush(softDeletePC);
            }
        }
    }

    @Transactional
    @Handles("hearing.defence-counsel-updated")
    public void defenceCounselUpdated(final JsonEnvelope envelope) {

        final DefenceCounselUpdated defenceCounselUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DefenceCounselUpdated.class);
        final Hearing hearing = hearingRepository.findBy(defenceCounselUpdated.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Defence counsel updated for hearingId {} ", defenceCounselUpdated.getHearingId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingDefenceCounsel hearingDefenceCounsel = hearingDefenceCounselJPAMapper.toJPA(hearing, defenceCounselUpdated.getDefenceCounsel());
            hearingDefenceCounsel.setId(new HearingSnapshotKey(defenceCounselUpdated.getDefenceCounsel().getId(), hearing.getId()));
            hearingDefenceCounselRepository.saveAndFlush(hearingDefenceCounsel);

        }

    }
}
