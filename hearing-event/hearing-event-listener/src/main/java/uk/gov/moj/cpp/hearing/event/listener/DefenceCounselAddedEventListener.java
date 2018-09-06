package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@SuppressWarnings({"squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class DefenceCounselAddedEventListener {

    private final HearingRepository hearingRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public DefenceCounselAddedEventListener(final HearingRepository hearingRepository, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingRepository = hearingRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.newdefence-counsel-added")
    public void defenseCounselAdded(final JsonEnvelope event) {

        final DefenceCounselUpsert defenceCounselUpsert = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), DefenceCounselUpsert.class);

        final Hearing hearing = hearingRepository.findBy(defenceCounselUpsert.getHearingId());

        if (hearing != null) {

            /*final DefenceAdvocate defenceAdvocate = hearing.getAttendees().stream()
                    .filter(a -> a instanceof DefenceAdvocate && a.getId().getId().equals(defenceCounselUpsert.getAttendeeId()))
                    .map(DefenceAdvocate.class::cast)
                    .findFirst()
                    .orElseGet(() -> {
                        final DefenceAdvocate defenceCounselor = DefenceAdvocate.builder()
                                .withId(new HearingSnapshotKey(defenceCounselUpsert.getAttendeeId(), hearing.getId()))
                                .build();
                        hearing.getAttendees().add(defenceCounselor);
                        return defenceCounselor;
                    });*/

            /*defenceAdvocate
                    .setStatus(defenceCounselUpsert.getStatus())
                    .setPersonId(defenceCounselUpsert.getPersonId())
                    .setTitle(defenceCounselUpsert.getTitle())
                    .setFirstName(defenceCounselUpsert.getFirstName())
                    .setLastName(defenceCounselUpsert.getLastName());*/

//            final List<Defendant> defendantsToRemove = new ArrayList<>();
//            for (Defendant defendant : defenceAdvocate.getDefendants()) {
//
//                if (!defenceCounselUpsert.getDefendantIds().contains(defendant.getId())) {
//                    defendantsToRemove.add(defendant);
//                }
//            }
//            defenceAdvocate.getDefendants().removeAll(defendantsToRemove);

/*  TODO review defence counsel missin from db model
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
*/
            this.hearingRepository.saveAndFlush(hearing);
        }
    }
}
