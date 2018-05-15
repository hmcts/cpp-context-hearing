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
import uk.gov.moj.cpp.hearing.command.prosecutionCounsel.AddProsecutionCounselCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;

import javax.inject.Inject;
import javax.json.JsonObject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class AddProsecutionCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddProsecutionCounselCommandHandler.class.getName());

    @Inject
    public AddProsecutionCounselCommandHandler(EventSource eventSource, Enveloper enveloper, AggregateService aggregateService, JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) throws EventStreamException {

        LOGGER.debug("hearing.add-prosecution-counsel event received {}", envelope.payloadAsJsonObject());

        final JsonObject payload = envelope.payloadAsJsonObject();
        AddProsecutionCounselCommand addProsecutionCounselCommand = jsonObjectToObjectConverter.convert(payload, AddProsecutionCounselCommand.class);

        aggregate(NewModelHearingAggregate.class, addProsecutionCounselCommand.getHearingId(), envelope,
                (hearingAggregate) ->  hearingAggregate.addProsecutionCounsel(addProsecutionCounselCommand));
    }
}
