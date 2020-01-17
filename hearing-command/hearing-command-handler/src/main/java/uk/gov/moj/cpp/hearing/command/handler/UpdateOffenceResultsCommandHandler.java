package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.OffenceResult;
import uk.gov.moj.cpp.hearing.domain.aggregate.DefendantAggregate;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class UpdateOffenceResultsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(UpdateOffenceResultsCommandHandler.class.getName());
    public static final String DEFENDANT_ID = "defendantId";
    public static final String CASE_ID = "caseId";
    public static final String OFFENCE_IDS = "offenceIds";
    public static final String RESULTED_OFFENCES = "resultedOffences";

    @Handles("hearing.command.handler.update-offence-results")
    public void updateOffenceResults(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.handler.update-offence-results event received {}", envelope.toObfuscatedDebugString());
        }

        final JsonObject payload = envelope.payloadAsJsonObject();
        final UUID defendantId = fromString(payload.getString(DEFENDANT_ID));
        final UUID caseId = fromString(payload.getString(CASE_ID));
        final List<UUID> offenceIds = payload.getJsonArray(OFFENCE_IDS).stream().map(toUUID()).collect(toList());
        final Map<UUID, OffenceResult> updatedResults = payload.getJsonArray(RESULTED_OFFENCES).stream().map(toPair()).collect(toMap(Pair::getKey, Pair::getValue));

        aggregate(DefendantAggregate.class,
                defendantId,
                envelope,
                defendantAggregate ->
                        defendantAggregate.updateOffenceResults(defendantId, caseId, offenceIds, updatedResults)
        );
    }

    private Function<JsonValue, Pair<UUID, OffenceResult>> toPair() {
        return (JsonValue jsonValue) -> {
            if (jsonValue.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                final JsonObject keyPairJson = (JsonObject) jsonValue;
                return Pair.of(fromString(keyPairJson.getString("offenceId")), OffenceResult.valueOf(keyPairJson.getString("offenceResult")));
            } else {
                throw new IllegalArgumentException("Unable to convert this to a key pair");
            }
        };
    }

    private Function<JsonValue, UUID> toUUID() {
        return (JsonValue jsonValue) -> {
            if (jsonValue.getValueType().equals(JsonValue.ValueType.STRING)) {
                return fromString(((JsonString) jsonValue).getString());
            } else {
                throw new IllegalArgumentException("Cannot convert this object type to UUID");
            }
        };
    }

}
