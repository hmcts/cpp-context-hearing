package uk.gov.moj.cpp.hearing.command.api;

import static uk.gov.justice.services.core.annotation.Component.COMMAND_API;
import static uk.gov.justice.services.messaging.JsonObjects.createObjectBuilder;

import uk.gov.justice.services.common.util.Clock;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;

import javax.inject.Inject;
import javax.json.JsonObject;

@ServiceComponent(COMMAND_API)
public class OutstandingFinesCommandApi {

    public static final String FIELD_HEARING_DATE = "hearingDate";
    private final Sender sender;
    private final Enveloper enveloper;
    private final Clock clock;

    @Inject
    public OutstandingFinesCommandApi(final Sender sender, final Enveloper enveloper, final Clock clock) {
        this.sender = sender;
        this.enveloper = enveloper;
        this.clock = clock;
    }

    @Handles("hearing.add-request-for-outstanding-fines")
    public void addRequestForOutstandingFines(final JsonEnvelope envelope) {
        JsonObject commandObject = envelope.payloadAsJsonObject();
        if (commandObject.get(FIELD_HEARING_DATE) == null || commandObject.isNull(FIELD_HEARING_DATE)) {
            commandObject = createObjectBuilder(commandObject)
                    .add(FIELD_HEARING_DATE, clock.now().toLocalDate().plusDays(1).toString()) // default next Day
                    .build();
        }

        this.sender.send(this.enveloper.withMetadataFrom(envelope, "hearing.command.add-request-for-outstanding-fines").apply(commandObject));
    }
}