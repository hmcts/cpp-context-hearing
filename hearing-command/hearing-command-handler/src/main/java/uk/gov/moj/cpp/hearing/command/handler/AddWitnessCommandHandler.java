package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import java.util.List;
import java.util.UUID;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddWitnessCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddWitnessCommandHandler.class.getName());

    @Handles("hearing.command.add-update-witness")
    public void addWitness(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.add-witness event received {}", envelope.toObfuscatedDebugString());
        }

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID witnessId = fromString(payload.getString("id"));
        final UUID hearingId = fromString(payload.getString("hearingId"));
        final String type = payload.getString("type");
        final String classification = payload.getString("classification");
        final String title = payload.getString("title", null);
        final String firstName = payload.getString("firstName");
        final String lastName = payload.getString("lastName");
        final List<DefendantId> defendantIdList =
                payload.getJsonArray("defendants").getValuesAs(JsonObject.class).stream()
                        .map(this::extractDefendantId)
                        .collect(toList());

        aggregate(NewModelHearingAggregate.class, hearingId, envelope, a -> a.addWitness(hearingId, witnessId, type, classification, title, firstName, lastName, defendantIdList));
    }

    @Handles("hearing.defence-witness-added")
    public void defenceWitnessAdded(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.defence-witness-added event received {}", envelope.toObfuscatedDebugString());
        }

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID witnessId = fromString(payload.getString("witnessId"));
        final UUID hearingId = fromString(payload.getString("hearingId"));
        final String type = payload.getString("type");
        final String classification = payload.getString("classification");
        final String title = payload.getString("title", null);
        final String firstName = payload.getString("firstName");
        final String lastName = payload.getString("lastName");
        final UUID defendantId = fromString(payload.getString("defendantId"));
        aggregate(DefendantAggregate.class, defendantId, envelope,
                (defendantAggregate) -> defendantAggregate.addWitness(witnessId, hearingId, defendantId, type, classification, title, firstName, lastName));
    }

    private DefendantId extractDefendantId(final JsonObject jsonObject) {
        return DefendantId.builder().withDefendantId(fromString(jsonObject.getString("defendantId"))).build();
    }
}
