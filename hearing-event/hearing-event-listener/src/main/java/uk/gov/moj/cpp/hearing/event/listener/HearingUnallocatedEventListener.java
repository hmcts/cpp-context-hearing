package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class HearingUnallocatedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(HearingUnallocatedEventListener.class);

    private static final String HEARING_EVENT_HEARING_UNALLOCATED = "hearing.events.hearing-unallocated";

    @Inject
    private HearingRepository hearingRepository;

    @Handles(HEARING_EVENT_HEARING_UNALLOCATED)
    public void hearingUnallocated(final JsonEnvelope event) {

        final UUID hearingId = UUID.fromString(event.payloadAsJsonObject().getString("hearingId"));

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Received event '{}' hearingId: {}", HEARING_EVENT_HEARING_UNALLOCATED, hearingId);
        }

        final Hearing hearing = hearingRepository.findBy(hearingId);

        if (hearing != null) {
            hearingRepository.remove(hearing);
        }
    }
}
