package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.CourtApplicationHearingDeleted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Objects;
import java.util.UUID;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class HearingDeletedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingDeletedEventListener.class);

    private static final String HEARING_EVENT_HEARING_DELETED = "hearing.events.hearing-deleted";
    private static final String HEARING_EVENT_HEARING_DELETED_BDF = "hearing.events.hearing-deleted-bdf";

    @Inject
    private HearingRepository hearingRepository;

    @Handles(HEARING_EVENT_HEARING_DELETED)
    public void hearingDeleted(final JsonEnvelope event) {

        final UUID hearingId = UUID.fromString(event.payloadAsJsonObject().getString("hearingId"));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received event '{}' hearingId: {}", HEARING_EVENT_HEARING_DELETED, hearingId);
        }

        final Hearing hearing = hearingRepository.findBy(hearingId);

        if (hearing != null) {
            hearingRepository.remove(hearing);
        }
    }

    @Handles(HEARING_EVENT_HEARING_DELETED_BDF)
    public void hearingDeletedBdf(final JsonEnvelope event) {
        final UUID hearingId = UUID.fromString(event.payloadAsJsonObject().getString("hearingId"));

        LOGGER.info("Received event '{}' hearingId: {}", HEARING_EVENT_HEARING_DELETED_BDF, hearingId);

        final Hearing hearing = hearingRepository.findBy(hearingId);

        if (hearing != null) {
            hearingRepository.remove(hearing);
        }
    }

    @Handles("hearing.event.court-application-hearing-deleted")
    public void processCourtApplicationDeleted(final Envelope<CourtApplicationHearingDeleted> event) {
        final UUID hearingId = event.payload().getHearingId();
        final Hearing hearingToBeDeleted = hearingRepository.findBy(hearingId);

        if (Objects.nonNull(hearingToBeDeleted)) {
            hearingRepository.remove(hearingToBeDeleted);
        }
    }
}
