package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.UUID.randomUUID;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.QUERY_VIEW;
import static uk.gov.justice.services.messaging.Envelope.metadataBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import javax.inject.Inject;

public class ReferenceDataService {
    private static final String GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";

    @Inject
    @ServiceComponent(QUERY_VIEW)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    public CrackedIneffectiveVacatedTrialTypes getCrackedIneffectiveVacatedTrialTypes() {

        final JsonEnvelope requestEnvelope = envelopeFrom(
                metadataBuilder().
                        withId(randomUUID()).
                        withName(GET_ALL_CRACKED_INEFFECTIVE_TRIAL_TYPES),
                createObjectBuilder());

        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        return jsonObjectToObjectConverter.convert(jsonResultEnvelope.payloadAsJsonObject(), CrackedIneffectiveVacatedTrialTypes.class);
    }
}
