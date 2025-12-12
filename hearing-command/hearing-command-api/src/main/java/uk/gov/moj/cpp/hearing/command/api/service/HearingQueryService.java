package uk.gov.moj.cpp.hearing.command.api.service;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.messaging.Envelope;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

public class HearingQueryService {

    private static final String FIELD_HEARING_ID = "hearingId";
    @Inject
    @ServiceComponent(COMMAND_API)
    private Requester requester;

    /**
     * this method will throw ForbiddenRequestException if user doesn't have access to hearing
     */
    public void validateIfUserHasAccessToHearing(final JsonEnvelope envelope) {
        final Optional<UUID> hearingId = getUUID(envelope.payloadAsJsonObject(), FIELD_HEARING_ID);
        if (hearingId.isPresent()) {
            final MetadataBuilder metadataWithActionName = Envelope.metadataFrom(envelope.metadata()).withName("hearing.get.hearing");
            final JsonEnvelope requestEnvelope = envelopeFrom(metadataWithActionName,
                    createObjectBuilder().add(FIELD_HEARING_ID, hearingId.get().toString()).build());
            requester.request(requestEnvelope, JsonObject.class);
        }
    }
}