package uk.gov.moj.cpp.hearing.event.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;

import uk.gov.justice.core.courts.HearingType;
import uk.gov.justice.hearing.courts.referencedata.HearingTypes;
import uk.gov.justice.hearing.courts.referencedata.HearingTypesResult;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;

import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

@SuppressWarnings({"squid:S00112", "squid:S1181"})
public class HearingTypeReverseLookup {

    public static final String GET_HEARING_TYPES_ID = "referencedata.query.hearing-types";

    @Inject
    @ServiceComponent(Component.EVENT_PROCESSOR)
    private Requester requester;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private HearingTypesResult hearingTypesResult(JsonEnvelope context) {

        final Envelope<JsonObject> requestEnvelope = Enveloper.envelop(createObjectBuilder().build())
                .withName(GET_HEARING_TYPES_ID)
                .withMetadataFrom(context);
        final JsonEnvelope jsonResultEnvelope = requester.requestAsAdmin(envelopeFrom(requestEnvelope.metadata(), requestEnvelope.payload()));

        final JsonObject organisationalUnitJson = jsonResultEnvelope.payloadAsJsonObject();
        return jsonObjectToObjectConverter.convert(organisationalUnitJson, HearingTypesResult.class);
    }

    public HearingType getHearingTypeByName(JsonEnvelope context, String typeName) {

        typeName = normalize(typeName);
        try {
            final HearingTypesResult hearingTypeResult = hearingTypesResult(context);
            for (final HearingTypes hearingTypes : hearingTypeResult.getHearingTypes()) {
                if (typeName.equals(normalize(hearingTypes.getHearingDescription()))) {
                    return HearingType.hearingType()
                            .withDescription(hearingTypes.getHearingDescription())
                            .withId(UUID.fromString(hearingTypes.getId()))
                            .build();
                }
            }
        } catch (Throwable tw) {
            throw new RuntimeException(String.format("failed to find hearing type with description %s", typeName), tw);
        }
        return null;
    }

    private String normalize(final String typeName) {
        return typeName.toLowerCase();
    }

}
