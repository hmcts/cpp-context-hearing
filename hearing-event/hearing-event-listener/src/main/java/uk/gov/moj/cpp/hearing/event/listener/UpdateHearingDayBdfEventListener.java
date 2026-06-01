package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.HearingUpdatedHearingDayBdf;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.Set;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_LISTENER)
public class UpdateHearingDayBdfEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateHearingDayBdfEventListener.class);

    @Inject
    private HearingRepository hearingRepository;

    @Handles("hearing.events.hearing-updated-hearing-day-bdf")
    public void hearingUpdatedHearingDayBdf(final Envelope<HearingUpdatedHearingDayBdf> event) {
        final HearingUpdatedHearingDayBdf payload = event.payload();

        LOGGER.info("Received event 'hearing.events.hearing-updated-hearing-day-bdf' hearingId: {}", payload.getHearingId());

        final Hearing hearing = hearingRepository.findBy(payload.getHearingId());
        if (hearing == null) {
            LOGGER.warn("Hearing not found for hearingId: {}", payload.getHearingId());
            return;
        }

        final Set<uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDay> hearingDays = hearing.getHearingDays();
        if (hearingDays == null || hearingDays.isEmpty()) {
            LOGGER.warn("No hearing days found for hearingId: {}", payload.getHearingId());
            return;
        }

        final HearingDay hearingDay = payload.getHearingDay();

        hearingDays.forEach(day -> {
            day.setSittingDay(hearingDay.getSittingDay());
            day.setDate(hearingDay.getSittingDay().toLocalDate());
            day.setDateTime(hearingDay.getSittingDay());
        });

        hearingRepository.save(hearing);
    }
}
