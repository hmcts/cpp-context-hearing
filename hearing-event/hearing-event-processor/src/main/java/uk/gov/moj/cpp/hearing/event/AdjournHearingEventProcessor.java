package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class AdjournHearingEventProcessor {
    private static final String PUBLIC_HEARING_EVENT_ADJOURNED = "public.hearing.adjourned";
    private static final String PRIVATE_HEARING_EVENT_ADJOURNED = "hearing.event.hearing-adjourned";


    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles(PRIVATE_HEARING_EVENT_ADJOURNED)
    public void publishPublicHearingAdjournedEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PUBLIC_HEARING_EVENT_ADJOURNED).apply(event.payloadAsJsonObject()));
    }

}
