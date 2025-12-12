package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.enveloper.Enveloper.envelop;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.AllBailStatuses;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.bailstatus.BailStatus;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonObject;

public class BailStatusReferenceDataLoader {

    static final String GET_ALL_BAILSTATUSES_REQUEST_ID = "referencedata.query.bail-statuses";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    List<BailStatus> getAllBailStatuses(final JsonEnvelope context) {

        final Envelope<JsonObject> requestEnvelope = envelop(createObjectBuilder()
                .build())
                .withName(GET_ALL_BAILSTATUSES_REQUEST_ID)
                .withMetadataFrom(context);

        return requester.request(requestEnvelope, AllBailStatuses.class).payload().getBailStatuses();
    }
}
