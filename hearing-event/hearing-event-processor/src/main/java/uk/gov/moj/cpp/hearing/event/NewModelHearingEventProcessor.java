package uk.gov.moj.cpp.hearing.event;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonValueConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

import javax.inject.Inject;
import javax.json.JsonString;

import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class NewModelHearingEventProcessor {

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Inject
    private ObjectToJsonValueConverter objectToJsonValueConverter;

    @Handles("hearing.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        JsonString hearingId = event.payloadAsJsonObject().getJsonObject("hearing").getJsonString("id");

        InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        for (Defendant defendant : initiateHearingCommand.getHearing().getDefendants()){
            for (Offence offence: defendant.getOffences()){
                this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence").apply(createObjectBuilder()
                        .add("hearingId", hearingId)
                        .add("offenceId", offence.getId().toString())
                        .add("caseId", offence.getCaseId().toString())
                        .add("defendantId", defendant.getId().toString())
                        .build()));
            }
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.initiated").apply(createObjectBuilder()
                .add("hearingId", hearingId)
                .build()));
    }

    @Handles("hearing.initiate-hearing-offence-enriched")
    public void hearingInitiateOffencePlea(final JsonEnvelope event) {

        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence-plea").apply(event.payloadAsJsonObject()));
    }
}
