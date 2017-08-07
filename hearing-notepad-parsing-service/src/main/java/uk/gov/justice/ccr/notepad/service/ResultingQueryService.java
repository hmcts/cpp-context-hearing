package uk.gov.justice.ccr.notepad.service;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.Json;

/**
 * Used for querying Results from Reference Data Context.
 */
public class ResultingQueryService {

    @ServiceComponent(Component.QUERY_API)
    @Inject
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    public JsonEnvelope getAllDefinitions(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-definitions")
                .apply(Json.createObjectBuilder()
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllDefinitionKeywordSynonyms(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-definition-keyword-synonyms")
                .apply(Json.createObjectBuilder()
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllPrompts(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-prompts")
                .apply(Json.createObjectBuilder()
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllPromptFixedLists(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-prompt-fixedlists")
                .apply(Json.createObjectBuilder()
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllPromptKeywordSynonyms(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-prompt-keyword-synonyms")
                .apply(Json.createObjectBuilder()
                        .build());
        return requester.request(requestEnvelope);
    }
}
