package uk.gov.moj.cpp.hearing.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.Defendant;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterDefendantWithHearingCommand;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;
import javax.json.JsonString;
import java.util.UUID;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventProcessor.class);

    @Handles("hearing.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        LOGGER.debug("hearing.initiated event received {}", event.payloadAsJsonObject());

        JsonString hearingId = event.payloadAsJsonObject().getJsonObject("hearing").getJsonString("id");

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        final JsonArrayBuilder cases = createArrayBuilder();

        for (final Defendant defendant : initiateHearingCommand.getHearing().getDefendants()) {

            final RegisterDefendantWithHearingCommand command = RegisterDefendantWithHearingCommand.builder()
                    .withDefendantId(defendant.getId())
                    .withHearingId(UUID.fromString(hearingId.getString()))
                    .build();

            this.sender.send(this.enveloper
                    .withMetadataFrom(event, "hearing.command.register-defendant-with-hearing")
                    .apply(command));

            for (Offence offence : defendant.getOffences()) {
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
        LOGGER.debug("hearing.initiate-hearing-offence-enriched event received {}", event.payloadAsJsonObject());
        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.initiate-hearing-offence-plea").apply(event.payloadAsJsonObject()));
    }
}
