package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String CASE_ID = "caseId";
    private static final String OFFENCE_ID = "offenceId";
    private static final String CONVICTION_DATE = "convictionDate";

    private Enveloper enveloper;
    private Sender sender;
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvictionDateEventProcessor.class);

    @Inject
    public ConvictionDateEventProcessor(final Enveloper enveloper, final Sender sender,
            final JsonObjectToObjectConverter jsonObjectToObjectConverter,
            final ObjectToJsonObjectConverter objectToJsonObjectConverter) {
        this.enveloper = enveloper;
        this.sender = sender;
        this.jsonObjectToObjectConverter = jsonObjectToObjectConverter;
    }

    @Handles("hearing.conviction-date-added")
    public void publishOffenceConvictionDateChangedPublicEvent(final JsonEnvelope event) {
        LOGGER.debug("hearing.conviction-date-added event received {}", event.payloadAsJsonObject());

        ConvictionDateAdded convictionDateAdded = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                ConvictionDateAdded.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.offence-conviction-date-changed")
                .apply(createObjectBuilder().add(CASE_ID, convictionDateAdded.getCaseId().toString())
                        .add(OFFENCE_ID, convictionDateAdded.getOffenceId().toString())
                        .add(CONVICTION_DATE, convictionDateAdded.getConvictionDate().toString()).build()));
    }

    @Handles("hearing.conviction-date-removed")
    public void publishOffenceConvictionDateRemovedPublicEvent(final JsonEnvelope event) {
        LOGGER.debug("hearing.conviction-date-removed event received {}", event.payloadAsJsonObject());

        ConvictionDateRemoved convictionDateRemoved = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.offence-conviction-date-removed")
                .apply(createObjectBuilder().add(CASE_ID, convictionDateRemoved.getCaseId().toString())
                        .add(OFFENCE_ID, convictionDateRemoved.getOffenceId().toString()).build()));
    }
}
