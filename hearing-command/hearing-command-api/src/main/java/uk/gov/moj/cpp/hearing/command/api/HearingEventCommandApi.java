package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(COMMAND_API)
public class HearingEventCommandApi {

    @Inject
    private Sender sender;

    @Handles("hearing.create-hearing-event-definitions")
    public void createHearingEventDefinitions(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.log-hearing-event")
    public void logHearingEvent(final JsonEnvelope command) {
        sender.send(command);
    }

    @Handles("hearing.correct-hearing-event")
    public void correctEvent(final JsonEnvelope command) {
        sender.send(command);
    }

}
