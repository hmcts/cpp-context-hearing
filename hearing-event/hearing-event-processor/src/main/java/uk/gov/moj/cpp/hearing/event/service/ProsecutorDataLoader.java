package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.hearing.courts.referencedata.Prosecutor;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class ProsecutorDataLoader {

    public static final String GET_PROSECUTOR_BY_ID = "referencedata.query.prosecutor";
    public static final String ID_PATH_PARAM = "id";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public Prosecutor getProsecutorById(final JsonEnvelope context, final UUID id) {
        final JsonObject payload = createObjectBuilder()
                .add(ID_PATH_PARAM, id.toString())
                .build();
        final Envelope<JsonObject> requestEnvelope = envelop(payload)
                .withName(GET_PROSECUTOR_BY_ID)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, Prosecutor.class).payload();
    }
}
