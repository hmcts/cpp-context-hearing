package uk.gov.moj.cpp.hearing.command.handler;

import static java.util.stream.Collectors.toList;
import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import java.util.List;

import javax.inject.Inject;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.aggregate.AggregateService;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.eventsourcing.source.core.EventSource;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.plea.HearingUpdatePleaCommand;
import uk.gov.moj.cpp.hearing.command.plea.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.OffenceAggregate;

@ServiceComponent(COMMAND_HANDLER)
public class NewModelUpdatePleaCommandHandler extends AbstractCommandHandler {

    @Inject
    public NewModelUpdatePleaCommandHandler(final EventSource eventSource, final Enveloper enveloper,
            final AggregateService aggregateService, final JsonObjectToObjectConverter jsonObjectToObjectConverter,
            final HearingCommandHandler hearingCommandHandler) {
        super(eventSource, enveloper, aggregateService, jsonObjectToObjectConverter, hearingCommandHandler);
    }

    @Handles("hearing.offence-plea-update")
    public void updatePlea(final JsonEnvelope envelope) throws EventStreamException {
        
        final HearingUpdatePleaCommand command = convertToObject(envelope, HearingUpdatePleaCommand.class);
        final List<Offence> offences = command.getDefendants().stream().flatMap(d -> d.getOffences().stream()).collect(toList());

        for (final Offence offence : offences) {
            aggregate(offence.getId(), envelope, OffenceAggregate.class, 
                    (a) -> a.updatePlea(command.getHearingId(), offence.getId(), offence.getPlea()));
        }
        
        //TODO: GPE-3032: sanitise
        hearingCommandHandler.updatePlea(envelope);
    }
}
