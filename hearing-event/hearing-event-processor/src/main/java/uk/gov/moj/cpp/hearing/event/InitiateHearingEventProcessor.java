package uk.gov.moj.cpp.hearing.event;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.justice.services.core.annotation.Component.EVENT_PROCESSOR;
import static uk.gov.justice.services.messaging.JsonEnvelope.envelopeFrom;
import static uk.gov.justice.services.messaging.JsonEnvelope.metadataFrom;

import uk.gov.justice.core.courts.ApplicationStatus;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationCase;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.JudicialResult;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.services.common.converter.JsonObjectToObjectConverter;
import uk.gov.justice.services.common.converter.ObjectToJsonObjectConverter;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.core.annotation.Handles;
import uk.gov.justice.services.core.annotation.ServiceComponent;
import uk.gov.justice.services.core.enveloper.Enveloper;
import uk.gov.justice.services.core.sender.Sender;
import uk.gov.justice.services.messaging.JsonEnvelope;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstCaseCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstDefendantCommand;
import uk.gov.moj.cpp.hearing.command.initiate.RegisterHearingAgainstOffenceCommand;
import uk.gov.moj.cpp.hearing.event.service.ProgressionService;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingInitiated;
import uk.gov.moj.cpp.hearing.repository.HearingRepository;
import uk.gov.moj.cpp.util.HearingDetailUtil;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;

import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"squid:S1188"})
@ServiceComponent(EVENT_PROCESSOR)
public class InitiateHearingEventProcessor {

    public static final String APPEAL = "APPEAL";
    public static final String STAT_DEC = "STAT_DEC";
    private static final Map<String, String> ENFORCEMENT_NOTIFICATION_APPLICATION_TYPES = ImmutableMap.<String, String>builder()
            .put("MC80527", STAT_DEC)
            .put("MC80528", STAT_DEC)
            .put("MC80524", "REOPEN")
            .put("MC80802", APPEAL)
            .put("MC80803", APPEAL)
            .put("MC80801", APPEAL)
            .build();

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Logger LOGGER = LoggerFactory.getLogger(InitiateHearingEventProcessor.class);
    public static final String COURT_APPLICATIONS = "courtApplications";
    public static final String NEXT_HEARING = "Next Hearing";
    @Inject
    private Enveloper enveloper;
    @Inject
    private Sender sender;
    @Inject
    private JsonObjectToObjectConverter jsonObjectToObjectConverter;
    @Inject
    private ObjectToJsonObjectConverter objectToJsonObjectConverter;
    @Inject
    private StringToJsonObjectConverter stringToJsonObjectConverter;
    @Inject
    private ProgressionService progressionService;


    @Handles("hearing.events.initiated")
    public void hearingInitiated(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.initiated event received {}", event.toObfuscatedDebugString());
        }

        final InitiateHearingCommand initiateHearingCommand = this.jsonObjectToObjectConverter.convert(event.payloadAsJsonObject(), InitiateHearingCommand.class);

        final List<UUID> cases = new ArrayList<>();

        final List<ProsecutionCase> prosecutionCases = initiateHearingCommand.getHearing().getProsecutionCases();

        if (prosecutionCases != null) {
            prosecutionCases.forEach(prosecutionCase -> {

                prosecutionCase.getDefendants().forEach(defendant -> {

                    this.sender.send(Enveloper.envelop(RegisterHearingAgainstDefendantCommand.builder()
                            .withDefendantId(defendant.getId())
                            .withHearingId(initiateHearingCommand.getHearing().getId())
                            .build()).withName("hearing.command.register-hearing-against-defendant").withMetadataFrom(event));

                    for (final uk.gov.justice.core.courts.Offence offence : defendant.getOffences()) {

                        cases.add(prosecutionCase.getId());

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

        final Hearing hearing = initiateHearingCommand.getHearing();
        final PublicHearingInitiated publicHearingInitiated = PublicHearingInitiated.publicHearingInitiated()
                .setHearingId(hearing.getId())
                .setCases(cases)
                .setApplicationDetails(HearingDetailUtil.getApplicationDetails(hearing))
                .setCaseDetails(HearingDetailUtil.getCaseDetails(hearing))
                .setHearingDateTime(hearing.getHearingDays().get(0).getSittingDay())
                .setJurisdictionType(hearing.getJurisdictionType());


        final JsonObject eventPayload = this.objectToJsonObjectConverter.convert(publicHearingInitiated);
        this.sender.send(envelopeFrom(metadataFrom(event.metadata()).withName("public.hearing.initiated"), eventPayload));


        final List<CourtApplication> courtApplications = initiateHearingCommand.getHearing().getCourtApplications();
        ofNullable(courtApplications).map(Collection::stream).orElseGet(Stream::empty)
                .filter(courtApplication -> ENFORCEMENT_NOTIFICATION_APPLICATION_TYPES.containsKey(courtApplication.getType().getCode()))
                .filter(courtApplication -> courtApplication.getSubject().getMasterDefendant() != null)
                .filter(courtApplication -> !isApplicationFinalisedOrListed(event, courtApplication.getId()))
                .forEach(
                        courtApplication -> {
                            this.sender.send(Enveloper.envelop(createObjectBuilder()
                                    .add("applicationType", ENFORCEMENT_NOTIFICATION_APPLICATION_TYPES.get(courtApplication.getType().getCode()))
                                    .add("masterDefendantId", courtApplication.getSubject().getMasterDefendant().getMasterDefendantId().toString())
                                    .add("listingDate", dateTimeFormatter.format(initiateHearingCommand.getHearing().getHearingDays().get(0).getSittingDay()))
                                    .add("caseUrns", createCaseUrns(courtApplication).build())
                                    .add("hearingCourtCentreName", initiateHearingCommand.getHearing().getCourtCentre().getName())
                                    .add("caseOffenceIdList", createCaseOffenceIds(courtApplication.getCourtApplicationCases()))
                                    .build()).withName("public.hearing.nces-email-notification-for-application").withMetadataFrom(event));
                        });
    }

    @Handles("hearing.events.hearing-initiate-ignored")
    public void ignoreHearingInitiate(final JsonEnvelope event) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("hearing.events.hearing-initiate-ignored event received {}", event.toObfuscatedDebugString());
        }
        this.sender.send(Enveloper.envelop(event.payloadAsJsonObject()).withName("public.hearing.initiate-ignored").withMetadataFrom(event));
    }

    private Boolean isApplicationFinalisedOrListed(final JsonEnvelope event, final UUID applicationId) {
        final Optional<JsonObject> courtApplication = progressionService.getApplicationDetails(event, applicationId);
        if (courtApplication.isPresent()) {
            final CourtApplication courtApplicationObj = jsonObjectToObjectConverter.convert(courtApplication.get().getJsonObject("courtApplication"), CourtApplication.class);
            return courtApplicationObj.getApplicationStatus() == ApplicationStatus.FINALISED || hasNextHearing(courtApplicationObj.getJudicialResults());
        }
        return false;
    }

    private Boolean hasNextHearing(final List<JudicialResult> judicialResults) {
        return nonNull(judicialResults) && judicialResults.stream().anyMatch(result -> nonNull(result.getNextHearing()));
    }


    private JsonArrayBuilder createCaseUrns(final CourtApplication courtApplication) {
        final JsonArrayBuilder builder = createArrayBuilder();
        courtApplication.getCourtApplicationCases().stream()
                .map(cac -> ofNullable(cac.getProsecutionCaseIdentifier().getCaseURN()).orElse(cac.getProsecutionCaseIdentifier().getProsecutionAuthorityReference()))
                .forEach(builder::add);
        return builder;
    }

    private JsonArrayBuilder createCaseOffenceIds(final List<CourtApplicationCase> courtApplicationCases) {
        final JsonArrayBuilder builder = createArrayBuilder();
        if (isNotEmpty(courtApplicationCases)) {
            courtApplicationCases.stream()
                    .filter(cac -> isNotEmpty(cac.getOffences()))
                    .flatMap(cac -> cac.getOffences().stream())
                    .map(offence -> offence.getId().toString())
                    .forEach(builder::add);
        }
        return builder;
    }

}
