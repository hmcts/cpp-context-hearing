package uk.gov.moj.cpp.hearing.event.listener;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.AttendeeDeleted;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingDate;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

@ServiceComponent(EVENT_LISTENER)
public class AttendeeDeletedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttendeeDeletedEventListener.class);

    private final HearingRepository hearingRepository;
    private final AttendeeHearingDateRespository attendeeHearingDateRespository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public AttendeeDeletedEventListener(final HearingRepository hearingRepository,
            final AttendeeHearingDateRespository attendeeHearingDateRespository, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingRepository = hearingRepository;
        this.attendeeHearingDateRespository = attendeeHearingDateRespository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.events.attendee-deleted")
    public void onAttendeeDeleted(final JsonEnvelope envelope) {
        final AttendeeDeleted attendeeDeleted = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), AttendeeDeleted.class);
        final Hearing hearing = hearingRepository.findById(attendeeDeleted.getHearingId());
        if (null != hearing) {
            final List<HearingDate> hearingDays = hearing.getHearingDays().stream()
                    .filter(hearingDay -> hearingDay.getDate().compareTo(attendeeDeleted.getHearingDate()) >= 0)
                    .collect(toList());
            hearingDays.forEach(hearingDay -> {
                final int result = this.attendeeHearingDateRespository.delete(attendeeDeleted.getHearingId(), attendeeDeleted.getAttendeeId(), hearingDay.getId().getId());
                if (result == 0) {
                    LOGGER.info("Attendee {} already deleted for hearing {} from day >= {}", attendeeDeleted.getAttendeeId(), attendeeDeleted.getHearingId(), attendeeDeleted.getHearingDate());
                }
            });
        }
    }
}
