package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialTypes;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S00112", "squid:S1181"})
public class CrackedIneffectiveVacatedTrialTypesReverseLookup {

    public static final String CRACKED_INEFFECTIVE_VACATED_TRIAL_TYPES = "referencedata.query.cracked-ineffective-vacated-trial-types";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private Enveloper enveloper;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @SuppressWarnings("squid:CallToDeprecatedMethod")
    public CrackedIneffectiveVacatedTrialTypes getCrackedIneffectiveVacatedTrialType(JsonEnvelope context) {

        final Envelope<JsonObject> envelope = Enveloper.envelop(createObjectBuilder().build())
                .withName(CRACKED_INEFFECTIVE_VACATED_TRIAL_TYPES)
                .withMetadataFrom(context);
        final JsonEnvelope requestEnvelope = envelopeFrom(envelope.metadata(), envelope.payload());
        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(requestEnvelope);

        final JsonObject json = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(json, CrackedIneffectiveVacatedTrialTypes.class);

    }


}
