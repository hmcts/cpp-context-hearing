package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AlcoholLevelMethod;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.alcohollevel.AllAlcoholLevelMethods;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonObject;

public class AlcoholLevelMethodsReferenceDataLoader {

    private static final String GET_ALL_ALCOHOL_LEVEL_METHODS_REQUEST_ID = "referencedata.query.alcohol-level-methods";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public List<AlcoholLevelMethod> getAllAlcoholLevelMethods(final JsonEnvelope context) {

        final Envelope<JsonObject> requestEnvelope = envelop(createObjectBuilder()
                .build())
                .withName(GET_ALL_ALCOHOL_LEVEL_METHODS_REQUEST_ID)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, AllAlcoholLevelMethods.class).payload().getAlcoholLevelMethods();
    }
}
