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
import uk.gov.moj.cpp.hearing.command.initiate.DefendantCase;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.LookupPleaOnOffenceForHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.Offence;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;

import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

    private static final String HEARING_ID = "hearingId";
    private static final String DEFENDANT_ID = "defendantId";

    @Inject
    private Enveloper enveloper;

    @Inject
    private Sender sender;

    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;

    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventProcessor.class);

    @Handles("hearing.events.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        LOGGER.debug("hearing.events.initiated event received {}", event.payloadAsJsonObject());

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        final JsonArrayBuilder cases = createArrayBuilder();

        for (final Defendant defendant : initiateHearingCommand.getHearing().getDefendants()) {

            this.sender.send(this.enveloper
                    .withMetadataFrom(event, "hearing.command.register-hearing-against-defendant")
                    .apply(RegisterHearingAgainstDefendantCommand.builder()
                            .withDefendantId(defendant.getId())
                            .withHearingId(initiateHearingCommand.getHearing().getId())
                            .build()));

            for (Offence offence : defendant.getOffences()) {
                cases.add(offence.getCaseId().toString());

                this.sender.send(this.enveloper
                        .withMetadataFrom(event, "hearing.command.lookup-plea-on-offence-for-hearing")
                        .apply(LookupPleaOnOffenceForHearingCommand.lookupPleaOnOffenceForHearingCommand()
                                .setHearingId(initiateHearingCommand.getHearing().getId())
                                .setDefendantId(defendant.getId())
                                .setOffenceId(offence.getId())
                                .setCaseId(offence.getCaseId())
                        ));
            }

            this.sender.send(this.enveloper.withMetadataFrom(event,
                    "hearing.command.lookup-witnesses-on-defendant-for-hearing")
                    .apply(createObjectBuilder().add(HEARING_ID, initiateHearingCommand.getHearing().getId().toString())
                            .add(DEFENDANT_ID, defendant.getId().toString())
                            .build()));

            for (DefendantCase defendantCase : defendant.getDefendantCases()) {

                final RegisterHearingAgainstCaseCommand registerHearingAgainstCaseCommand = RegisterHearingAgainstCaseCommand.builder()
                        .withCaseId(defendantCase.getCaseId())
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .build();

                this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.register-hearing-against-case")
                        .apply(registerHearingAgainstCaseCommand));
            }
        }

        this.sender.send(this.enveloper.withMetadataFrom(event, "public.hearing.initiated").apply(createObjectBuilder()
                .add(HEARING_ID, initiateHearingCommand.getHearing().getId().toString())
                .add("cases", cases.build())
                .build()));
    }

    @Handles("hearing.events.found-plea-for-hearing-to-inherit")
    public void hearingInitiateOffencePlea(final JsonEnvelope event) {
        LOGGER.debug("hearing.events.found-plea-for-hearing-to-inherit event received {}", event.payloadAsJsonObject());
        this.sender.send(this.enveloper.withMetadataFrom(event, "hearing.command.update-hearing-with-inherited-plea").apply(event.payloadAsJsonObject()));
    }
}
