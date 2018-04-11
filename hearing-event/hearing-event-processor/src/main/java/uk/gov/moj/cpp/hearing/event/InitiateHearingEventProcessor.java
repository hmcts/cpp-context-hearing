package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import uk.gov.moj.cpp.hearing.domain.event.NewMagsCourtHearingRecorded;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;

import java.util.UUID;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@SuppressWarnings("WeakerAccess")
@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

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

        JsonArrayBuilder cases = createArrayBuilder();

        for (Defendant defendant : initiateHearingCommand.getHearing().getDefendants()) {
            for (Offence offence : defendant.getOffences()) {
                cases.add(offence.getCaseId().toString());
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
                .add("cases", cases.build())
                .build()));
    }

    @Handles("hearing.initiate-hearing-offence-enriched")
    public void hearingInitiateOffencePlea(final JsonEnvelope event) {

        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence-plea").apply(event.payloadAsJsonObject()));
    }
}
