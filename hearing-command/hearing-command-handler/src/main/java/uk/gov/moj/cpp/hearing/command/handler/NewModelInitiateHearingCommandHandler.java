package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.Plea;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

import javax.inject.Inject;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelInitiateHearingCommandHandler {

    @Inject
    private EventSource eventSource;

    @Inject
    private Enveloper enveloper;

    @Inject
    private AggregateService aggregateService;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.initiate")
    public void initiate(final JsonEnvelope command) throws EventStreamException {

        InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(command.payloadAsJsonObject(), InitiateHearingCommand.class);

        initiateCases(command, initiateHearingCommand);

        for (Defendant defendant : forAllDefendants(initiateHearingCommand).collect(Collectors.toList())) {
            for (Offence offence : defendant.getOffences()) {

                OffenceAggregate offenceAggregate = initiateOffences(command, defendant, offence);

                applyOldPleasToNewHearing(offence, offenceAggregate);
            }
        }

        initiateHearing(command, initiateHearingCommand);
    }

    private void initiateHearing(JsonEnvelope command, InitiateHearingCommand initiateHearingCommand) throws EventStreamException {
        applyToHearingAggregate(initiateHearingCommand.getHearing().getId(), a -> a.initiate(initiateHearingCommand), command);
    }

    private void applyOldPleasToNewHearing(Offence offence, OffenceAggregate offenceAggregate) {
        ofNullable(offenceAggregate.getPlea())
                .ifPresent(plea -> {
                    offence.setPlea(
                            Plea.builder()
                                    .withId(plea.getId())
                                    .withOriginalHearingId(plea.getOriginHearingId())
                                    .withValue(plea.getValue())
                                    .withPleaDate(plea.getPleaDate())
                                    .build()

                    );
                });
    }

    private OffenceAggregate initiateOffences(JsonEnvelope command, Defendant defendant, Offence offence) throws EventStreamException {
        return applyToOffenceAggregate(offence.getId(), a -> a.initiateHearing(defendant.getId(), offence), command);
    }

    private void initiateCases(JsonEnvelope command, InitiateHearingCommand initiateHearingCommand) throws EventStreamException {
        for (UUID caseId : forAllOffences(initiateHearingCommand).map(Offence::getCaseId).collect(Collectors.toList())) {
            applyToCaseAggregate(caseId, a -> a.initiateHearing(caseId, initiateHearingCommand), command);
        }
    }

    private static Stream<Defendant> forAllDefendants(InitiateHearingCommand initiateHearingCommand) {
        return initiateHearingCommand.getHearing().getDefendants().stream();
    }

    private static Stream<Offence> forAllOffences(InitiateHearingCommand initiateHearingCommand) {
        return initiateHearingCommand.getHearing().getDefendants().stream().flatMap(d -> d.getOffences().stream());
    }

    private void applyToHearingAggregate(final UUID streamId, final Function<NewModelHearingAggregate, Stream<Object>> function,
                                         final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final NewModelHearingAggregate aggregate = this.aggregateService.get(eventStream, NewModelHearingAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    private void applyToCaseAggregate(final UUID streamId, final Function<CaseAggregate, Stream<Object>> function,
                                      final JsonEnvelope envelope) throws EventStreamException {

        final EventStream eventStream = this.eventSource.getStreamById(flip(streamId));
        final CaseAggregate aggregate = this.aggregateService.get(eventStream, CaseAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
    }

    private OffenceAggregate applyToOffenceAggregate(final UUID streamId, final Function<OffenceAggregate, Stream<Object>> function,
                                                     final JsonEnvelope envelope) throws EventStreamException {
        final EventStream eventStream = this.eventSource.getStreamById(streamId);
        final OffenceAggregate aggregate = this.aggregateService.get(eventStream, OffenceAggregate.class);
        final Stream<Object> events = function.apply(aggregate);
        eventStream.append(events.map(this.enveloper.withMetadataFrom(envelope)));
        return aggregate;
    }

    private UUID flip(UUID id) {
        //TODO - GPE-3032 CLEANUP - get rid of this method.
        return new UUID(id.getLeastSignificantBits(), id.getMostSignificantBits());
    }
}
