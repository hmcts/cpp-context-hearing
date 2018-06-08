package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.AttendeeHearingDate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AttendeeHearingDateRespository;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.UUID;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@SuppressWarnings("squid:S00112")
@ServiceComponent(EVENT_LISTENER)
public class ProsecutionCounselAddedEventListener {

    private final HearingRepository hearingRepository;
    private final AttendeeHearingDateRespository attendeeHearingDateRespository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public ProsecutionCounselAddedEventListener(final HearingRepository hearingRepository,
            final AttendeeHearingDateRespository attendeeHearingDateRespository, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingRepository = hearingRepository;
        this.attendeeHearingDateRespository = attendeeHearingDateRespository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.newprosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {

        final ProsecutionCounselUpsert prosecutionCounselAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ProsecutionCounselUpsert.class);

        final Hearing hearing = hearingRepository.findBy(prosecutionCounselAdded.getHearingId());

        if (hearing == null){
            throw new IllegalArgumentException("Hearing not found by id: " + prosecutionCounselAdded.getHearingId() + " for add prosecution counsel");
        }

        final ProsecutionAdvocate prosecutionAdvocate = hearing.getAttendees().stream()
                .filter(a -> a instanceof ProsecutionAdvocate)
                .map(ProsecutionAdvocate.class::cast)
                .filter(a -> a.getId().getId().equals(prosecutionCounselAdded.getAttendeeId()))
                .findFirst()
                .orElseGet(() -> {

                    final ProsecutionAdvocate prosecutionCounselor = ProsecutionAdvocate.builder()
                            .withId(new HearingSnapshotKey(prosecutionCounselAdded.getAttendeeId(), hearing.getId()))
                            .build();

                    if (hearing.getAttendees() == null) {
                        hearing.setAttendees(new ArrayList<>());
                    }
                    hearing.getAttendees().add(prosecutionCounselor);
                    return prosecutionCounselor;
                });

        prosecutionAdvocate
                .setStatus(prosecutionCounselAdded.getStatus())
                .setPersonId(prosecutionCounselAdded.getPersonId())
                .setTitle(prosecutionCounselAdded.getTitle())
                .setFirstName(prosecutionCounselAdded.getFirstName())
                .setLastName(prosecutionCounselAdded.getLastName());

        this.hearingRepository.saveAndFlush(hearing);

        hearing.getHearingDays().forEach(hearingDay ->
            this.attendeeHearingDateRespository.saveAndFlush(
                    AttendeeHearingDate.builder()
                        .withId(new HearingSnapshotKey(UUID.randomUUID(), hearing.getId()))
                        .withAttendeeId(prosecutionAdvocate.getId().getId())
                        .withHearingDateId(hearingDay.getId().getId())
                        .build()
                   )
        );
    }
}
