package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;

import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S2629"})
@ServiceComponent(COMMAND_HANDLER)
public class SetTrialTypeCommandHandler extends AbstractCommandHandler {

    public static final String TRIAL_TYPE_ID = "trialTypeId";

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SetTrialTypeCommandHandler.class.getName());

    @Inject
    private Requester requester;

    @Handles("hearing.command.set-trial-type")
    public void handleTrialType(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.set-trial-type event received {}", envelope.toObfuscatedDebugString());

        final TrialType trialType = convertToObject(envelope, TrialType.class);

        if (nonNull(trialType.getVacatedTrialReasonId())) {

            final JsonObject vacateReason = constructVacateTrial(envelope);

            aggregate(HearingAggregate.class, trialType.getHearingId(), envelope, a -> a.setTrialType(trialType.getHearingId(), getUUID(vacateReason, "id").get(), vacateReason.getString("code"), vacateReason.getString("type"), vacateReason.getString("description")));

        }

        if (nonNull(trialType.getTrialTypeId())) {

            final JsonObject crackedIneffectiveReason = getCrackedIneffectiveTrial(envelope);

            final String code = crackedIneffectiveReason.getString("code");
            final String description = crackedIneffectiveReason.getString("description");
            final String type = crackedIneffectiveReason.getString("type");
            final Optional<UUID> id = getUUID(crackedIneffectiveReason, "id");

            aggregate(HearingAggregate.class, trialType.getHearingId(), envelope, a -> a.setTrialType(new HearingTrialType(trialType.getHearingId(), id.get(), code, type, description)));
        }

        if (nonNull(trialType.getIsEffectiveTrial()) && trialType.getIsEffectiveTrial()) {
            aggregate(HearingAggregate.class, trialType.getHearingId(), envelope, a -> a.setTrialType(new HearingEffectiveTrial(trialType.getHearingId(), trialType.getIsEffectiveTrial())));
        }
    }

    private JsonObject getCrackedIneffectiveTrial(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();


        final JsonEnvelope query = enveloper.withMetadataFrom(command, "hearing.get-cracked-ineffective-reason")
                .apply(createObjectBuilder()
                        .add(TRIAL_TYPE_ID, payload.getString(TRIAL_TYPE_ID))
                        .build()
                );

        return requester.request(query).payloadAsJsonObject();
    }

    private JsonObject constructVacateTrial(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();

        final MetadataBuilder metadata = metadataFrom(command.metadata()).withName("hearing.get-cracked-ineffective-reason");
        final JsonEnvelope query = envelopeFrom(metadata, createObjectBuilder()
                .add(TRIAL_TYPE_ID, payload.getString("vacatedTrialReasonId"))
                .build());

        return requester.request(query).payloadAsJsonObject();
    }

}
