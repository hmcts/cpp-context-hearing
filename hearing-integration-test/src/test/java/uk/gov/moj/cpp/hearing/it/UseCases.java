package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static javax.json.Json.createObjectBuilder;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs.enforcementAreaBacs;
import static uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit.organisationalUnit;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataWithRandomUUID;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_ZONED_DATE_TIME;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.USER_ID_VALUE_AS_ADMIN;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getRequestSpec;
import static uk.gov.moj.cpp.hearing.it.AbstractIT.getStringFromResource;
import static uk.gov.moj.cpp.hearing.it.MatcherUtil.getPastDate;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearing;
import static uk.gov.moj.cpp.hearing.it.Queries.pollForHearingEvents;
import static uk.gov.moj.cpp.hearing.it.Utilities.JsonUtil.objectToJsonObject;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.AddProsecutionCounselCommandTemplates.addProsecutionCounselCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.WireMockStubUtils.setupNoProsecutionCaseByHearingId;

import uk.gov.justice.core.courts.Address;
import uk.gov.justice.core.courts.ContactNumber;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtApplicationParty;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DefendantCase;
import uk.gov.justice.core.courts.Gender;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.InterpreterIntermediary;
import uk.gov.justice.core.courts.JurisdictionType;
import uk.gov.justice.core.courts.Marker;
import uk.gov.justice.core.courts.MasterDefendant;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Person;
import uk.gov.justice.core.courts.PersonDefendant;
import uk.gov.justice.core.courts.ProsecutingAuthority;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;
import uk.gov.justice.core.courts.ProsecutionCounsel;
import uk.gov.justice.core.courts.RespondentCounsel;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.hearing.courts.AddApplicantCounsel;
import uk.gov.justice.hearing.courts.AddCompanyRepresentative;
import uk.gov.justice.hearing.courts.AddDefenceCounsel;
import uk.gov.justice.hearing.courts.AddProsecutionCounsel;
import uk.gov.justice.hearing.courts.AddRespondentCounsel;
import uk.gov.justice.hearing.courts.RemoveApplicantCounsel;
import uk.gov.justice.hearing.courts.RemoveCompanyRepresentative;
import uk.gov.justice.hearing.courts.RemoveDefenceCounsel;
import uk.gov.justice.hearing.courts.RemoveInterpreterIntermediary;
import uk.gov.justice.hearing.courts.RemoveProsecutionCounsel;
import uk.gov.justice.hearing.courts.RemoveRespondentCounsel;
import uk.gov.justice.hearing.courts.UpdateApplicantCounsel;
import uk.gov.justice.hearing.courts.UpdateCompanyRepresentative;
import uk.gov.justice.hearing.courts.UpdateDefenceCounsel;
import uk.gov.justice.hearing.courts.UpdateInterpreterIntermediary;
import uk.gov.justice.hearing.courts.UpdateProsecutionCounsel;
import uk.gov.justice.hearing.courts.UpdateRespondentCounsel;
import uk.gov.justice.hearing.courts.referencedata.EnforcementAreaBacs;
import uk.gov.justice.hearing.courts.referencedata.OrganisationalUnit;
import uk.gov.justice.progression.events.CaseDefendantDetails;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.converter.jackson.ObjectMapperProducer;
import uk.gov.moj.cpp.hearing.command.HearingVacatedTrialCleared;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.bookprovisional.ProvisionalHearingSlotInfo;
import uk.gov.moj.cpp.hearing.command.defendant.UpdateDefendantAttendanceCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.hearing.details.HearingVacatedTrialDetailsUpdateCommand;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.logEvent.LogEventCommand;
import uk.gov.moj.cpp.hearing.command.offence.UpdateOffencesForDefendantCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareResultsCommand;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLine;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.command.verdict.HearingUpdateVerdictCommand;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.event.CpsProsecutorUpdated;
import uk.gov.moj.cpp.hearing.domain.event.RespondentCounselChangeIgnored;
import uk.gov.moj.cpp.hearing.domain.updatepleas.UpdatePleaCommand;
import uk.gov.moj.cpp.hearing.eventlog.CourtCentre;
import uk.gov.moj.cpp.hearing.eventlog.HearingEvent;
import uk.gov.moj.cpp.hearing.eventlog.HearingEventDefinition;
import uk.gov.moj.cpp.hearing.eventlog.PublicHearingEventLogged;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.json.JSONObject;

public class UseCases {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public static <T> Consumer<T> asDefault() {
        return c -> {
        };
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        return initiateHearing(requestSpec, initiateHearing, true, true, true, false, false, false);
    }

    public static InitiateHearingCommand initiateHearingWithoutBreachApplication(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        return initiateHearing(requestSpec, initiateHearing, false, false, true, false, false, false);
    }

    public static InitiateHearingCommand initiateHearingWithNsp(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {

        return initiateHearing(requestSpec, initiateHearing, false, false, true, false, true, false);
    }

    public static InitiateHearingCommand initiateHearingForApplication(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing) {
        return initiateHearing(requestSpec, initiateHearing, true, true, false, false, false, false);
    }

    public static InitiateHearingCommand initiateHearing(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearing, final boolean includeApplicationCases, final boolean includeApplicationOrder, final boolean includeProsecutionCase, final boolean includeMasterDefendantInSubject, final boolean isNsp, final boolean includeProsecutingAuthority) {

        Hearing hearing = initiateHearing.getHearing();

        if (!includeApplicationCases && !includeApplicationOrder) {
            hearing.setCourtApplications(null);
        } else if (!includeApplicationCases) {
            hearing.getCourtApplications().forEach(app -> app.setCourtApplicationCases(null));
        } else if (!includeApplicationOrder) {
            hearing.getCourtApplications().forEach(app -> app.setCourtOrder(null));
        }
        if (includeMasterDefendantInSubject) {
            final UUID masterDefendantId = randomUUID();

            hearing.getCourtApplications().get(0).getSubject().setMasterDefendant(MasterDefendant.masterDefendant()
                    .withMasterDefendantId(masterDefendantId)
                    .withDefendantCase(Arrays.asList(DefendantCase.defendantCase()
                            .withDefendantId(masterDefendantId)
                            .withCaseId(hearing.getProsecutionCases().get(0).getId())
                            .withCaseReference(hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getProsecutionAuthorityReference())
                            .build()))
                    .withPersonDefendant(PersonDefendant.personDefendant()
                            .withPersonDetails(Person.person()
                                    .withLastName(STRING.next())
                                    .withGender(Gender.MALE)
                                    .build())
                            .withDriverNumber("DVLA12345")
                            .build())
                    .build());
        }
        if (nonNull(hearing.getCourtApplications()) && nonNull(hearing.getCourtApplications().get(0))) {
            hearing.getCourtApplications().get(0).setHearingIdToBeVacated(UUID.randomUUID());

            if (includeProsecutingAuthority) {
                Address address = Address.address().withAddress1("address1")
                        .withAddress2("address2")
                        .withPostcode("CB3 0GU").build();
                ContactNumber contact = ContactNumber.contactNumber().withPrimaryEmail("James.Thomas@gmail.com").build();
                ProsecutingAuthority prosecutingAuthority = ProsecutingAuthority.prosecutingAuthority()
                        .withProsecutionAuthorityId(randomUUID())
                        .withProsecutionAuthorityCode("ABC")
                        .withName("ABC Org")
                        .withAddress(address)
                        .withContact(contact)
                        .build();
                CourtApplicationParty party = CourtApplicationParty.courtApplicationParty()
                        .withProsecutingAuthority(prosecutingAuthority)
                        .withId(randomUUID())
                        .withSummonsRequired(false)
                        .withNotificationRequired(false)
                        .build();
                hearing.getCourtApplications().get(0).setRespondents(Arrays.asList(party));
            }

        }
        if (!includeProsecutionCase) {
            hearing.setProsecutionCases(null);
        }

        if (isNsp) {
            hearing.getProsecutionCases().stream().forEach(prosecutionCase -> prosecutionCase.getProsecutionCaseIdentifier().setAddress(Address.address()
                            .withPostcode("E14 4EX").withAddress1("line 1").withAddress2("line 2").withAddress3("line 3").withAddress4("line 4").withAddress5("line 5").build())
                    .setProsecutionAuthorityName("ProsecutionAuthorityName")
                    .setContact(ContactNumber.contactNumber().withPrimaryEmail("contact@cpp.co.uk").build())
                    .setProsecutorCategory("Charity")
                    .setMajorCreditorCode(null));
        }

        makeCommand(requestSpec, "hearing.initiate")
                .ofType("application/vnd.hearing.initiate+json")
                .withPayload(initiateHearing)
                .executeSuccessfully();

        BeanMatcher<HearingDetailsResponse> resultMatcher = isBean(HearingDetailsResponse.class);
        final List<ProsecutionCase> prosecutionCases = hearing.getProsecutionCases();
        if (isNotEmpty(prosecutionCases)) {
            if (nonNull(hearing.getIsGroupProceedings()) && hearing.getIsGroupProceedings()) {
                ProsecutionCase masterProsecutionCase = hearing.getProsecutionCases().stream()
                        .filter(prosecutionCase -> prosecutionCase.getIsGroupMaster().equals(true))
                        .findFirst().get();
                resultMatcher.with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, getProsecutionCasesMatcher(Arrays.asList(masterProsecutionCase))));

            } else {
                resultMatcher.with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getProsecutionCases, getProsecutionCasesMatcher(prosecutionCases)));
            }
        }
        getHearingPollForMatch(hearing.getId(), resultMatcher);


        return initiateHearing;
    }

    private static Matcher<Iterable<ProsecutionCase>> getProsecutionCasesMatcher(final List<ProsecutionCase> prosecutionCases) {
        return hasItems(
                prosecutionCases.stream().map(prosecutionCase -> isBean(ProsecutionCase.class)
                        .with(ProsecutionCase::getId, Matchers.is(prosecutionCase.getId()))
                        .with(ProsecutionCase::getDefendants, hasItems(
                                prosecutionCase.getDefendants().stream().map(
                                        defendant -> isBean(Defendant.class)
                                                .with(Defendant::getId, Matchers.is(defendant.getId()))
                                                .with(Defendant::getOffences, hasItems(
                                                        defendant.getOffences().stream().map(offence ->
                                                                isBean(Offence.class).with(Offence::getId, Matchers.is(offence.getId()))
                                                        ).toArray((IntFunction<Matcher<Offence>[]>) Matcher[]::new)
                                                ))
                                ).toArray((IntFunction<Matcher<Defendant>[]>) Matcher[]::new)

                        ))
                ).toArray((IntFunction<Matcher<ProsecutionCase>[]>) Matcher[]::new)
        );
    }

    public static UpdatePleaCommand updatePlea(final RequestSpecification requestSpec, final UUID hearingId, final UUID offenceId,
                                               final UpdatePleaCommand hearingUpdatePleaCommand) {

        final EventListener eventListener = listenFor("public.hearing.plea-updated")
                .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));

        makeCommand(requestSpec, "hearing.update-hearing")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(hearingUpdatePleaCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdatePleaCommand;
    }

    public static UpdatePleaCommand updatePleaNoAdditionalCheck(final RequestSpecification requestSpec, final UUID hearingId,
                                                                final UpdatePleaCommand hearingUpdatePleaCommand) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(hearingUpdatePleaCommand)
                .executeSuccessfully();

        return hearingUpdatePleaCommand;
    }

    public static UpdatePleaCommand updatePlea(final RequestSpecification requestSpec, final UUID hearingId, final UUID offenceId,
                                               final UpdatePleaCommand hearingUpdatePleaCommand, final UUID applicationId) {

        final EventListener eventListener;
        if (offenceId != null) {
            eventListener = listenFor("public.hearing.plea-updated")
                    .withFilter(isJson(withJsonPath("$.offenceId", is(offenceId.toString()))));
        } else {
            eventListener = listenFor("public.hearing.plea-updated")
                    .withFilter(isJson(withJsonPath("$.applicationId", is(applicationId.toString()))));
        }

        makeCommand(requestSpec, "hearing.update-hearing")
                .withArgs(hearingId)
                .ofType("application/vnd.hearing.update-plea+json")
                .withPayload(hearingUpdatePleaCommand)
                .executeSuccessfully();

        eventListener.waitFor();

        return hearingUpdatePleaCommand;
    }

    public static HearingUpdateVerdictCommand updateVerdict(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {

        final EventListener eventListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearingId.toString()))));

        updateVerdictNoAdditionalCheck(requestSpec, hearingId, hearingUpdateVerdictCommand);

        eventListener.waitFor();

        return hearingUpdateVerdictCommand;
    }

    public static HearingUpdateVerdictCommand updateVerdictNoAdditionalCheck(final RequestSpecification requestSpec, final UUID hearingId,
                                                                             final HearingUpdateVerdictCommand hearingUpdateVerdictCommand) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-verdict+json")
                .withArgs(hearingId)
                .withPayload(hearingUpdateVerdictCommand)
                .executeSuccessfully();

        return hearingUpdateVerdictCommand;
    }

    public static LogEventCommand logEvent(final UUID hearingEventId,
                                           final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                                           final boolean alterable,
                                           final UUID defenceCounselId,
                                           final ZonedDateTime eventTime,
                                           final String recordedLabel, String note) {
        stubOrganisationUnit(initiateHearingCommand.getHearing().getCourtCentre().getId().toString());

        final EnforcementAreaBacs enforcementAreaBacs = enforcementAreaBacs()
                .withBankAccntName("account name")
                .withBankAccntNum(1)
                .withBankAccntSortCode("867878")
                .withBankAddressLine1("address1")
                .withRemittanceAdviceEmailAddress("test@test.com")
                .build();

        final OrganisationalUnit organisationalUnit = organisationalUnit()
                .withOucode(null)
                .withLja(null)
                .withId(initiateHearingCommand.getHearing().getCourtCentre().getId().toString())
                .withIsWelsh(true)
                .withAddress1("address 1")
                .withWelshAddress1("Welsh address1")
                .withWelshAddress2("Welsh address2")
                .withWelshAddress3("Welsh address3")
                .withWelshAddress4("Welsh address4")
                .withWelshAddress4("Welsh address5")
                .withPostcode("AL4 9LG")
                .withOucodeL3WelshName(initiateHearingCommand.getHearing().getCourtCentre().getWelshName())
                .withEnforcementArea(enforcementAreaBacs)
                .build();
        ReferenceDataStub.stub(organisationalUnit);

        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(hearingEventId)
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(eventTime)
                        .withLastModifiedTime(getPastDate())
                        .withRecordedLabel(recordedLabel)
                        .withDefenceCounselId(defenceCounselId)
                        .withAlterable(alterable)
                        .withNote(note)
                , consumer).build();


        postHearingLogEventCommand(requestSpec, initiateHearingCommand, logEvent);
        pollForHearingEvents(initiateHearingCommand.getHearing().getId().toString(), eventTime.toLocalDate(), new Matcher[]{
                withJsonPath("$.hearingId", is(initiateHearingCommand.getHearing().getId().toString()))
        });


        return logEvent;
    }

    public static void postHearingLogEventCommand(final RequestSpecification requestSpec, final InitiateHearingCommand initiateHearingCommand, final LogEventCommand logEvent) {
        makeCommand(requestSpec, "hearing.log-hearing-event")
                .withArgs(initiateHearingCommand.getHearing().getId())
                .ofType("application/vnd.hearing.log-hearing-event+json")
                .withPayload(logEvent)
                .executeSuccessfully();
    }

    public static String getReference(final Hearing hearing) {
        String reference = "";
        if (isNotEmpty(hearing.getProsecutionCases())) {
            final ProsecutionCaseIdentifier prosecutionCaseIdentifier = hearing.getProsecutionCases().get(0).getProsecutionCaseIdentifier();
            reference = isNull(prosecutionCaseIdentifier.getProsecutionAuthorityReference()) ? prosecutionCaseIdentifier.getCaseURN() : prosecutionCaseIdentifier.getProsecutionAuthorityReference();
        } else if (isNotEmpty(hearing.getCourtApplications())) {
            reference = hearing.getCourtApplications().get(0).getApplicationReference();
        }
        return reference;
    }

    public static LogEventCommand logEvent(final UUID hearingEventId,
                                           final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                                           final boolean alterable,
                                           final UUID defenceCounselId,
                                           final ZonedDateTime eventTime, String note) {
        return logEvent(hearingEventId, requestSpec, consumer, initiateHearingCommand, hearingEventDefinitionId, alterable, defenceCounselId, eventTime, STRING.next(), note);
    }

    public static LogEventCommand logEvent(final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                                           final boolean alterable,
                                           final UUID defenceCounselId,
                                           final ZonedDateTime eventTime, String note) {
        return logEvent(randomUUID(), requestSpec, consumer, initiateHearingCommand, hearingEventDefinitionId, alterable, defenceCounselId, eventTime, STRING.next(), note);
    }

    public static boolean amendHearing(final RequestSpecification requestSpec, final UUID hearingId, final HearingState newHearingState) {

        JSONObject json = new JSONObject();

        json.put("hearingId", hearingId);
        json.put("newHearingState", newHearingState.toString());

        makeCommand(requestSpec, "hearing.amend")
                .ofType("application/vnd.hearing.amend+json")
                .withArgs(hearingId)
                .withPayload(json.toString())
                .executeSuccessfully();

        return true;
    }

    private static Stream<SharedResultsCommandResultLine> sharedResultsCommandResultLineStream(final Target target) {
        return target.getResultLines().stream().map(resultLineIn ->
                new SharedResultsCommandResultLine(resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        target.getOffenceId(),
                        target.getDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream().map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(),
                                p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef())).collect(toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        target.getApplicationId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        nonNull(resultLineIn.getAmendmentDate()) ? resultLineIn.getAmendmentDate() : null,
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        null,
                        null,
                        target.getShadowListed(),
                        target.getDraftResult()));
    }

    private static Stream<SharedResultsCommandResultLineV2> sharedResultsResultLinePerDay(final Target target) {
        return target.getResultLines().stream().map(resultLineIn ->
                new SharedResultsCommandResultLineV2(
                        "SHORT_CODE", //resultLineIn.getShortCode(),
                        resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        target.getOffenceId(),
                        target.getDefendantId(),
                        target.getMasterDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream()
                                .map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(), p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef()))
                                .collect(toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        target.getApplicationId(),
                        UUID.randomUUID(),//target.getCaseId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        ZonedDateTime.now(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        resultLineIn.getChildResultLineIds(),
                        resultLineIn.getParentResultLineIds(),
                        target.getShadowListed(),
                        target.getDraftResult(),
                        "AMENDMENTS_LOG",
                        "I",
                        false));
    }

    private static Stream<SharedResultsCommandResultLineV2> sharedResultsResultLinePerDay(final Target target, final UUID caseId) {
        return target.getResultLines().stream().map(resultLineIn ->
                new SharedResultsCommandResultLineV2(
                        "SHORT_CODE", //resultLineIn.getShortCode(),
                        resultLineIn.getDelegatedPowers(),
                        resultLineIn.getOrderedDate(),
                        resultLineIn.getSharedDate(),
                        resultLineIn.getResultLineId(),
                        target.getOffenceId(),
                        target.getDefendantId(),
                        target.getMasterDefendantId(),
                        resultLineIn.getResultDefinitionId(),
                        resultLineIn.getPrompts().stream()
                                .map(p -> new SharedResultsCommandPrompt(p.getId(), p.getLabel(), p.getFixedListCode(), p.getValue(), p.getWelshValue(), p.getWelshLabel(), p.getPromptRef()))
                                .collect(toList()),
                        resultLineIn.getResultLabel(),
                        resultLineIn.getLevel().name(),
                        resultLineIn.getIsModified(),
                        resultLineIn.getIsComplete(),
                        target.getApplicationId(),
                        caseId,//target.getCaseId(),
                        resultLineIn.getAmendmentReasonId(),
                        resultLineIn.getAmendmentReason(),
                        ZonedDateTime.now(),
                        resultLineIn.getFourEyesApproval(),
                        resultLineIn.getApprovedDate(),
                        resultLineIn.getIsDeleted(),
                        resultLineIn.getChildResultLineIds(),
                        resultLineIn.getParentResultLineIds(),
                        target.getShadowListed(),
                        target.getDraftResult(),
                        "AMENDMENTS_LOG",
                        "A",
                        true));

    }

    public static ShareResultsCommand shareResults(final RequestSpecification requestSpec, final UUID hearingId, final ShareResultsCommand shareResultsCommand, final List<Target> targets) {

        // TODO GPE-6699
        shareResultsCommand.setResultLines(
                targets.stream()
                        .flatMap(target -> sharedResultsCommandResultLineStream(target))
                        .collect(Collectors.toList()));


        makeCommand(requestSpec, "hearing.share-results")
                .ofType("application/vnd.hearing.share-results+json")
                .withArgs(hearingId)
                .withPayload(shareResultsCommand)
                .executeSuccessfully();

        return shareResultsCommand;
    }

    public static ShareDaysResultsCommand shareResultsPerDay(final RequestSpecification requestSpec, final UUID hearingId, final ShareDaysResultsCommand command, final List<Target> targets) {

        command.setResultLines(
                targets.stream()
                        .flatMap(target -> sharedResultsResultLinePerDay(target))
                        .collect(toList()));


        makeCommand(requestSpec, "hearing.share-days-results")
                .ofType("application/vnd.hearing.shared-results+json")
                .withArgs(hearingId, command.getHearingDay())
                .withPayload(command)
                .executeSuccessfully();

        return command;
    }

    public static ShareDaysResultsCommand shareResultsPerDay(final RequestSpecification requestSpec, final UUID hearingId, final UUID caseId, final ShareDaysResultsCommand command, final List<Target> targets) {

        command.setResultLines(
                targets.stream()
                        .flatMap(target -> sharedResultsResultLinePerDay(target, caseId))
                        .collect(toList()));


        makeCommand(requestSpec, "hearing.share-days-results")
                .ofType("application/vnd.hearing.shared-results+json")
                .withArgs(hearingId, command.getHearingDay())
                .withPayload(command)
                .executeSuccessfully();

        return command;
    }

    public static AddDefenceCounsel addDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                      final AddDefenceCounsel addDefenceCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(addDefenceCounsel)
                .executeSuccessfully();

        return addDefenceCounsel;
    }

    public static AddCompanyRepresentative addCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId,
                                                                    final AddCompanyRepresentative addCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-company-representative+json")
                .withArgs(hearingId)
                .withPayload(addCompanyRepresentative)
                .executeSuccessfully();

        return addCompanyRepresentative;
    }

    public static UpdateCompanyRepresentative updateCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId, final UpdateCompanyRepresentative updateCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-company-representative+json")
                .withArgs(hearingId)
                .withPayload(updateCompanyRepresentative)
                .executeSuccessfully();

        return updateCompanyRepresentative;
    }

    public static RemoveCompanyRepresentative removeCompanyRepresentative(final RequestSpecification requestSpec, final UUID hearingId, final RemoveCompanyRepresentative removeCompanyRepresentative) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-company-representative+json")
                .withArgs(hearingId)
                .withPayload(removeCompanyRepresentative)
                .executeSuccessfully();

        return removeCompanyRepresentative;
    }

    public static AddProsecutionCounsel addProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                              final AddProsecutionCounsel addProsecutionCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(addProsecutionCounsel)
                .executeSuccessfully();

        return addProsecutionCounsel;
    }

    public static RemoveProsecutionCounsel removeProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                    final RemoveProsecutionCounsel removeProsecutionCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeProsecutionCounsel)
                .executeSuccessfully();

        return removeProsecutionCounsel;
    }

    public static RemoveDefenceCounsel removeDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final RemoveDefenceCounsel removeDefenceCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeDefenceCounsel)
                .executeSuccessfully();

        return removeDefenceCounsel;
    }

    public static CaseDefendantDetails updateDefendants(final CaseDefendantDetails caseDefendantDetails) throws Exception {

        final String eventName = "public.progression.case-defendant-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final String payloadAsString = mapper.writeValueAsString(caseDefendantDetails.getDefendants().get(0));

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("defendant",
                                createObjectBuilder(jsonObject).build())
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return caseDefendantDetails;
    }


    public static UpdateOffencesForDefendantCommand updateOffences(final UpdateOffencesForDefendantCommand updateOffencesForDefendantCommand) throws Exception {

        final String eventName = "public.progression.defendant-offences-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final String jsonValueAsString = mapper.writeValueAsString(updateOffencesForDefendantCommand);

        final JsonObject jsonObject = mapper.readValue(jsonValueAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return updateOffencesForDefendantCommand;
    }

    public static HearingDetailsUpdateCommand updateHearing(final HearingDetailsUpdateCommand hearingDetailsUpdateCommand) throws Exception {
        final String eventName = "public.hearing-detail-changed";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(hearingDetailsUpdateCommand), JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return hearingDetailsUpdateCommand;
    }

    public static HearingVacatedTrialCleared rescheduleHearing(final HearingVacatedTrialCleared hearingVacatedTrialCleared) throws Exception {
        final String eventName = "public.listing.hearing-rescheduled";
        String eventPayloadString = " { \"hearingId\": \"" + hearingVacatedTrialCleared.getHearingId() + "\" }";
        final JsonObject jsonObject = new StringToJsonObjectConverter().convert(eventPayloadString);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());
        return hearingVacatedTrialCleared;
    }

    public static HearingVacatedTrialDetailsUpdateCommand updateHearingVacatedTrialDetail(final HearingVacatedTrialDetailsUpdateCommand hearingVacateTrialDetailsUpdateCommand) throws Exception {
        final String eventName = "public.listing.vacated-trial-updated";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();

        final JsonObject jsonObject = mapper.readValue(mapper.writeValueAsString(hearingVacateTrialDetailsUpdateCommand), JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                jsonObject,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

        return hearingVacateTrialDetailsUpdateCommand;
    }

    public static UpdateDefendantAttendanceCommand updateDefendantAttendance(final RequestSpecification requestSpec, final UpdateDefendantAttendanceCommand updateDefendantAttendanceCommand) {

        makeCommand(requestSpec, "hearing.update-defendant-attendance-on-hearing-day")
                .ofType("application/vnd.hearing.update-defendant-attendance-on-hearing-day+json")
                .withPayload(updateDefendantAttendanceCommand)
                .executeSuccessfully();

        return updateDefendantAttendanceCommand;
    }

    public static UpdateDefenceCounsel updateDefenceCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateDefenceCounsel updateDefenceCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-defence-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateDefenceCounselCommandTemplate)
                .executeSuccessfully();

        return updateDefenceCounselCommandTemplate;
    }

    public static UpdateProsecutionCounsel updateProsecutionCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateProsecutionCounsel updateProsecutionCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-prosecution-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateProsecutionCounselCommandTemplate)
                .executeSuccessfully();

        return updateProsecutionCounselCommandTemplate;
    }

    public static AddRespondentCounsel addRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                            final AddRespondentCounsel addRespondentCounsel) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.respondent-counsel-added", "hearing.event")
                .withFilter(convertStringTo(AddRespondentCounsel.class, isBean(AddRespondentCounsel.class)
                        .with(AddRespondentCounsel::getHearingId, Matchers.is(hearingId))
                        .with(AddRespondentCounsel::getRespondentCounsel, isBean(RespondentCounsel.class).with(RespondentCounsel::getId, is(addRespondentCounsel.getRespondentCounsel().getId())))
                ))

        ) {

            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.add-respondent-counsel+json")
                    .withArgs(hearingId)
                    .withPayload(addRespondentCounsel)
                    .executeSuccessfully();

            eventTopic.waitFor();

        }

        return addRespondentCounsel;
    }

    public static UpdateRespondentCounsel updateRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateRespondentCounsel updateRespondentCounselCommandTemplate) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.respondent-counsel-updated", "hearing.event")
                .withFilter(convertStringTo(UpdateRespondentCounsel.class, isBean(UpdateRespondentCounsel.class)
                        .with(UpdateRespondentCounsel::getHearingId, Matchers.is(hearingId))
                        .with(UpdateRespondentCounsel::getRespondentCounsel, isBean(RespondentCounsel.class).with(RespondentCounsel::getId, is(updateRespondentCounselCommandTemplate.getRespondentCounsel().getId())))
                ))

        ) {
            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.update-respondent-counsel+json")
                    .withArgs(hearingId)
                    .withPayload(updateRespondentCounselCommandTemplate)
                    .executeSuccessfully();

            eventTopic.waitFor();
        }

        return updateRespondentCounselCommandTemplate;
    }

    public static UpdateRespondentCounsel updateRespondentCounselAfterRemovingCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateRespondentCounsel updateRespondentCounselCommandTemplate) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.respondent-counsel-change-ignored", "hearing.event")
                .withFilter(convertStringTo(RespondentCounselChangeIgnored.class, isBean(RespondentCounselChangeIgnored.class)
                        .with(RespondentCounselChangeIgnored::getReason, Matchers.containsString(updateRespondentCounselCommandTemplate.getRespondentCounsel().getId().toString()))
                ))

        ) {
            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.update-respondent-counsel+json")
                    .withArgs(hearingId)
                    .withPayload(updateRespondentCounselCommandTemplate)
                    .executeSuccessfully();

            eventTopic.waitFor();
        }

        return updateRespondentCounselCommandTemplate;
    }

    public static RemoveRespondentCounsel removeRespondentCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                  final RemoveRespondentCounsel removeRespondentCounsel) {

        try (final Utilities.EventListener eventTopic = listenFor("hearing.respondent-counsel-removed", "hearing.event")
                .withFilter(convertStringTo(RemoveRespondentCounsel.class, isBean(RemoveRespondentCounsel.class)
                        .with(RemoveRespondentCounsel::getHearingId, Matchers.is(hearingId))
                ))

        ) {

            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.remove-respondent-counsel+json")
                    .withArgs(hearingId)
                    .withPayload(removeRespondentCounsel)
                    .executeSuccessfully();
            eventTopic.waitFor();
        }

        return removeRespondentCounsel;
    }

    public static AddApplicantCounsel addApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                          final AddApplicantCounsel addApplicantCounsel) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.add-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(addApplicantCounsel)
                .executeSuccessfully();

        return addApplicantCounsel;
    }

    public static UpdateApplicantCounsel updateApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId, final UpdateApplicantCounsel updateApplicantCounselCommandTemplate) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(updateApplicantCounselCommandTemplate)
                .executeSuccessfully();

        return updateApplicantCounselCommandTemplate;
    }

    public static RemoveApplicantCounsel removeApplicantCounsel(final RequestSpecification requestSpec, final UUID hearingId,
                                                                final RemoveApplicantCounsel removeApplicantCounsel) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-applicant-counsel+json")
                .withArgs(hearingId)
                .withPayload(removeApplicantCounsel)
                .executeSuccessfully();

        return removeApplicantCounsel;
    }

    public static void addDefendant(final Defendant defendant) throws Exception {

        final String eventName = "public.progression.defendants-added-to-hearing";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        final String payloadAsString = mapper.writeValueAsString(defendant);

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("foooo", "to test additional properties")
                        .add("defendants", Json.createArrayBuilder().add(createObjectBuilder(jsonObject).build()))
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void sendPublicApplicationChangedMessage(final CourtApplication courtApplication) throws Exception {

        final String eventName = "public.progression.court-application-updated";

        final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
        final String payloadAsString = mapper.writeValueAsString(courtApplication);

        final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                createObjectBuilder()
                        .add("courtApplication", createObjectBuilder(jsonObject).build())
                        .build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void addInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId,
                                                  final InterpreterIntermediary interpreterIntermediary) {
        try {

            final JsonObject addInterpreterIntermediary = createObjectBuilder().add("interpreterIntermediary", objectToJsonObject(interpreterIntermediary)).build();

            makeCommand(requestSpec, "hearing.update-hearing")
                    .ofType("application/vnd.hearing.add-interpreter-intermediary+json")
                    .withArgs(hearingId)
                    .withPayload(addInterpreterIntermediary.toString())
                    .executeSuccessfully();

        } catch (final JsonProcessingException exception) {
            System.out.println(exception);
        }

    }

    public static RemoveInterpreterIntermediary removeInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId,
                                                                              final RemoveInterpreterIntermediary removeInterpreterIntermediary) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.remove-interpreter-intermediary+json")
                .withArgs(hearingId)
                .withPayload(removeInterpreterIntermediary)
                .executeSuccessfully();

        return removeInterpreterIntermediary;
    }

    public static UpdateInterpreterIntermediary updateInterpreterIntermediary(final RequestSpecification requestSpec, final UUID hearingId, final UpdateInterpreterIntermediary updateInterpreterIntermediary) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.update-interpreter-intermediary+json")
                .withArgs(hearingId)
                .withPayload(updateInterpreterIntermediary)
                .executeSuccessfully();

        return updateInterpreterIntermediary;
    }

    public static TrialType setTrialType(final RequestSpecification requestSpec, final UUID hearingId,
                                         final TrialType trialType, final boolean isVacated) {

        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.set-trial-type+json")
                .withArgs(hearingId)
                .withPayload(trialType)
                .executeSuccessfully();

        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, Matchers.is(hearingId))
                        .with(Hearing::getIsVacatedTrial, is(isVacated)
                        ))
        );

        return trialType;
    }

    public static TrialType setTrialType(final RequestSpecification requestSpec, final UUID hearingId,
                                         final TrialType trialType) {
        makeCommand(requestSpec, "hearing.update-hearing")
                .ofType("application/vnd.hearing.set-trial-type+json")
                .withArgs(hearingId)
                .withPayload(trialType)
                .executeSuccessfully();

        return trialType;
    }

    public static void updateCaseMarkers(final UUID prosecutionCaseId, final UUID hearingId, final List<Marker> markers) throws Exception {

        final String eventName = "public.progression.case-markers-updated";
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (final Marker marker : markers) {
            final ObjectMapper mapper = new ObjectMapperProducer().objectMapper();
            final String payloadAsString = mapper.writeValueAsString(marker);
            final JsonObject jsonObject = mapper.readValue(payloadAsString, JsonObject.class);
            arrayBuilder.add(createObjectBuilder(jsonObject).build());
        }
        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingId", hearingId.toString())
                .add("caseMarkers", arrayBuilder)
                .build();
        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                payload,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void removeCaseFromGroupCases(final UUID groupId, final UUID masterCaseId, final ProsecutionCase removedCase, final ProsecutionCase newGroupMaster) throws Exception {
        final String eventName = "public.progression.case-removed-from-group-cases";
        final JsonObjectBuilder jsonObjectBuilder = createObjectBuilder()
                .add("groupId", groupId.toString())
                .add("masterCaseId", masterCaseId.toString())
                .add("removedCase", objectToJsonObject(removedCase));

        if (nonNull(newGroupMaster)) {
            jsonObjectBuilder.add("newGroupMaster", objectToJsonObject(newGroupMaster));
        }

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                jsonObjectBuilder.build(),
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());
    }

    public static void changeNextHearingDate(final UUID seedingHearingId, final UUID hearingId, final ZonedDateTime nextHearingStartDate) throws Exception {

        final String eventName = "public.events.listing.next-hearing-day-changed";

        final JsonObject payload = createObjectBuilder()
                .add("seedingHearingId", seedingHearingId.toString())
                .add("hearingId", hearingId.toString())
                .add("hearingStartDate", nextHearingStartDate.format(DATE_TIME_FORMATTER))
                .build();

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                payload,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void updateProsecutor(final UUID prosecutionCaseId, final List<UUID> hearingIds, final CpsProsecutorUpdated cpsProsecutorUpdated) throws JsonProcessingException {

        final String eventName = "public.progression.events.cps-prosecutor-updated";
        final JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        for (final UUID hearingId : hearingIds) {
            arrayBuilder.add(hearingId.toString());
        }
        final JsonObject payload = createObjectBuilder()
                .add("prosecutionCaseId", prosecutionCaseId.toString())
                .add("hearingIds", arrayBuilder.build())
                .add("prosecutionAuthorityId", cpsProsecutorUpdated.getProsecutionAuthorityId().toString())
                .add("prosecutionAuthorityReference", cpsProsecutorUpdated.getProsecutionAuthorityReference())
                .add("prosecutionAuthorityCode", cpsProsecutorUpdated.getProsecutionAuthorityCode())
                .add("prosecutionAuthorityName", cpsProsecutorUpdated.getProsecutionAuthorityName())
                .add("caseURN", cpsProsecutorUpdated.getCaseURN())
                .add("address", Utilities.JsonUtil.objectToJsonObject(cpsProsecutorUpdated.getAddress()))
                .build();

        sendMessage(
                getPublicTopicInstance().createProducer(),
                eventName,
                payload,
                metadataWithRandomUUID(eventName).withUserId(randomUUID().toString()).build());

    }

    public static void bookHearingSlots(final RequestSpecification requestSpec, final UUID hearingId, final List<ProvisionalHearingSlotInfo> hearingSlots) throws Exception {

        callCommand(requestSpec, hearingId, hearingSlots, "hearing.book-provisional-hearing-slots.json");

    }

    private static void callCommand(final RequestSpecification requestSpec, final UUID hearingId, final List<ProvisionalHearingSlotInfo> hearingSlots, final String payload)
            throws IOException {
        final String commandPayloadString = getStringFromResource(payload)
                .replace("UUID1", hearingSlots.get(0).getCourtScheduleId().toString())
                .replace("UUID2", hearingSlots.get(1).getCourtScheduleId().toString())
                .replace("HEARING_START_TIME1", hearingSlots.get(0).getHearingStartTime().format(DATE_TIME_FORMATTER))
                .replace("HEARING_START_TIME2", hearingSlots.get(0).getHearingStartTime().format(DATE_TIME_FORMATTER));

        makeCommand(requestSpec, "hearing.book-provisional-hearing-slots")
                .ofType("application/vnd.hearing.book-provisional-hearing-slots+json")
                .withArgs(hearingId)
                .withPayload(commandPayloadString)
                .executeSuccessfully();
    }

    public static void correctHearingDaysWithoutCourtCentre(final RequestSpecification requestSpec, final UUID hearingId, final List<HearingDay> hearingDays) {

        final JsonArrayBuilder hearingDayArrayBuilder = Json.createArrayBuilder();
        hearingDays.forEach(d -> {
            try {
                hearingDayArrayBuilder.add(objectToJsonObject(d));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

        final JsonObject commandPayload = Json.createObjectBuilder().add("id", hearingId.toString())
                .add("hearingDays", hearingDayArrayBuilder).build();


        makeCommand(requestSpec, "hearing.correct-hearing-days-without-court-centre")
                .ofType("application/vnd.hearing.correct-hearing-days-without-court-centre+json")
                .withArgs(hearingId)
                .withPayload(commandPayload.toString())
                .withCppUserId(USER_ID_VALUE_AS_ADMIN)
                .executeSuccessfully();

    }

    private static LogEventCommand getLogEventCommand(final UUID hearingEventId, final Consumer<LogEventCommand.Builder> consumer, final InitiateHearingCommand initiateHearingCommand, final UUID hearingEventDefinitionId, final boolean alterable, final UUID defenceCounselId, final ZonedDateTime eventTime, final String recordedLabel, final String note) {
        final LogEventCommand logEvent = with(
                LogEventCommand.builder()
                        .withHearingEventId(hearingEventId)
                        .withHearingEventDefinitionId(hearingEventDefinitionId)
                        .withHearingId(initiateHearingCommand.getHearing().getId())
                        .withEventTime(eventTime)
                        .withLastModifiedTime(PAST_ZONED_DATE_TIME.next().withZoneSameLocal(ZoneId.of("UTC")))
                        .withRecordedLabel(recordedLabel)
                        .withDefenceCounselId(defenceCounselId)
                        .withAlterable(alterable)
                        .withNote(note)
                , consumer).build();
        return logEvent;
    }

    public static LogEventCommand shouldNotlogPIEvent(final UUID hearingEventId,
                                                      final RequestSpecification requestSpec,
                                                      final Consumer<LogEventCommand.Builder> consumer,
                                                      final InitiateHearingCommand initiateHearingCommand,
                                                      final UUID hearingEventDefinitionId,
                                                      final boolean alterable,
                                                      final UUID defenceCounselId,
                                                      final ZonedDateTime eventTime,
                                                      final String recordedLabel, String note) {
        stubOrganisationUnit(initiateHearingCommand.getHearing().getCourtCentre().getId().toString());

        final LogEventCommand logEvent = getLogEventCommand(hearingEventId, consumer, initiateHearingCommand, hearingEventDefinitionId, alterable, defenceCounselId, eventTime, recordedLabel, note);

        final String reference = getReference(initiateHearingCommand.getHearing());

        try (final Utilities.EventListener publicEventTopic = listenFor("public.hearing.event-logged")
                .withFilter(convertStringTo(PublicHearingEventLogged.class, isBean(PublicHearingEventLogged.class)
                        .with(PublicHearingEventLogged::getHearingEvent, isBean(uk.gov.moj.cpp.hearing.eventlog.HearingEvent.class)
                                .with(HearingEvent::getHearingEventId, is(logEvent.getHearingEventId()))
                                .with(HearingEvent::getRecordedLabel, is(logEvent.getRecordedLabel()))
                                .with(HearingEvent::getLastHearingEventId, is(nullValue()))
                                .with(HearingEvent::getEventTime, is(logEvent.getEventTime().withZoneSameInstant(ZoneId.of("UTC"))))
                                .with(HearingEvent::getLastModifiedTime, is(logEvent.getLastModifiedTime().withZoneSameInstant(ZoneId.of("UTC"))))

                        )
                        .with(PublicHearingEventLogged::getCase, isBean(uk.gov.moj.cpp.hearing.eventlog.Case.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Case::getCaseUrn, is(reference))
                        )
                        .with(PublicHearingEventLogged::getHearingEventDefinition, isBean(HearingEventDefinition.class)
                                .with(HearingEventDefinition::getHearingEventDefinitionId, is(logEvent.getHearingEventDefinitionId()))
                                .with(HearingEventDefinition::isPriority, is(!alterable))
                        )
                        .with(PublicHearingEventLogged::getHearing, isBean(uk.gov.moj.cpp.hearing.eventlog.Hearing.class)
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getCourtCentreId, is(initiateHearingCommand.getHearing().getCourtCentre().getId()))
                                        .with(CourtCentre::getCourtCentreName, is(initiateHearingCommand.getHearing().getCourtCentre().getName()))
                                        .with(CourtCentre::getCourtRoomId, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomId()))
                                        .with(CourtCentre::getCourtRoomName, is(initiateHearingCommand.getHearing().getCourtCentre().getRoomName()))
                                )
                                .with(uk.gov.moj.cpp.hearing.eventlog.Hearing::getHearingType, is(initiateHearingCommand.getHearing().getType().getDescription()))
                        )
                ))) {

            if (JurisdictionType.CROWN.equals(initiateHearingCommand.getHearing().getJurisdictionType())) {
                setupNoProsecutionCaseByHearingId(initiateHearingCommand.getHearing().getId(), hearingEventId);
            }
            postHearingLogEventCommand(requestSpec, initiateHearingCommand, logEvent);

            publicEventTopic.waitFor();
        }
        return logEvent;
    }

    public static LogEventCommand logEvent(final RequestSpecification requestSpec,
                                           final Consumer<LogEventCommand.Builder> consumer,
                                           final InitiateHearingCommand initiateHearingCommand,
                                           final UUID hearingEventDefinitionId,
                                           final boolean alterable,
                                           final UUID defenceCounselId,
                                           final ZonedDateTime eventTime, String recordedLabel, String note) {
        return logEvent(randomUUID(), requestSpec, consumer, initiateHearingCommand, hearingEventDefinitionId, alterable, defenceCounselId, eventTime, recordedLabel, note);
    }

    public static ProsecutionCounsel createFirstProsecutionCounsel(final CommandHelpers.InitiateHearingCommandHelper hearingOne) {

        final AddProsecutionCounsel firstProsecutionCounselCommand = addProsecutionCounsel(getRequestSpec(), hearingOne.getHearingId(),
                addProsecutionCounselCommandTemplate(hearingOne.getHearingId())
        );

        ProsecutionCounsel firstProsecutionCounsel = firstProsecutionCounselCommand.getProsecutionCounsel();
        pollForHearing(hearingOne.getHearingId().toString(),
                withJsonPath("$.hearing.prosecutionCounsels.[0].status", is(firstProsecutionCounsel.getStatus())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].firstName", is(firstProsecutionCounsel.getFirstName())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].lastName", is(firstProsecutionCounsel.getLastName())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].title", is(firstProsecutionCounsel.getTitle())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].middleName", is(firstProsecutionCounsel.getMiddleName())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].attendanceDays.[0]", is(firstProsecutionCounsel.getAttendanceDays().get(0).toString())),
                withJsonPath("$.hearing.prosecutionCounsels.[0].prosecutionCases.[0]", is(firstProsecutionCounsel.getProsecutionCases().get(0).toString()))
        );
        return firstProsecutionCounsel;

    }
}
