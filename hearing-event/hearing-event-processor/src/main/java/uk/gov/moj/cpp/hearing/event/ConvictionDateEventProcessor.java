package uk.gov.moj.cpp.hearing.event;

import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

@ServiceComponent(EVENT_PROCESSOR)
public class ConvictionDateEventProcessor {

    private Enveloper enveloper;
    private Sender sender;
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;

    @Inject
    public ConvictionDateEventProcessor(final Enveloper enveloper, final Sender sender,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter,
            final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
        this.objectToJsonObjectConverter = objectToJsonObjectConverter;
    }

    @Handles("hearing.conviction-date-added")
    public void publishOffenceConvictionDateChangedPublicEvent(final JsonEnvelope event) {

        ConvictionDateAdded convictionDateAdded = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                ConvictionDateAdded.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.offence-conviction-date-changed")
                .apply(this.objectToJsonObjectConverter.convert(convictionDateAdded)));
    }

    @Handles("hearing.conviction-date-removed")
    public void publishOffenceConvictionDateRemovedPublicEvent(final JsonEnvelope event) {

        ConvictionDateRemoved convictionDateRemoved = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.offence-conviction-date-removed")
                .apply(this.objectToJsonObjectConverter.convert(convictionDateRemoved)));
    }
}
