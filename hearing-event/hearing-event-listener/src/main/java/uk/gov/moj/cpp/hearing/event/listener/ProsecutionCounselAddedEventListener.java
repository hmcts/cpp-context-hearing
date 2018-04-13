package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.NewProsecutionCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ex.ProsecutionAdvocate;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class ProsecutionCounselAddedEventListener {

    @Inject
    private AhearingRepository ahearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.newprosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {

        NewProsecutionCounselAdded prosecutionCounselAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NewProsecutionCounselAdded.class);

        Ahearing hearing = ahearingRepository.findBy(prosecutionCounselAdded.getHearingId());

        ProsecutionAdvocate prosecutionAdvocate = hearing.getAttendees().stream()
                .filter(a -> a instanceof ProsecutionAdvocate)
                .map(ProsecutionAdvocate.class::cast)
                .filter(a -> a.getId().getId().equals(prosecutionCounselAdded.getAttendeeId()))
                .findFirst()
                .orElseGet(() -> {

                    ProsecutionAdvocate prosecutionCounselor = ProsecutionAdvocate.builder()
                            .withId(new HearingSnapshotKey(prosecutionCounselAdded.getAttendeeId(), prosecutionCounselAdded.getHearingId()))
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

        ahearingRepository.save(hearing);
    }
}
