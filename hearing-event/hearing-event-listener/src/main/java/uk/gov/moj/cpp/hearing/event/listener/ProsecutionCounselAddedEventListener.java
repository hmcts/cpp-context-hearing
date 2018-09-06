package uk.gov.moj.cpp.hearing.event.listener;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ProsecutionCounselUpsert;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Hearing;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;

@SuppressWarnings({"squid:S00112", "squid:CommentedOutCodeLine"})
@ServiceComponent(EVENT_LISTENER)
public class ProsecutionCounselAddedEventListener {

    private final HearingRepository hearingRepository;
    private final JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    public ProsecutionCounselAddedEventListener(final HearingRepository hearingRepository,
                                                final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        this.hearingRepository = hearingRepository;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Transactional
    @Handles("hearing.newprosecution-counsel-added")
    public void prosecutionCounselAdded(final JsonEnvelope event) {

        final ProsecutionCounselUpsert prosecutionCounselAdded = jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), ProsecutionCounselUpsert.class);

        final Hearing hearing = hearingRepository.findBy(prosecutionCounselAdded.getHearingId());

        if (hearing != null) {

            /*
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
*/
            this.hearingRepository.saveAndFlush(hearing);
        }
    }
}
