package uk.gov.moj.cpp.hearing.event.listener;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.DefendantsInYouthCourtUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourDefendantsKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingYouthCourtDefendants;
import uk.gov.moj.cpp.hearing.repository.HearingYouthCourtDefendantsRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

@ServiceComponent(EVENT_LISTENER)
public class YouthCourtDefendantsUpdatedEventListener {

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private HearingYouthCourtDefendantsRepository hearingYouthCourtDefendantsRepository;

    @Transactional
    @Handles("hearing.event.defendants-in-youthcourt-updated")
    public void updateYouthCourtDefendants(final JsonEnvelope envelope) {
        final DefendantsInYouthCourtUpdated defendantsInYouthCourtUpdated = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), DefendantsInYouthCourtUpdated.class);
        final List<UUID> latestDefendantIdsOfYouth = defendantsInYouthCourtUpdated.getYouthCourtDefendantIds();
        final List<HearingYouthCourtDefendants>  hearingYouthCourtDefendants = hearingYouthCourtDefendantsRepository.findAllByHearingId(defendantsInYouthCourtUpdated.getHearingId());
        final List<UUID> existingDefendantIds = hearingYouthCourtDefendants.stream().map(d -> d.getId().getDefendantId()).collect(Collectors.toList());


        final List<HearingYouthCourtDefendants>  defendantsNoMoreInYouthCourt = hearingYouthCourtDefendants.stream().filter(d -> !defendantsInYouthCourtUpdated.getYouthCourtDefendantIds().contains(d.getId().getDefendantId())).collect(Collectors.toList());
        defendantsNoMoreInYouthCourt.stream().forEach(d -> hearingYouthCourtDefendantsRepository.remove(d));

        //Remove already present in database
        latestDefendantIdsOfYouth.removeAll(existingDefendantIds);

        //Add the new ones.
        latestDefendantIdsOfYouth.stream().forEach( d -> {
            final HearingYouthCourDefendantsKey hearingYouthCourDefendantsKey = new HearingYouthCourDefendantsKey(d, defendantsInYouthCourtUpdated.getHearingId());
            hearingYouthCourtDefendantsRepository.save(new HearingYouthCourtDefendants(hearingYouthCourDefendantsKey));
        });
    }
}