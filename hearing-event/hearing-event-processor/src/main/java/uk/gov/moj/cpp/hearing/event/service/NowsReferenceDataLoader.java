package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

@SuppressWarnings({"squid:S3358", "squid:S1612"})
public class NowsReferenceDataLoader {
    private static final String GET_ALL_NOWS_REQUEST_ID = "referencedata.get-all-now-metadata";
    private static final String GET_ALL_RESULT_DEFINITIONS_REQUEST_ID = "referencedata.get-all-result-definitions";


    private static final String ON_QUERY_PARAMETER = "on";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public AllNows loadAllNowsReference(JsonEnvelope context, LocalDate localDate) {

        final String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(context, GET_ALL_NOWS_REQUEST_ID)
                .apply(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate).build());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        final AllNows allNows = jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllNows.class);


        allNows.getNows().forEach(now -> now.setReferenceDate(localDate));

        return allNows;
    }

    private List<String> trim(List<String> strs) {
        return strs == null ? null : strs.stream().map(s -> s == null ? null : s.trim()).collect(Collectors.toList());
    }

    private void trimUserGroups(final ResultDefinition resultDefinition) {
        resultDefinition.setUserGroups(trim(resultDefinition.getUserGroups()));
        resultDefinition.getPrompts().forEach(p -> p.setUserGroups(trim(p.getUserGroups())));
    }

    public AllResultDefinitions loadAllResultDefinitions(JsonEnvelope context, LocalDate localDate) {

        final String strLocalDate = localDate.toString();
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(context, GET_ALL_RESULT_DEFINITIONS_REQUEST_ID)
                .apply(createObjectBuilder().add(ON_QUERY_PARAMETER, strLocalDate).build());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);
        final AllResultDefinitions allResultDefinitions = jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), AllResultDefinitions.class);
        //correct incoming data
        allResultDefinitions.getResultDefinitions().forEach(rd -> trimUserGroups(rd));
        return allResultDefinitions;
    }


}
