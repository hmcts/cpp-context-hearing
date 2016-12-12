package uk.gov.moj.cpp.hearing.command.controller;

import uk.gov.justice.services.core.annotation.Component;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

@ServiceComponent(Component.COMMAND_CONTROLLER)
public class HearingCommandController {
    @Inject
    private Sender sender;

    @Handles("hearing.initiate-hearing")
    public void initiateHearing(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.allocate-court")
    public void allocateCourt(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.book-room")
    public void bookRoom(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.start")
    public void end(JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.end")
    public void start(JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.add-prosecution-counsel")
    public void addProsecutionCounsel(final JsonEnvelope envelope) {
        sender.send(envelope);
    }

    @Handles("hearing.add-case")
    public void addCase(final JsonEnvelope envelope) {
        sender.send(envelope);
    }
}