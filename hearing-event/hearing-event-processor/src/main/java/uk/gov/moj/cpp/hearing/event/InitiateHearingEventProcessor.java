package uk.gov.moj.cpp.hearing.event;

import static java.util.Optional.ofNullable;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;

import uk.gov.justice.core.courts.CourtApplication;
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

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.JsonArrayBuilder;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188"})
@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

    public static final String APPEAL = "APPEAL";
    public static final String STAT_DEC = "STAT_DEC";
    private static final Map<String, String> APPLICATION_TYPE_LIST = ImmutableMap.<String, String>builder()
            .put("f3a6e917-7cc8-3c66-83dd-d958abd6a6e4", STAT_DEC)
            .put("7375727f-30fc-3f55-99f3-36adc4f0e70e", STAT_DEC)
            .put("44c238d9-3bc2-3cf3-a2eb-a7d1437b8383", "REOPEN")
            .put("57810183-a5c2-3195-8748-c6b97eda1ebd", APPEAL)
            .put("beb08419-0a9a-3119-b3ec-038d56c8a718", APPEAL)
            .put("36f3b0c3-9f75-31aa-a226-cfee69216160", APPEAL)
            .build();

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

        if (prosecutionCases!=null) {
            prosecutionCases.forEach(prosecutionCase -> {

                prosecutionCase.getDefendants().forEach(defendant -> {

                    this.sender.send(Enveloper.envelop(RegisterHearingAgainstDefendantCommand.builder()
                            .withDefendantId(defendant.getId())
                            .withHearingId(initiateHearingCommand.getHearing().getId())
                            .build()).withName("hearing.command.register-hearing-against-defendant").withMetadataFrom(event));

                    for (final uk.gov.justice.core.courts.Offence offence : defendant.getOffences()) {

                        cases.add(prosecutionCase.getId().toString());

                        this.sender.send(Enveloper.envelop(RegisterHearingAgainstOffenceCommand.registerHearingAgainstOffenceDefendantCommand()
                                .setHearingId(initiateHearingCommand.getHearing().getId())
                                .setOffenceId(offence.getId())).withName("hearing.command.register-hearing-against-offence")
                                .withMetadataFrom(event));
                    }
                });

                final RegisterHearingAgainstCaseCommand registerHearingAgainstCaseCommand = RegisterHearingAgainstCaseCommand.builder()
                        .withCaseId(prosecutionCase.getId())
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .build();

                this.sender.send(Enveloper.envelop(registerHearingAgainstCaseCommand)
                        .withName("hearing.command.register-hearing-against-case").withMetadataFrom(event));
            });
        }

        this.sender.send(Enveloper.envelop(createObjectBuilder()
                .add(HEARING_ID, initiateHearingCommand.getHearing().getId().toString())
                .add("cases", cases.build())
                .build()).withName("public.hearing.initiated").withMetadataFrom(event));

        final List<CourtApplication> courtApplications = initiateHearingCommand.getHearing().getCourtApplications();
        ofNullable(courtApplications).map(Collection::stream).orElseGet(Stream::empty)
                .filter(courtApplication -> APPLICATION_TYPE_LIST.containsKey(courtApplication.getType().getId().toString()))
                .filter(courtApplication -> courtApplication.getSubject().getMasterDefendant() != null)
                .forEach(courtApplication ->
                            this.sender.send(Enveloper.envelop(createObjectBuilder()
                                    .add("applicationType",APPLICATION_TYPE_LIST.get(courtApplication.getType().getId().toString()))
                                    .add("masterDefendantId", courtApplication.getSubject().getMasterDefendant().getMasterDefendantId().toString())
                                    .add("listingDate", dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))
                                    .add("caseUrns", createCaseUrns(courtApplication).build())
                                    .build()).withName("public.hearing.nces-email-notification-for-application").withMetadataFrom(event))
                );
    }

    private JsonArrayBuilder createCaseUrns(final CourtApplication courtApplication) {
        final JsonArrayBuilder builder = createArrayBuilder();
        courtApplication.getCourtApplicationCases().stream().map(cac -> ofNullable(cac.getProsecutionCaseIdentifier().getCaseURN()).orElse(cac.getProsecutionCaseIdentifier().getProsecutionAuthorityReference())).forEach(builder::add);
        return builder;
    }

    @Handles("hearing.events.hearing-initiate-ignored")
    public void ignoreHearingInitiate(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.hearing-initiate-ignored event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(Enveloper.envelop(event.payloadAsJsonObject()).withName("public.hearing.initiate-ignored").withMetadataFrom(event));
    }
}
