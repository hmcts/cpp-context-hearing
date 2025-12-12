package uk.gov.moj.cpp.hearing.command.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.eventsourcing.source.core.exception.EventStreamException;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.common.YouthCourtDefendantsInHearing;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_HANDLER;

@ServiceComponent(COMMAND_HANDLER)
public class YouthCourtDefendantsCommandHandler extends AbstractCommandHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(YouthCourtDefendantsCommandHandler.class.getName());
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.command.youth-court-defendants")
    public void updateYouthCourtDefendants(final JsonEnvelope envelope) throws EventStreamException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.command.youth-court-defendants command received {}", envelope.toObfuscatedDebugString());
        }
        final YouthCourtDefendantsInHearing youthCourtDefendantsInHearing = jsonObjectToObjectConverter.convert(envelope.payloadAsJsonObject(), YouthCourtDefendantsInHearing.class);
        aggregate(HearingAggregate.class, youthCourtDefendantsInHearing.getHearingId(), envelope, hearingAggregate -> hearingAggregate.receiveDefendantsPartOfYouthCourtHearing(youthCourtDefendantsInHearing.getYouthCourtDefendantIds()));
    }
}
