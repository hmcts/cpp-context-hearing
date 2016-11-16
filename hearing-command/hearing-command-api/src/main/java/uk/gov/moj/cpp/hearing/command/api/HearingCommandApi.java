package uk.gov.moj.cpp.hearing.command.api;

import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;

@ServiceComponent(COMMAND_API)
public class HearingCommandApi {
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
