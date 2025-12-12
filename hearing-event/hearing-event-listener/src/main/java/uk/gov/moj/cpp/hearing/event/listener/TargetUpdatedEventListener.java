package uk.gov.moj.cpp.hearing.event.listener;


import static uk.gov.justice.services.core.annotation.Component.EVENT_LISTENER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.moj.cpp.hearing.domain.event.ApplicationFinalisedOnTargetUpdated;
import uk.gov.moj.cpp.hearing.persist.entity.ha.HearingSnapshotKey;
import uk.gov.moj.cpp.hearing.persist.entity.ha.Target;
import uk.gov.moj.cpp.hearing.repository.TargetRepository;

import javax.inject.Inject;

@ServiceComponent(EVENT_LISTENER)
public class TargetUpdatedEventListener {

    @Inject
    private TargetRepository targetRepository;

    @Handles("hearing.events.application-finalised-on-target-updated")
    public void handleApplicationFinalisedOnTargetUpdated(final Envelope<ApplicationFinalisedOnTargetUpdated> event) {
        final ApplicationFinalisedOnTargetUpdated payload = event.payload();
        final Target target = targetRepository.findBy(new HearingSnapshotKey(payload.getId(), payload.getHearingId()));
        target.setApplicationFinalised(true);
        targetRepository.save(target);
    }
}
