package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;

import javax.inject.Inject;
import java.time.LocalDate;

import static javax.json.Json.createObjectBuilder;

public class NowsReferenceDataLoader {
    private static final String GET_ALL_NOWS_REQUEST_ID = "referencedata.get-all-now-metadata";
    private static final String GET_ALL_RESULT_DEFINITIONS_REQUEST_ID = "referencedata.get-all-result-definitions";

    private static final String ON_QUERY_PARAMETER = "on";

    private JsonEnvelope context;

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public void setContext(JsonEnvelope context) {
        this.context = context;
    }

    public AllNows loadAllNowsReference(LocalDate localDate) {

        final String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(context, GET_ALL_NOWS_REQUEST_ID)
                .apply(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate).build());

        final JsonEnvelope jsonResultEnvelope = requester.request(requestEnvelope);

        final AllNows allNows = jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllNows.class);

        allNows.getNows().forEach(now -> now.setReferenceDate(localDate));

        return allNows;
    }

    public AllResultDefinitions loadAllResultDefinitions(LocalDate localDate) {

        final String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(context, GET_ALL_RESULT_DEFINITIONS_REQUEST_ID)
                .apply(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate).build());

        JsonEnvelope jsonResultEnvelope = requester.request(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllResultDefinitions.class);
    }


}
