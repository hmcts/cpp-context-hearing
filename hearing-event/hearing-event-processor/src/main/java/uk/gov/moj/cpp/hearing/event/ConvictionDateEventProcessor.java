package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;

import javax.inject.Inject;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(EVENT_PROCESSOR)
public class ConvictionDateEventProcessor {

    private static final String CASE_ID = "caseId";
    private static final String OFFENCE_ID = "offenceId";
    private static final String CONVICTION_DATE = "convictionDate";
    private static final String COURT_APPLICATION_ID = "courtApplicationId";

    private static final Logger LOGGER = LoggerFactory.getLogger(ConvictionDateEventProcessor.class);
    private Enveloper enveloper;
    private Sender sender;
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

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
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-added event received {}", event.toObfuscatedDebugString());
        }

        ConvictionDateAdded convictionDateAdded = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(),
                ConvictionDateAdded.class);
        final JsonObjectBuilder builder = createObjectBuilder();
        if(convictionDateAdded.getCourtApplicationId() == null){
            builder.add(CASE_ID, convictionDateAdded.getCaseId().toString())
                    .add(OFFENCE_ID, convictionDateAdded.getOffenceId().toString())
                    .add(CONVICTION_DATE, convictionDateAdded.getConvictionDate().toString());
        }else{
            builder.add(COURT_APPLICATION_ID, convictionDateAdded.getCourtApplicationId().toString())
                    .add(CONVICTION_DATE, convictionDateAdded.getConvictionDate().toString());
            if(convictionDateAdded.getOffenceId() != null){
               builder.add(OFFENCE_ID, convictionDateAdded.getOffenceId().toString());
            }
        }
        this.sender.send(Enveloper.envelop(builder.build())
                .withName("public.hearing.offence-conviction-date-changed")
                .withMetadataFrom(event));
    }

    @Handles("hearing.conviction-date-removed")
    public void publishOffenceConvictionDateRemovedPublicEvent(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.conviction-date-removed event received {}", event.toObfuscatedDebugString());
        }

        ConvictionDateRemoved convictionDateRemoved = this.jsonObjectToObjectConverter
                .convert(event.payloadAsJsonObject(), ConvictionDateRemoved.class);

        final JsonObjectBuilder builder = createObjectBuilder();
        if(convictionDateRemoved.getCourtApplicationId() == null){
            builder.add(CASE_ID, convictionDateRemoved.getCaseId().toString())
                    .add(OFFENCE_ID, convictionDateRemoved.getOffenceId().toString());
        }else{
            builder.add(COURT_APPLICATION_ID, convictionDateRemoved.getCourtApplicationId().toString());
            if(convictionDateRemoved.getOffenceId() != null) {
                builder.add(OFFENCE_ID, convictionDateRemoved.getOffenceId().toString());
            }
        }
        this.sender.send(Enveloper.envelop(builder.build())
                .withName("public.hearing.offence-conviction-date-removed")
                .withMetadataFrom(event));
    }
}
