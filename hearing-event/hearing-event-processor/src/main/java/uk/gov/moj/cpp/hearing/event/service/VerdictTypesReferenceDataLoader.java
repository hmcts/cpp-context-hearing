package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.core.courts.VerdictType;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.verdicttype.AllVerdictTypes;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonObject;

public class VerdictTypesReferenceDataLoader {

    private static final String GET_ALL_VERDICT_TYPES_REQUEST_ID = "referencedata.query.verdict-types";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    public List<VerdictType> getAllVerdictTypes(final JsonEnvelope context) {

        final Envelope<JsonObject> requestEnvelope = envelop(createObjectBuilder()
                .build())
                .withName(GET_ALL_VERDICT_TYPES_REQUEST_ID)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, AllVerdictTypes.class).payload().getVerdictTypes();
    }
}
