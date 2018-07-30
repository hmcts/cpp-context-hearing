package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;
import javax.transaction.Transactional;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(EVENT_PROCESSOR)
public class AttendeeDeletedEventProcessor {

    private final Enveloper enveloper;
    private final Sender sender;

    @Inject
    public AttendeeDeletedEventProcessor(final Enveloper enveloper, final Sender sender) {
        this.enveloper = enveloper;
        this.sender = sender;
    }
    @Transactional
    @Handles("hearing.events.attendee-deleted")
    public void onAttendeeDeleted(final JsonEnvelope envelope) {
        this.sender.send(this.enveloper.withMetadataFrom(envelope, "public.hearing.events.attendee-deleted")
                .apply(createObjectBuilder()
                        .add("attendeeId", envelope.payloadAsJsonObject().getJsonString("attendeeId"))
                        .build()
                 ));
    }
}