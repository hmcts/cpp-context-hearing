package uk.gov.moj.cpp.hearing.command.handler;

import uk.gov.justice.progression.events.SendingSheetCompleted;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.EventStream;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.RecordMagsCourtHearingCommand;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.CaseAggregate;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingTransformer;
import uk.gov.moj.cpp.hearing.domain.aggregate.NewModelHearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.MagsCourtHearingRecorded;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class MagistratesCourtInitiateHearingCommandHandler extends AbstractCommandHandler {

    @Inject
    public MagistratesCourtInitiateHearingCommandHandler(final EventSource eventSource, final Enveloper enveloper,
                                                         final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter);
    }

    @Handles("hearing.record-sending-sheet-complete")
    public void recordSendingSheetComplete(final JsonEnvelope command) throws EventStreamException {

        final SendingSheetCompleted sendingSheetCompleted = jsonObjectToObjectConverter
                .convert(command.payloadAsJsonObject(), SendingSheetCompleted.class);

        aggregate(CaseAggregate.class, sendingSheetCompleted.getHearing().getCaseId(), command,
                aggregate -> aggregate.recordSendingSheetComplete(sendingSheetCompleted));
    }

    @Handles("hearing.record-mags-court-hearing")
    public void recordMagsCourtHearing(final JsonEnvelope command) throws EventStreamException {


        final List<MagsCourtHearingRecorded> hearings2Initiate = new HearingTransformer()
                .transform(jsonObjectToObjectConverter
                        .convert(command.payloadAsJsonObject(), RecordMagsCourtHearingCommand.class).getHearing());

        for (MagsCourtHearingRecorded magsCourtHearingRecorded : hearings2Initiate) {

            List<Object> events = new ArrayList<>();
            final EventStream eventStream = eventSource.getStreamById(magsCourtHearingRecorded.getHearingId());
            final NewModelHearingAggregate aggregate = aggregateService.get(eventStream, NewModelHearingAggregate.class);

            events.addAll(
                    aggregate.initiate(
                            new NewSendingSheetTransformer().transform(magsCourtHearingRecorded.getOriginatingHearing(), magsCourtHearingRecorded.getHearingId())
                    ).collect(Collectors.toList())
            );


            HearingUpdatePleaCommand hearingUpdatePleaCommand = new NewSendingSheetTransformer()
                    .mapToUpdatePleaCommands(magsCourtHearingRecorded.getOriginatingHearing(), magsCourtHearingRecorded.getHearingId());

            final List<Offence> offences = hearingUpdatePleaCommand.getDefendants().stream().flatMap(d -> d.getOffences().stream()).collect(toList());
            for (final Offence offence : offences) {

                events.addAll(
                        aggregate.updatePlea(
                                magsCourtHearingRecorded.getHearingId(),
                                offence.getId(),
                                offence.getPlea().getPleaDate(),
                                offence.getPlea().getValue()
                        ).collect(Collectors.toList())
                );

            }

            eventStream.append(events.stream().map(enveloper.withMetadataFrom(command)));
        }
    }
}
