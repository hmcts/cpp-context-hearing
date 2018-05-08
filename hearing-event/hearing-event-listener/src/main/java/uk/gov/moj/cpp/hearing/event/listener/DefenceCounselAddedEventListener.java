package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.persist.entity.ha.DefenceAdvocate;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Defendant;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

@ServiceComponent(EVENT_LISTENER)
public class DefenceCounselAddedEventListener {

    @Inject
    private HearingRepository hearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.newdefence-counsel-added")
    public void defenseCounselAdded(final JsonEnvelope event) {

        DefenceCounselUpsert defenceCounselUpsert = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DefenceCounselUpsert.class);

        Hearing hearing = hearingRepository.findBy(defenceCounselUpsert.getHearingId());

        if (hearing == null){
            throw new RuntimeException("hearing id is not found.");
        }

        DefenceAdvocate defenceAdvocate = hearing.getAttendees().stream()
                .filter(a -> a instanceof DefenceAdvocate && a.getId().getId().equals(defenceCounselUpsert.getAttendeeId()))
                .map(DefenceAdvocate.class::cast)
                .findFirst()
                .orElseGet(() -> {
                    DefenceAdvocate defenceCounselor = DefenceAdvocate.builder()
                            .withId(new HearingSnapshotKey(defenceCounselUpsert.getAttendeeId(), hearing.getId()))

                            .build();
                    hearing.getAttendees().add(defenceCounselor);
                    return defenceCounselor;
                });

        defenceAdvocate
                .setStatus(defenceCounselUpsert.getStatus())
                .setPersonId(defenceCounselUpsert.getPersonId())
                .setTitle(defenceCounselUpsert.getTitle())
                .setFirstName(defenceCounselUpsert.getFirstName())
                .setLastName(defenceCounselUpsert.getLastName());

        List<Defendant> defendantsToRemove = new ArrayList<>();
        for (Defendant defendant: defenceAdvocate.getDefendants()){

            if (!defenceCounselUpsert.getDefendantIds().contains(defendant.getId().getId())){
                defendantsToRemove.add(defendant);
                defendant.getDefenceAdvocates().removeIf(da -> da.getId().getId().equals(defenceAdvocate.getId().getId()));
            }
        }
        defenceAdvocate.getDefendants().removeAll(defendantsToRemove);

        defenceCounselUpsert.getDefendantIds().forEach(
                defendantId -> {
                    Defendant defendant = hearing.getDefendants().stream()
                            .filter(d -> d.getId().getId().equals(defendantId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                            String.format("hearing %s defence counsel %s added for unknown defendant %s ",
                                                    hearing.getId(),
                                                    defenceCounselUpsert.getAttendeeId(),
                                                    defendantId
                                            )
                                    )
                            );
                    if (defenceAdvocate.getDefendants().stream()
                            .noneMatch(d -> d.getId().getId().equals(defendantId))) {
                        defenceAdvocate.getDefendants().add(defendant);
                    }

                    if (defendant.getDefenceAdvocates().stream()
                            .noneMatch(d -> d.getId().getId().equals(defenceAdvocate.getId().getId()))) {
                        defendant.getDefenceAdvocates().add(defenceAdvocate);
                    }
                }
        );
        hearingRepository.save(hearing);
    }
}
