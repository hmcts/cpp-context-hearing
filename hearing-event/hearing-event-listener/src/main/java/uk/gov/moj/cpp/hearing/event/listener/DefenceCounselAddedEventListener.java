package uk.gov.moj.cpp.hearing.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(NewModelPleaUpdateEventListener.class);

    @Inject
    private AhearingRepository ahearingRepository;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Transactional
    @Handles("hearing.newdefence-counsel-added")
    public void defenseCounselAdded(final JsonEnvelope event) {
        LOGGER.info("update defence counselor: " + event.toString() );

        DefenceCounselUpsert defenceCounselUpsert = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DefenceCounselUpsert.class);

        Ahearing hearing = ahearingRepository.findBy(defenceCounselUpsert.getHearingId());

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

        defenceCounselUpsert.getDefendantIds().forEach(
                defendantId -> {
                    Defendant defendant = hearing.getDefendants().stream()
                            .filter(d -> d.getId().getId().equals(defendantId))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException(
                                            String.format("hearing %s defence counsel %s added for unkown defendant %s ",
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
        ahearingRepository.save(hearing);
    }
}
