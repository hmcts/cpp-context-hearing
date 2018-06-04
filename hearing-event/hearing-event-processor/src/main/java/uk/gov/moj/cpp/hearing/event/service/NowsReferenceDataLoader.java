package uk.gov.moj.cpp.hearing.event.service;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.DefaultJsonEnvelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.Metadata;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import javax.inject.Inject;
import javax.json.JsonObject;
import java.time.LocalDate;
import java.util.UUID;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonObjectMetadata.metadataWithDefaults;

public class NowsReferenceDataLoader {

    private static final String GET_ALL_NOWS_REQUEST_ID = "referencedata.query.get-all-nows-definitions";
    private static final String GET_RESULT_DEFINITION_REQUEST_ID = "referencedata.query.get-result-definitions.v2";

    private static final String AS_OF_DATE_QUERY_PARAMETER = "asOfDate";

    private static final String RESULT_DEFINITION_ID_QUERY_PARAMETER = "resultDefinitionId";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public AllNows loadAllNowsReference(LocalDate localDate) {

        final JsonObject jsonObject = createObjectBuilder().build();
        final Metadata metadata = metadataWithDefaults().build();
        JsonEnvelope envelopeIn = new DefaultJsonEnvelope(metadata, jsonObject);

        String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelopeIn, GET_ALL_NOWS_REQUEST_ID)
                .apply(createObjectBuilder().add(AS_OF_DATE_QUERY_PARAMETER, strLocalDate).build());

        JsonEnvelope jsonResultEnvelope =  requester.request(requestEnvelope);

        return  jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllNows.class);

    }

    public ResultDefinition getResultDefinitionById(UUID resultDefinitionId) {

           final JsonObject jsonObject = createObjectBuilder().build();
        final Metadata metadata = metadataWithDefaults().build();
        JsonEnvelope envelopeIn = new DefaultJsonEnvelope(metadata, jsonObject);

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelopeIn, GET_RESULT_DEFINITION_REQUEST_ID)
                .apply(createObjectBuilder().add(RESULT_DEFINITION_ID_QUERY_PARAMETER, resultDefinitionId.toString()).build());

        JsonEnvelope jsonResultEnvelope =  requester.request(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), ResultDefinition.class);

    }


}
