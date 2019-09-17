package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.Objects.nonNull;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonObjects.getUUID;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
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

@ServiceComponent(COMMAND_HANDLER)
public class SetTrialTypeCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(SetTrialTypeCommandHandler.class.getName());

    @Inject
    private Requester requester;

    @Handles("hearing.command.set-trial-type")
    public void setTrialType(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.set-trial-type event received {}", envelope.toObfuscatedDebugString());
        }

        final TrialType trialType = convertToObject(envelope, TrialType.class);

        if(nonNull(trialType.getTrialTypeId())) {

            final JsonObject crackedIneffectiveReason = getCrackedIneffectiveTrial(envelope);

            final String code = crackedIneffectiveReason.getString("code");
            final String description = crackedIneffectiveReason.getString("description");
            final String type = crackedIneffectiveReason.getString("type");
            final Optional<UUID> id = getUUID(crackedIneffectiveReason, "id");

            aggregate(HearingAggregate.class, trialType.getHearingId(), envelope, a -> a.setTrialType(new HearingTrialType(trialType.getHearingId(), id.get(), code, type, description)));
        }

        if(nonNull(trialType.getIsEffectiveTrial()) && trialType.getIsEffectiveTrial()) {
            aggregate(HearingAggregate.class, trialType.getHearingId(), envelope, a -> a.setTrialType(new HearingEffectiveTrial(trialType.getHearingId(), trialType.getIsEffectiveTrial())));
        }
    }

    private JsonObject getCrackedIneffectiveTrial(final JsonEnvelope command) {
        final JsonObject payload = command.payloadAsJsonObject();

        final JsonEnvelope query = enveloper.withMetadataFrom(command, "hearing.get-cracked-ineffective-reason")
                .apply(createObjectBuilder()
                        .add("trialTypeId", payload.getString("trialTypeId"))
                        .build()
                );

        return requester.request(query).payloadAsJsonObject();
    }
}
