package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.nows.events.NowsMaterialStatusUpdated;
import uk.gov.moj.cpp.hearing.nows.events.NowsRequested;

import javax.inject.Inject;
import javax.json.JsonObject;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class GenerateNowsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GenerateNowsCommandHandler.class.getName());

    @Inject
    public GenerateNowsCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                      final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }


    @Handles("hearing.command.generate-nows")
    public void genarateNows(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.generate-nows event received {}", envelope.payloadAsJsonObject());

        final JsonObject payload = envelope.payloadAsJsonObject();
        final NowsRequested nowsRequested = jsonObjectToObjectConverter.convert(payload, NowsRequested.class);
        aggregate(NewModelHearingAggregate.class, fromString(nowsRequested.getHearing().getId()), envelope, a -> a.generateNows(nowsRequested));
    }

    @Handles("hearing.command.generate-nows.v2")
    public void genarateNowsV2(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.info("hearing.command.generate-nows.v2 event received {} (process N/A) ", envelope.payloadAsJsonObject());
    }



    @Handles("hearing.command.update-nows-material-status")
    public void nowsGenerated(final JsonEnvelope envelope) throws EventStreamException {
        LOGGER.debug("hearing.command.update-nows-material-status {}", envelope.payloadAsJsonObject());

        final JsonObject payload = envelope.payloadAsJsonObject();
        final NowsMaterialStatusUpdated nowsRequested = jsonObjectToObjectConverter.convert(payload, NowsMaterialStatusUpdated.class);
        aggregate(NewModelHearingAggregate.class, nowsRequested.getHearingId(), envelope, a -> a.nowsMaterialStatusUpdated(nowsRequested));
    }
}
