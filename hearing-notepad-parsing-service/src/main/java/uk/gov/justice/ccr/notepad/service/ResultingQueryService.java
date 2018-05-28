package uk.gov.justice.ccr.notepad.service;

import static javax.json.Json.createObjectBuilder;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

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

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-result-definitions")
                .apply(createObjectBuilder().build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllDefinitionKeywordSynonyms(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.result.get-all-definition-keyword-synonyms")
                .apply(createObjectBuilder().build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllPromptFixedLists(final JsonEnvelope envelope) {
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-fixed-list")
                .apply(createObjectBuilder().build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllPromptKeywordSynonyms(final JsonEnvelope envelope) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-result-prompt-word-synonyms")
                .apply(createObjectBuilder().build());
        return requester.request(requestEnvelope);
    }
}
