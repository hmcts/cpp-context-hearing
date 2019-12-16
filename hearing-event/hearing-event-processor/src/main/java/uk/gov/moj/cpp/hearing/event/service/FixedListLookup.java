package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.hearing.courts.referencedata.FixedListResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

public class FixedListLookup {


    public static final String GET_ALL_FIXED_LIST = "referencedata.get-all-fixed-list";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public FixedListResult getAllFixedLists(final JsonEnvelope context) {

        final Envelope<JsonObject> envelope = Enveloper.envelop(createObjectBuilder().build())
                .withName(GET_ALL_FIXED_LIST)
                .withMetadataFrom(context);
        final JsonEnvelope requestEnvelope = envelopeFrom(envelope.metadata(), envelope.payload());
        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);
        final JsonObject responseJsonObject = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(responseJsonObject, FixedListResult.class);
    }
}
