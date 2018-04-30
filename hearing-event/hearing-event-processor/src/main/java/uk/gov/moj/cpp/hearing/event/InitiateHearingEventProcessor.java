package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;

import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;

@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

    private static final String HEARING_ID = "hearingId";
    private static final String OFFENCE_ID = "offenceId";
    private static final String CASE_ID = "caseId";
    private static final String DEFENDANT_ID = "defendantId";
    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        final JsonString hearingId = event.payloadAsJsonObject().getJsonObject("hearing").getJsonString("id");

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        final JsonArrayBuilder cases = createArrayBuilder();

        for (final Defendant defendant : initiateHearingCommand.getHearing().getDefendants()) {
            for (final Offence offence : defendant.getOffences()) {
                cases.add(offence.getCaseId().toString());
                this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence").apply(createObjectBuilder()
                        .add(HEARING_ID, hearingId)
                        .add(OFFENCE_ID, offence.getId().toString())
                        .add(CASE_ID, offence.getCaseId().toString())
                        .add(DEFENDANT_ID, defendant.getId().toString())
                        .build()));
            }
            this.sender.send(this.enveloper.withMetadataFrom(event,
                            "hearing.command.initiate-hearing-defence-witness-enrich")
                            .apply(createObjectBuilder().add(HEARING_ID, hearingId)
                                            .add(DEFENDANT_ID, defendant.getId().toString())
                                            .build()));

        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.initiated").apply(createObjectBuilder()
                .add(HEARING_ID, hearingId)
                .add("cases", cases.build())
                .build()));
    }

    @Handles("hearing.initiate-hearing-offence-enriched")
    public void hearingInitiateOffencePlea(final JsonEnvelope event) {
        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence-plea").apply(event.payloadAsJsonObject()));
    }
}
