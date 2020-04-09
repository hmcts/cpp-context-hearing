package uk.gov.justice.ccr.notepad.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.common.converter.LocalDates.to;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.time.LocalDate;

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

    public JsonEnvelope getAllDefinitions(final JsonEnvelope envelope, final LocalDate orderedDate) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-result-definitions")
                .apply(createObjectBuilder()
                        .add("on", to(orderedDate))
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllDefinitionWordSynonyms(final JsonEnvelope envelope, final LocalDate orderedDate) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-result-word-synonyms")
                .apply(createObjectBuilder()
                        .add("on", to(orderedDate))
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllFixedLists(final JsonEnvelope envelope, final LocalDate orderedDate) {
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-fixed-list")
                .apply(createObjectBuilder()
                        .add("on", to(orderedDate))
                        .build());
        return requester.request(requestEnvelope);
    }

    public JsonEnvelope getAllCourtCentre(final JsonEnvelope envelope) {
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.query.courtrooms")
                .apply(createObjectBuilder().build());
        return requester.requestAsAdmin(requestEnvelope);
    }

    public JsonEnvelope getHearingTypes(final JsonEnvelope envelope) {
        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.query.hearing-types")
                .apply(createObjectBuilder().build());
        return requester.requestAsAdmin(requestEnvelope);
    }

    public JsonEnvelope getAllResultPromptWordSynonyms(final JsonEnvelope envelope, final LocalDate orderedDate) {

        final JsonEnvelope requestEnvelope = enveloper.withMetadataFrom(envelope, "referencedata.get-all-result-prompt-word-synonyms")
                .apply(createObjectBuilder()
                        .add("on", to(orderedDate))
                        .build());
        return requester.request(requestEnvelope);
    }

}
