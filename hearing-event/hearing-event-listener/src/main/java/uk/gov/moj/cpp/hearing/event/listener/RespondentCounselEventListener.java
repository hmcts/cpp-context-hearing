package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselAdded;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselRemoved;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselUpdated;
import uk.gov.moj.cpp.hearing.mapping.HearingRespondentCounselJPAMapper;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingRespondentCounsel;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingRespondentCounselRepository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Optional;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S00112", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class RespondentCounselEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RespondentCounselEventListener.class);
    public static final String HEARING_NOT_FOUND = "Hearing not found";

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private HearingRespondentCounselRepository hearingRespondentCounselRepository;

    @Inject
    private HearingRespondentCounselJPAMapper hearingRespondentCounselJPAMapper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;


    @Transactional
    @Handles("hearing.respondent-counsel-added")
    public void respondentCounselAdded(final JsonEnvelope envelope) {

        final RespondentCounselAdded respondentCounselAdded = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), RespondentCounselAdded.class);
        final Hearing hearing = hearingRepository.findBy(respondentCounselAdded.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.respondent-counsel-added for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingRespondentCounsel hearingRespondentCounsel = hearingRespondentCounselJPAMapper.toJPA(hearing, respondentCounselAdded.getRespondentCounsel());
            hearingRespondentCounsel.setId(new HearingSnapshotKey(respondentCounselAdded.getRespondentCounsel().getId(), hearing.getId()));
            hearingRespondentCounselRepository.saveAndFlush(hearingRespondentCounsel);
        }

    }

    @Transactional
    @Handles("hearing.respondent-counsel-removed")
    public void respondentCounselRemoved(final JsonEnvelope event) {
        final RespondentCounselRemoved respondentCounselRemoved = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), RespondentCounselRemoved.class);
        final Hearing hearing = hearingRepository.findBy(respondentCounselRemoved.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.respondent-counsel-removed for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final Optional<HearingRespondentCounsel> hearingRespondentCounsel =
                    hearing.getRespondentCounsels().stream().filter(pc -> pc.getId().getId().equals(respondentCounselRemoved.getId()))
                            .findFirst();

            if (hearingRespondentCounsel.isPresent()) {
                final HearingRespondentCounsel softDeleteRespondentCounsel = hearingRespondentCounsel.get();
                softDeleteRespondentCounsel.setDeleted(true);
                hearingRespondentCounselRepository.saveAndFlush(softDeleteRespondentCounsel);
            }
        }
    }

    @Transactional
    @Handles("hearing.respondent-counsel-updated")
    public void respondentCounselUpdated(final JsonEnvelope envelope) {

        final RespondentCounselUpdated respondentCounselUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), RespondentCounselUpdated.class);
        final Hearing hearing = hearingRepository.findBy(respondentCounselUpdated.getHearingId());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.respondent-counsel-updated for hearingId {} ", hearing.getId());
        }
        if (hearing == null) {
            LOGGER.error(HEARING_NOT_FOUND);
        } else {
            final HearingRespondentCounsel hearingRespondentCounsel = hearingRespondentCounselJPAMapper.toJPA(hearing, respondentCounselUpdated.getRespondentCounsel());
            hearingRespondentCounsel.setId(new HearingSnapshotKey(respondentCounselUpdated.getRespondentCounsel().getId(), hearing.getId()));
            hearingRespondentCounselRepository.saveAndFlush(hearingRespondentCounsel);

        }

    }
}
