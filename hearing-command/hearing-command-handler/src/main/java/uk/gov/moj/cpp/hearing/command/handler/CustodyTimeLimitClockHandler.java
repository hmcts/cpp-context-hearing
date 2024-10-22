package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.requester.Requester;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.justice.services.messaging.MetadataBuilder;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_HANDLER)
public class CustodyTimeLimitClockHandler extends AbstractCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustodyTimeLimitClockHandler.class);

    private static final String HEARING_ID = "hearingId";

    @Inject
    private Requester requester;


    @Handles("hearing.command.stop-custody-time-limit-clock")
    public void stopCustodyTimeLimitClock(final JsonEnvelope envelope) throws EventStreamException {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("'hearing.command.extend-custody-time-limit' received with payload {}", envelope.toObfuscatedDebugString());
        }

        final UUID hearingId = UUID.fromString(envelope.payloadAsJsonObject().getString(HEARING_ID));

        final JsonObject payload = createObjectBuilder().add("category", "F").add("on", LocalDate.now().toString()).build();

        final MetadataBuilder metadata = metadataFrom(envelope.metadata()).withName("referencedata.query-result-definitions-with-category");

        final JsonEnvelope jsonEnvelope = requester.request(envelopeFrom(metadata, payload));

        final JsonArray resultsArray = jsonEnvelope.payloadAsJsonObject().getJsonArray("resultDefinitions");

        final List<UUID> resultIdList = resultsArray.stream()
                .map(jsonValue -> {
                    final JsonObject jsonObject = (JsonObject) jsonValue;
                    return UUID.fromString(jsonObject.getString("id"));
                })
                .collect(toList());

        LOGGER.info("referencedata.query-result-definitions-with-category size {} ", resultIdList.size());

        aggregate(HearingAggregate.class, hearingId, envelope, a -> a.stopCustodyTimeLimitClock(resultIdList));

    }
}
