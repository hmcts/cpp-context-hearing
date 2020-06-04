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
public class HearingDetailChangeEventProcessor {
    private static final String PUBLIC_PROGRESSION_EVENT_HEARING_DETAIL_CHANGED = "public.hearing-detail-changed";
    private static final String PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE = "hearing.change-hearing-detail";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Handles(PUBLIC_PROGRESSION_EVENT_HEARING_DETAIL_CHANGED)
    public void publishHearingDetailChangedPrivateEvent(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, PRIVATE_HEARING_COMMAND_HEARING_DETAIL_CHANGE).apply(event.payloadAsJsonObject()));
    }
}
