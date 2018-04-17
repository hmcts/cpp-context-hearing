package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.NewDefenceCounselAdded;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Ahearing;
import uk.gov.moj.cpp.hearing.persist.entity.ex.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ex.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ex.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.AhearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class DefenceCounselAddedEventListener {


    @Inject
    private AhearingRepository ahearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.newdefence-counsel-added")
    public void defenseCounselAdded(final JsonEnvelope event) {

        NewDefenceCounselAdded newDefenceCounselAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), NewDefenceCounselAdded.class);

        Ahearing hearing = ahearingRepository.findBy(newDefenceCounselAdded.getHearingId());

        DefenceAdvocate defenceAdvocate = hearing.getAttendees().stream()
                .filter(a -> a instanceof DefenceAdvocate && a.getId().getId().equals(newDefenceCounselAdded.getAttendeeId()))
                .map(DefenceAdvocate.class::cast)
                .findFirst()
                .orElseGet(() -> {
                    DefenceAdvocate defenceCounselor = DefenceAdvocate.builder()
                            .withId(new HearingSnapshotKey(newDefenceCounselAdded.getAttendeeId(), hearing.getId()))

                            .build();
                    hearing.getAttendees().add(defenceCounselor);
                    return defenceCounselor;
                });

        defenceAdvocate
                .setStatus(newDefenceCounselAdded.getStatus())
                .setPersonId(newDefenceCounselAdded.getPersonId())
                .setTitle(newDefenceCounselAdded.getTitle())
                .setFirstName(newDefenceCounselAdded.getFirstName())
                .setLastName(newDefenceCounselAdded.getLastName());

        newDefenceCounselAdded.getDefendantIds().forEach(
                defendantId -> {
                    Defendant defendant = hearing.getDefendants().stream()
                            .filter(d -> d.getId().getId().equals(defendantId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                            String.format("hearing %s defence counsel %s added for unkown defendant %s ",
                                                    hearing.getId(),
                                                    newDefenceCounselAdded.getAttendeeId(),
                                                    defendantId
                                            )
                                    )
                            );
                    defenceAdvocate.getDefendants().add(defendant);
                    defendant.getDefenceAdvocates().add(defenceAdvocate);
                }
        );
        ahearingRepository.save(hearing);
    }
}
