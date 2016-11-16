package uk.gov.moj.cpp.hearing.command.controller;

import javax.inject.Inject;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

@ServiceComponent(Component.COMMAND_CONTROLLER)
public class HearingCommandController {
    @Inject
    private Sender sender;

    @Handles("hearing.command.list-hearing")
    public void listHearing(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.command.vacate-hearing")
    public void vacateHearing(final JsonEnvelope envelope) {
        sender.send(envelope);
    }
}
