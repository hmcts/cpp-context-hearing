package uk.gov.moj.cpp.hearing.event.relist;


import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingPrompt;
import uk.gov.moj.cpp.hearing.event.relist.metadata.NextHearingResultDefinition;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RelistReferenceDataService {
    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING = "referencedata.get-result-definition-next-hearing";
    private static final String REFERENCE = "reference";
    private static final String RESULT_ID = "id";
    private static final String PROMPT_ID = "id";
    private static final String RESULT_DEFINITIONS = "resultDefinitions";
    private static final String PROMPTS = "prompts";
    private static final String ON = "on";
    private static final Logger LOGGER = LoggerFactory.getLogger(RelistReferenceDataService.class);
    private static final String REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN = "referencedata.get-result-definition-withdrawn";
    @Inject
    @ServiceComponent(EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    public List<UUID> getWithdrawnResultDefinitionUuids(final JsonEnvelope envelope, final LocalDate on) {

        return getWithdrawnResultDefinitions(envelope, on).getJsonArray(RESULT_DEFINITIONS).stream()
                .map(jsonValue -> ((JsonObject) jsonValue).getString(("id")))
                .map(UUID::fromString)
                .collect(Collectors.toList());
    }

    public Map<UUID, NextHearingResultDefinition> getNextHearingResultDefinitions(final JsonEnvelope envelope, final LocalDate on) {
        final Map<UUID, NextHearingResultDefinition> nextHearingResultDefinitions = new HashMap<>();

        getNextHearingDefinitions(envelope, on).getJsonArray(RESULT_DEFINITIONS)
                .forEach(jsonValue -> {
                    final JsonObject definitionJsonObject = (JsonObject) jsonValue;
                    final UUID resultDefinitionId = UUID.fromString(definitionJsonObject.getString(RESULT_ID));
                    definitionJsonObject.getJsonArray(PROMPTS).forEach(jsonValue1 -> {
                        final JsonObject promptJsonObject = (JsonObject) jsonValue1;
                        LOGGER.debug("prompt {}", promptJsonObject);
                        if (promptJsonObject.containsKey(REFERENCE)) {
                            final String promptReference = promptJsonObject.getString(REFERENCE);
                            final UUID promptId = UUID.fromString(promptJsonObject.getString(PROMPT_ID));
                            nextHearingResultDefinitions.compute(resultDefinitionId, (k, v) -> v == null ? new NextHearingResultDefinition(resultDefinitionId, new NextHearingPrompt(promptId, promptReference))
                                    : addPrompt(promptReference, v, promptId));
                        }
                    });

                });
        return nextHearingResultDefinitions;
    }

    private NextHearingResultDefinition addPrompt(final String promptReference, final NextHearingResultDefinition nextHearingResultDefinition, final UUID promptId) {
        nextHearingResultDefinition.addNextHearingPrompt(new NextHearingPrompt(promptId, promptReference));
        return nextHearingResultDefinition;
    }

    private JsonObject getWithdrawnResultDefinitions(final JsonEnvelope envelope, final LocalDate on) {
        return requester.request(enveloper.withMetadataFrom(envelope, REFERENCE_DATA_GET_RESULT_DEFINITION_WITHDRAWN)
                .apply(createObjectBuilder().add(ON, on.toString()).build())).payloadAsJsonObject();
    }

    private JsonObject getNextHearingDefinitions(final JsonEnvelope envelope, final LocalDate on) {
        return requester.request(enveloper.withMetadataFrom(envelope, REFERENCE_DATA_GET_RESULT_DEFINITION_NEXT_HEARING)
                .apply(createObjectBuilder().add(ON, on.toString()).build())).payloadAsJsonObject();
    }
}
