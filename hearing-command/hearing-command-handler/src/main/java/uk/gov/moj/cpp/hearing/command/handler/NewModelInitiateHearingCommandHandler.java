package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.UUID.fromString;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffenceCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingOffencePleaCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.json.JsonObject;
import java.util.UUID;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelInitiateHearingCommandHandler extends AbstractCommandHandler {

    @Inject
    public NewModelInitiateHearingCommandHandler(final EventSource eventSource, final Enveloper enveloper,
            final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingCommand command = convertToObject(envelop, InitiateHearingCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearing().getId(), envelop, a -> a.initiate(command));
    }

    @Handles("hearing.command.initiate-hearing-offence")
    public void initiateHearingOffence(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingOffenceCommand command = convertToObject(envelop, InitiateHearingOffenceCommand.class);
        aggregate(OffenceAggregate.class, command.getOffenceId(), envelop, a -> a.initiateHearingOffence(command));
    }

    @Handles("hearing.command.initiate-hearing-offence-plea")
    public void initiateHearingOffencePlea(final JsonEnvelope envelop) throws EventStreamException {
        final InitiateHearingOffencePleaCommand command = convertToObject(envelop, InitiateHearingOffencePleaCommand.class);
        aggregate(NewModelHearingAggregate.class, command.getHearingId(), envelop, a -> a.initiateHearingOffencePlea(command));
    }


    @Handles("hearing.command.add-witness")
    public void addWitness(final JsonEnvelope envelop) throws EventStreamException {
        final JsonObject payload = envelop.payloadAsJsonObject();
        final UUID witnessId = fromString(payload.getString("id"));
        final UUID caseId = fromString(payload.getString("caseId"));
        final UUID hearingId = fromString(payload.getString("hearingId"));
        final String type = payload.getString("type");
        final String classification = payload.getString("classification");
        final UUID personId = fromString(payload.getString("personId"));
        final String title = payload.getString("title");
        final String firstName = payload.getString("firstName");
        final String lastName = payload.getString("lastName");

        aggregate(NewModelHearingAggregate.class, hearingId, envelop, a ->a.addWitness(hearingId, caseId, witnessId, type, classification, personId, title, firstName, lastName));
    }
}
