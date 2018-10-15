package uk.gov.moj.cpp.hearing.command.handler;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.DefendantId;
import uk.gov.moj.cpp.hearing.command.defenceCounsel.AddDefenceCounselCommand;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.DefenceCounselUpsert;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ServiceComponent(COMMAND_HANDLER)
public class AddDefenceCounselCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AddDefenceCounselCommandHandler.class.getName());

    @Handles("hearing.add-defence-counsel")
    public void addDefenceCounsel(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.add-defence-counsel event received {}", envelope.toObfuscatedDebugString());
        }

        final AddDefenceCounselCommand addDefenceCounselCommand = convertToObject(envelope, AddDefenceCounselCommand.class);
        final DefenceCounselUpsert defenceCounselUpsert = DefenceCounselUpsert.builder()
                .withHearingId(addDefenceCounselCommand.getHearingId())
                .withDefendantIds(
                        addDefenceCounselCommand.getDefendantIds().stream()
                                .map(DefendantId::getDefendantId)
                                .collect(Collectors.toList())
                )
                .withAttendeeId(addDefenceCounselCommand.getAttendeeId())
                .withPersonId(addDefenceCounselCommand.getPersonId())
                .withFirstName(addDefenceCounselCommand.getFirstName())
                .withLastName(addDefenceCounselCommand.getLastName())
                .withStatus(addDefenceCounselCommand.getStatus())
                .withTitle(addDefenceCounselCommand.getTitle())
                .build();

        aggregate(HearingAggregate.class, addDefenceCounselCommand.getHearingId(), envelope,
                hearingAggregate -> hearingAggregate.addDefenceCounsel(defenceCounselUpsert));
    }
}
