package uk.gov.moj.cpp.hearing.event;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;

import java.util.List;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188"})
@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

    private static final String HEARING_ID = "hearingId";
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventProcessor.class);
    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    @Handles("hearing.events.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.initiated event received {}", event.toObfuscatedDebugString());
        }

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        final JsonArrayBuilder cases = createArrayBuilder();

        final List<ProsecutionCase> prosecutionCases = initiateHearingCommand.getHearing().getProsecutionCases();

        prosecutionCases.forEach(prosecutionCase -> {

            prosecutionCase.getDefendants().forEach(defendant -> {

                this.sender.send(this.enveloper
                        .withMetadataFrom(event, "hearing.command.register-hearing-against-defendant")
                        .apply(RegisterHearingAgainstDefendantCommand.builder()
                                .withDefendantId(defendant.getId())
                                .withHearingId(initiateHearingCommand.getHearing().getId())
                                .build()));

                for (final uk.gov.justice.core.courts.Offence offence : defendant.getOffences()) {

                    cases.add(prosecutionCase.getId().toString());

                    this.sender.send(this.enveloper
                            .withMetadataFrom(event, "hearing.command.register-hearing-against-offence")
                            .apply(RegisterHearingAgainstOffenceCommand.registerHearingAgainstOffenceDefendantCommand()
                                    .setHearingId(initiateHearingCommand.getHearing().getId())
                                    .setOffenceId(offence.getId())
                            ));
                }
            });

            final RegisterHearingAgainstCaseCommand registerHearingAgainstCaseCommand = RegisterHearingAgainstCaseCommand.builder()
                    .withCaseId(prosecutionCase.getId())
                    .withHearingId(initiateHearingCommand.getHearing().getId())
                    .build();

            this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.register-hearing-against-case")
                    .apply(registerHearingAgainstCaseCommand));
        });

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.initiated").apply(createObjectBuilder()
                .add(HEARING_ID, initiateHearingCommand.getHearing().getId().toString())
                .add("cases", cases.build())
                .build()));
    }

    @Handles("hearing.events.found-plea-for-hearing-to-inherit")
    public void hearingInitiateOffencePlea(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.found-plea-for-hearing-to-inherit event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.update-hearing-with-inherited-plea").apply(event.payloadAsJsonObject()));
    }

    @Handles("hearing.events.found-verdict-for-hearing-to-inherit")
    public void hearingInitiateOffenceVerdict(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.found-verdict-for-hearing-to-inherit event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.update-hearing-with-inherited-verdict").apply(event.payloadAsJsonObject()));
    }
}
