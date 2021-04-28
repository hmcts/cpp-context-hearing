package uk.gov.moj.cpp.hearing.it;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.of;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static javax.json.Json.createObjectBuilder;
import static javax.ws.rs.core.Response.Status.OK;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.Prompt.prompt;
import static uk.gov.justice.services.test.utils.core.http.RequestParamsBuilder.requestParams;
import static uk.gov.justice.services.test.utils.core.matchers.ResponsePayloadMatcher.payload;
import static uk.gov.justice.services.test.utils.core.matchers.ResponseStatusMatcher.status;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.constants.ApplicationOutcome.GRANTED;
import static uk.gov.moj.cpp.hearing.constants.ApplicationOutcome.WITHDRAWN;
import static uk.gov.moj.cpp.hearing.constants.ApplicationType.APPEAL_AGAINST_SENTENCE;
import static uk.gov.moj.cpp.hearing.constants.ApplicationType.APPEARANCE_TO_MAKE_STATUTORY_DECLARATION;
import static uk.gov.moj.cpp.hearing.constants.EmailAmendmentTitles.AMEND_RESULT;
import static uk.gov.moj.cpp.hearing.constants.EmailAmendmentTitles.APPEAL_WITHDRAWN;
import static uk.gov.moj.cpp.hearing.constants.EmailAmendmentTitles.WRITE_OFF_ONE_DAY_DEEMED_SERVED;
import static uk.gov.moj.cpp.hearing.constants.EmailStatus.NOT_SENT;
import static uk.gov.moj.cpp.hearing.constants.EmailStatus.SENT;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows.allNows;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition.now;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement.resultDefinitions;
import static uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions.allResultDefinitions;
import static uk.gov.moj.cpp.hearing.it.ProsecutionCounselIT.createFirstProsecutionCounsel;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResults;
import static uk.gov.moj.cpp.hearing.it.Utilities.JsonUtil.objectToJsonObject;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllNowsMetaData;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllResultDefinitions;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.poll;
import static uk.gov.moj.cpp.hearing.utils.ResultDefinitionUtil.getCategoryForResultDefinition;
import static uk.gov.moj.cpp.hearing.utils.StagingEnforcementStub.requestIssuedForStagingEnforcementForNowsId;
import static uk.gov.moj.cpp.hearing.utils.SystemIdMapperStub.findNowsIdForGivenHearingIdFromSystemMapper;
import static uk.gov.moj.cpp.hearing.utils.SystemIdMapperStub.stubMappingForNowsRequestId;

import uk.gov.justice.core.courts.AllocationDecision;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.http.HeaderConstants;
import uk.gov.moj.cpp.hearing.command.initiate.ExtendHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.constants.ApplicationOutcome;
import uk.gov.moj.cpp.hearing.constants.ApplicationType;
import uk.gov.moj.cpp.hearing.constants.EmailAmendmentTitles;
import uk.gov.moj.cpp.hearing.constants.EmailStatus;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllResultDefinitionsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.HearingFactory;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("The NCES feature will be upgraded after NOWs feature. Due to this reason this IT Test has been ignored.")
public class NCESJourneyIT extends AbstractIT {

    public static final String PUBLIC_EVENT_PROGRESSION_HEARING_EXTENDED = "public.progression.events.hearing-extended";
    private static final UUID NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID = fromString("66cd749a-1d51-11e8-accf-0ed5f89f718b");
    private static final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");
    private static final UUID RD_FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");
    private static final UUID RD_FIDICI = fromString("de946ddc-ad77-44b1-8480-8bbc251cdcfb");
    private static final String PUBLIC_EVENT_STAGINGENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT = "public.stagingenforcement.enforce-financial-imposition-acknowledgement";
    private static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";
    private final String FIRST_ACK_ACCOUNT_NUMBER = randomNumeric(9);
    private final String SECOND_ACK_ACCOUNT_NUMBER = randomNumeric(9);
    private LocalDate orderedDate;
    private ZonedDateTime testStartTime;

    private InitiateHearingCommandHelper getInitiateHearingCommandHelper() {
        testStartTime = ZonedDateTime.now();
        orderedDate = PAST_LOCAL_DATE.next();
        InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate(), false, false, true, false));
        return hearingCommandHelper;
    }

    @Test
    public void shouldSendToGOBButNotSendEmailWhenResultsSharedWithFinancialImpositionWithoutDeemedServedResults() {
        InitiateHearingCommandHelper hearingCommandHelper = getInitiateHearingCommandHelper();
        SaveDraftResultCommand draftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate, LocalDate.now());

        // extend hearing by creating an application against a defendant
        final Defendant firstDefendant = hearingCommandHelper.getFirstCase().getDefendants().get(0);
        UUID defendantId = firstDefendant.getId();
        UUID hearingId = hearingCommandHelper.getHearingId();
        initiateHearingAndShareResults(orderedDate, setupNowsReferenceData(orderedDate, false), hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, FIRST_ACK_ACCOUNT_NUMBER, testStartTime, hearingId);
        verifyEmailNotificationEvent(NOT_SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString()))
        )));
    }

    @Test
    public void shouldSendToGOBAndEmailWhenResultsSharedWithFinancialImpositionAndDeemedServedResults() {
        InitiateHearingCommandHelper hearingCommandHelper = getInitiateHearingCommandHelper();
        SaveDraftResultCommand draftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate, LocalDate.now());

        // extend hearing by creating an application against a defendant
        final Defendant firstDefendant = hearingCommandHelper.getFirstCase().getDefendants().get(0);
        UUID defendantId = firstDefendant.getId();
        UUID hearingId = hearingCommandHelper.getHearingId();
        initiateHearingAndShareResults(orderedDate, setupNowsReferenceData(orderedDate, true), hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, FIRST_ACK_ACCOUNT_NUMBER, testStartTime, hearingId);
        verifyEmailNotificationEvent(SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString())),
                withJsonPath("$.documentContent.gobAccountNumber", is(FIRST_ACK_ACCOUNT_NUMBER)),
                withJsonPath("$.documentContent.amendmentType", is(WRITE_OFF_ONE_DAY_DEEMED_SERVED))
        )));
    }

    @Test
    public void shouldSendToGOBAndNotEmailWhenResultsResharedWithFinancialImpositionAndWithDeemedServedResults() {
        InitiateHearingCommandHelper hearingCommandHelper = getInitiateHearingCommandHelper();
        SaveDraftResultCommand draftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate, LocalDate.now());

        // extend hearing by creating an application against a defendant
        final Defendant firstDefendant = hearingCommandHelper.getFirstCase().getDefendants().get(0);
        UUID defendantId = firstDefendant.getId();
        UUID hearingId = hearingCommandHelper.getHearingId();
        initiateHearingAndShareResults(orderedDate, setupNowsReferenceData(orderedDate, true), hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, FIRST_ACK_ACCOUNT_NUMBER, testStartTime, hearingId);
        verifyEmailNotificationEvent(SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString())),
                withJsonPath("$.documentContent.gobAccountNumber", is(FIRST_ACK_ACCOUNT_NUMBER)),
                withJsonPath("$.documentContent.amendmentType", is(WRITE_OFF_ONE_DAY_DEEMED_SERVED))
        )));

        final ZonedDateTime timeOfSubsequentSharing = ZonedDateTime.now();
        shareOffenceResults(hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, SECOND_ACK_ACCOUNT_NUMBER, timeOfSubsequentSharing, hearingId);
        verifyEmailNotificationEvent(NOT_SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString()))
        )));
    }

    @Test
    public void shouldSendToGOBAndEmailWhenResultsAreResharedButWithoutDeemedServedResults() {
        InitiateHearingCommandHelper hearingCommandHelper = getInitiateHearingCommandHelper();
        SaveDraftResultCommand draftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate, LocalDate.now());

        // extend hearing by creating an application against a defendant
        final Defendant firstDefendant = hearingCommandHelper.getFirstCase().getDefendants().get(0);
        UUID defendantId = firstDefendant.getId();
        UUID hearingId = hearingCommandHelper.getHearingId();
        initiateHearingAndShareResults(orderedDate, setupNowsReferenceData(orderedDate, false), hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, FIRST_ACK_ACCOUNT_NUMBER, testStartTime, hearingId);
        verifyEmailNotificationEvent(NOT_SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString()))
        )));

        final ZonedDateTime timeOfSubsequentSharing = ZonedDateTime.now();
        shareOffenceResults(hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, SECOND_ACK_ACCOUNT_NUMBER, timeOfSubsequentSharing, hearingId);
        verifyEmailNotificationEvent(SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString())),
                withJsonPath("$.documentContent.gobAccountNumber", is(SECOND_ACK_ACCOUNT_NUMBER)),
                withJsonPath("$.documentContent.amendmentType", is(AMEND_RESULT))
        )));

    }

    @Test
    public void shouldSendEmailWhenResultsSharedWithFinancialImpositionAndDeemedServedResultsAndApplicationWithdrawn() throws JsonProcessingException {
        InitiateHearingCommandHelper hearingCommandHelper = getInitiateHearingCommandHelper();
        SaveDraftResultCommand draftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate, LocalDate.now());

        // extend hearing by creating an application against a defendant
        final Defendant firstDefendant = hearingCommandHelper.getFirstCase().getDefendants().get(0);
        UUID defendantId = firstDefendant.getId();
        UUID caseId = hearingCommandHelper.getFirstCase().getId();
        UUID hearingId = hearingCommandHelper.getHearingId();
        initiateHearingAndShareResults(orderedDate, setupNowsReferenceData(orderedDate, true), hearingCommandHelper, draftResultCommand);
        sendStagingEnforcementAcknowledgment(2, FIRST_ACK_ACCOUNT_NUMBER, testStartTime, hearingId);
        verifyEmailNotificationEvent(SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString())),
                withJsonPath("$.documentContent.gobAccountNumber", is(FIRST_ACK_ACCOUNT_NUMBER)),
                withJsonPath("$.documentContent.amendmentType", is(WRITE_OFF_ONE_DAY_DEEMED_SERVED))
        )));

        final UUID applicationId = randomUUID();

        extendHearingWithApplicationLinkedToDefendant(defendantId, caseId, applicationId, hearingId, APPEAL_AGAINST_SENTENCE, of(WITHDRAWN), hearingCommandHelper);

        //saveDraftApplicationOutcome(hearingId, applicationId, WITHDRAWN, hearingCommandHelper);

        shareApplicationResults(applicationId, hearingId, WITHDRAWN, hearingCommandHelper, draftResultCommand);

        verifyEmailNotificationEvent(SENT, isJson(allOf(
                withJsonPath("$.hearingId", is(hearingId.toString())),
                withJsonPath("$.defendantId", is(defendantId.toString())),
                withJsonPath("$.documentContent.amendmentType", is(APPEAL_WITHDRAWN))
        )));

    }

    private void extendHearingWithApplicationLinkedToDefendant(UUID defendantId, UUID caseId, UUID applicationId, UUID hearingId, ApplicationType applicationType, Optional<ApplicationOutcome> optionalApplicationOutcome, InitiateHearingCommandHelper hearingCommandHelper) throws JsonProcessingException {
        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getCourtApplications, nullValue())
                )
        );

        ExtendHearingCommand extendHearingCommand = new ExtendHearingCommand();
        extendHearingCommand.setHearingId(hearingId);
        final CourtApplication newCourtApplication = (new HearingFactory()).courtApplicationWithDefendantParty(defendantId, caseId, applicationType.getId()).build();
        newCourtApplication.setId(applicationId);
        //TODO fix applications
//        newCourtApplication.getApplicant().getMasterDefendant().setOffences(hearingCommandHelper.getFirstDefendantForFirstCase().getOffences());
        extendHearingCommand.setCourtApplication(newCourtApplication);

        JsonObject commandJson = objectToJsonObject(extendHearingCommand);

        sendMessage(getPublicTopicInstance().createProducer(),
                PUBLIC_EVENT_PROGRESSION_HEARING_EXTENDED,
                commandJson,
                metadataOf(randomUUID(), PUBLIC_EVENT_PROGRESSION_HEARING_EXTENDED)
                        .withUserId(randomUUID().toString())
                        .build()
        );

        getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getCourtApplications, notNullValue())
                        .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                .withValue(CourtApplication::getId, applicationId)
                        )))
        );
    }

    private void sendStagingEnforcementAcknowledgment(final int numberOfRequests, final String accountNumber, final ZonedDateTime requestsAfter, final UUID hearingId) {
        //check the required number of system mapper requests
        await().timeout(DEFAULT_POLL_TIMEOUT_IN_SEC, SECONDS).until(() -> {
            final int size = findNowsIdForGivenHearingIdFromSystemMapper(hearingId, requestsAfter).size();
            return size == numberOfRequests;
        });

        List<String> requestIdsFromSystemMapper = findNowsIdForGivenHearingIdFromSystemMapper(hearingId, requestsAfter);

        //check the required number of financial requests were issued to staging enforcement for the above request ids
        await().until(() -> requestIssuedForStagingEnforcementForNowsId(requestIdsFromSystemMapper));

        // issue acknowledgement for the 1st nows request from staging enforcement
        final String nowId1 = requestIdsFromSystemMapper.get(0);
        stubMappingForNowsRequestId(nowId1, hearingId.toString());


        final JsonObject stagingEnforcementAckPayload = createObjectBuilder().add("originator", "courts")
                .add("requestId", nowId1)
                .add("exportStatus", "ENFORCEMENT_ACKNOWLEDGED")
                .add("updated", "2019-12-01T10:00:00Z")
                .add("acknowledgement", createObjectBuilder().add("accountNumber", accountNumber))
                .build();

        sendMessage(getPublicTopicInstance().createProducer(),
                PUBLIC_EVENT_STAGINGENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT,
                stagingEnforcementAckPayload,
                metadataOf(randomUUID(), PUBLIC_EVENT_STAGINGENFORCEMENT_ENFORCE_FINANCIAL_IMPOSITION_ACKNOWLEDGEMENT)
                        .withUserId(getLoggedInAdminUser().toString()).build());


    }

    private void verifyEmailNotificationEvent(final EmailStatus emailStatus, final Matcher matcher) {
        // assert that nces notification was sent
        try (final EventListener ncesNotificationRequestedListener = listenFor("public.hearing.event.nces-notification-requested")
                .withFilter(matcher)) {

            if (SENT == emailStatus) {
                ncesNotificationRequestedListener.waitFor();
            } else {
                ncesNotificationRequestedListener.expectNone();
            }
        }
    }

    private void initiateHearingAndShareResults(final LocalDate orderedDate, final AllNowsReferenceDataHelper allNows, InitiateHearingCommandHelper hearingCommandHelper, SaveDraftResultCommand draftResultCommand) {
        //setup reference data for ordered date
        setupNowsReferenceData(orderedDate, allNows.it());

        hearingCommandHelper.getHearing().getProsecutionCases().forEach(prosecutionCase -> stubLjaDetails(hearingCommandHelper.getHearing().getCourtCentre(), prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId()));

        CourtCentre courtCentre = hearingCommandHelper.getHearing().getCourtCentre();
        stubGetReferenceDataCourtRooms(courtCentre, hearingCommandHelper.getHearing().getHearingLanguage(), ouId3, ouId4);

        createFirstProsecutionCounsel(hearingCommandHelper);

        getHearingPollForMatch(hearingCommandHelper.getHearingId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingCommandHelper.getHearingId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(hearingCommandHelper.getFirstOffenceForFirstDefendantForFirstCase().getId()))
                                                .with(Offence::getAllocationDecision, isBean(AllocationDecision.class)
                                                        .with(AllocationDecision::getOffenceId, is(hearingCommandHelper.getFirstOffenceForFirstDefendantForFirstCase().getId())))
                                        ))))))));

        // ensuring no result lines are present in draft sharing
        draftResultCommand.getTarget().setResultLines(null);

        testSaveDraftResult(draftResultCommand, asList(draftResultCommand.getTarget()));

        setResultLinesBeforeSharing(allNows, draftResultCommand);

        shareOffenceResults(hearingCommandHelper, draftResultCommand);
    }

    private void setResultLinesBeforeSharing(final AllNowsReferenceDataHelper allNows, SaveDraftResultCommand draftResultCommand) {
        final UUID firstNowNonMandatoryResultDefinitionId = allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> !rd.getMandatory())
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
        final UUID secondNowPrimaryResultDefinitionId = allNows.it().getNows().get(1).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory())
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));

        //need to get out prompt label here or put in to create draft label
        final AllResultDefinitionsReferenceDataHelper resultDefHelper = setupResultDefinitionsReferenceData(orderedDate, asList(firstNowNonMandatoryResultDefinitionId, secondNowPrimaryResultDefinitionId));
        final Prompt firstNowNonMandatoryPrompt = getPromptForGivenResultDefinitionId(firstNowNonMandatoryResultDefinitionId, resultDefHelper);
        final Prompt secondNowPrimaryPrompt = getPromptForGivenResultDefinitionId(secondNowPrimaryResultDefinitionId, resultDefHelper);

        draftResultCommand.getTarget().setResultLines(asList(
                standardResultLineTemplate(randomUUID(), firstNowNonMandatoryResultDefinitionId, orderedDate).withPrompts(
                        singletonList(getCoreCourtsPrompt(firstNowNonMandatoryPrompt, "val0", "wval0", "fixedList0"))
                ).build(),
                standardResultLineTemplate(randomUUID(), secondNowPrimaryResultDefinitionId, orderedDate).withPrompts(
                        singletonList(getCoreCourtsPrompt(secondNowPrimaryPrompt, "val1", "wval1", "fixedList1"))
                ).build()
        ));
    }

    private void shareOffenceResults(InitiateHearingCommandHelper hearingCommandHelper, SaveDraftResultCommand draftResultCommand) {
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers()
                .withFirstName("Siouxsie").withLastName("Sioux")
                .withUserId(randomUUID()).build();

        final Hearing hearing = hearingCommandHelper.getHearing();

        try (final EventListener publicEventHearingResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingCommandHelper.getHearingId()))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {

            shareResults(getRequestSpec(), hearingCommandHelper.getHearingId(), with(
                    basicShareResultsCommandTemplate(),
                    command -> command.setCourtClerk(courtClerk)
            ), asList(draftResultCommand.getTarget()));

            publicEventHearingResulted.waitFor();

            poll(requestParams(getURL("hearing.get.hearing", hearingCommandHelper.getHearingId()), "application/vnd.hearing.get.hearing+json")
                    .withHeader(HeaderConstants.USER_ID, AbstractIT.getLoggedInUser()).build())
                    .until(status().is(OK),
                            print(),
                            payload().isJson(allOf(
                                    withJsonPath("$.hearing.hasSharedResults", CoreMatchers.is(true))

                            )));

        }

    }

    private void shareApplicationResults(final UUID applicationId, final UUID hearingId, final ApplicationOutcome outcome, InitiateHearingCommandHelper hearingCommandHelper, SaveDraftResultCommand draftResultCommand) {

        final Hearing hearing = hearingCommandHelper.getHearing();

        try (final EventListener publicEventHearingResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearingId))
                                .with(Hearing::getCourtApplications, hasItem(isBean(CourtApplication.class)
                                        .with(CourtApplication::getId, is(applicationId))
                                )))))) {

            final Target target = draftResultCommand.getTarget();
            target.setApplicationId(applicationId);
            target.setOffenceId(null);
            shareResults(getRequestSpec(), hearingId,
                    basicShareResultsCommandTemplate()
                    , asList(target));

            publicEventHearingResulted.waitFor();
        }
    }

    private uk.gov.justice.core.courts.Prompt getCoreCourtsPrompt(final Prompt referenceDataPrompt, final String value, final String welshValue, final String fixedListCode) {
        return prompt().withId(referenceDataPrompt.getId())
                .withValue(value)
                .withWelshValue(welshValue)
                .withFixedListCode(fixedListCode)
                .withLabel(referenceDataPrompt.getLabel())
                .build();
    }

    private Prompt getPromptForGivenResultDefinitionId(final UUID firstNowNonMandatoryResultDefinitionId, final AllResultDefinitionsReferenceDataHelper resultDefHelper) {
        return resultDefHelper.it().getResultDefinitions().stream()
                .filter(rd -> firstNowNonMandatoryResultDefinitionId.equals(rd.getId())).findFirst().orElse(null)
                .getPrompts().get(0);
    }

    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand, final List<Target> previousTargets) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final Target target = saveDraftResultCommand.getTarget();
        final BeanMatcher beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(target.getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(target.getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(target.getDefendantId()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(target.getOffenceId()));

        final String expectedMetaDataContextUser = getLoggedInUser().toString();
        try (final EventListener publicEventResulted = listenFor(PUBLIC_HEARING_DRAFT_RESULT_SAVED)
                .withFilter(beanMatcher, PUBLIC_HEARING_DRAFT_RESULT_SAVED, expectedMetaDataContextUser)) {

            makeCommand(getRequestSpec(), "hearing.save-draft-result")
                    .ofType("application/vnd.hearing.save-draft-result+json")
                    .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                    .withPayload(saveDraftResultCommand.getTarget())
                    .executeSuccessfully();

            publicEventResulted.waitFor();
        }
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final boolean hasDeemedServedResults) {
        final NowResultDefinitionRequirement financialResultDefinition = resultDefinitions()
                .setId(RD_FINE)
                .setMandatory(true)
                .setPrimary(true);

        final NowResultDefinitionRequirement deemedServedResultDefinition = resultDefinitions()
                .setId(RD_FIDICI)
                .setMandatory(false)
                .setPrimary(false);

        final NowResultDefinitionRequirement nonDeemedServedResultDefinition = resultDefinitions()
                .setId(randomUUID()) // non deemed
                .setMandatory(false)
                .setPrimary(false);

        final List<NowResultDefinitionRequirement> resultDefinitions = hasDeemedServedResults ?
                asList(financialResultDefinition, deemedServedResultDefinition) :
                asList(financialResultDefinition, nonDeemedServedResultDefinition);

        AllNows allnows = allNows()
                .setNows(asList(now()
                                .setId(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID)
                                .setResultDefinitions(resultDefinitions)
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        now()
                                .setId(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID)
                                .setResultDefinitions(asList(financialResultDefinition))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false)
                ));

        return setupNowsReferenceData(referenceDate, allnows);
    }

    private AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final AllNowsReferenceDataHelper allNows = h(data);
        stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

    private AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, List<UUID> resultDefinitionIds) {
        final String LISTING_OFFICER_USERGROUP = "Listing Officer";

        final AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(allResultDefinitions()
                .setResultDefinitions(
                        resultDefinitionIds.stream().map(
                                resultDefinitionId ->
                                        ResultDefinition.resultDefinition()
                                                .setId(resultDefinitionId)
                                                .setRank(1)
                                                .setIsAvailableForCourtExtract(true)
                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                .setFinancial("Y")
                                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                                .setPrompts(singletonList(Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USERGROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                        ).collect(Collectors.toList())
                ));

        stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }
}
