package uk.gov.moj.cpp.hearing.it;

import static java.time.ZonedDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static javax.json.Json.createArrayBuilder;
import static javax.json.Json.createObjectBuilder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.ApprovalType.APPROVAL;
import static uk.gov.justice.core.courts.ApprovalType.CHANGE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.changeCourtRoomsStubWithAdding;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllNowsMetaData;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.ApprovalRequest;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.RequestApprovalCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ValidateResultAmendmentsCommand;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.AllNows;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowDefinition;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.NowResultDefinitionRequirement;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("unchecked")
public class AddRequestApprovalIT extends AbstractIT {
    private static final LocalDate orderedDate = PAST_LOCAL_DATE.next();
    private static final UUID NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID = fromString("66cd749a-1d51-11e8-accf-0ed5f89f718b");
    private static final UUID ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID = fromString("10115268-8efc-49fe-b8e8-feee216a03da");
    private static final UUID RD_FINE = fromString("969f150c-cd05-46b0-9dd9-30891efcc766");
    private static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";

    @Before
    public void setUp() {
        setUpPerTest();
    }

    @Test
    public void shouldPostApprovalRequests() throws Exception {
        final UUID userId = getLoggedInUser();
        final ZonedDateTime requestApprovalTime = now();
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final HearingDay hearingDay = initiateHearingCommand.getHearing().getHearingDays().get(0);
        hearingDay.setSittingDay(now().plusDays(1));

        final CommandHelpers.InitiateHearingCommandHelper hearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), initiateHearingCommand));

        stubCourtRooms(hearingCommandHelper);

        final Hearing hearing = initiateHearingCommand.getHearing();
        final UUID hearingId = hearing.getId();

        final CommandHelpers.AllNowsReferenceDataHelper allNows = setupNowsReferenceData(orderedDate);

        final SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(hearingCommandHelper.it(), orderedDate);
        final UUID resultDefinitionID = getResultDefinitionId(allNows, false);

        saveDraftResultCommand.getTarget().setResultLines(singletonList(
                standardResultLineTemplate(randomUUID(), resultDefinitionID, orderedDate)
                        .build()));
        testSaveDraftResult(saveDraftResultCommand);

        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getApprovalsRequested, hasSize(Is.is(1)))
                        .with(Hearing::getApprovalsRequested, first(isBean(ApprovalRequest.class)
                                .with(ApprovalRequest::getHearingId, is(hearingId))
                                .with(ApprovalRequest::getUserId, is(userId))
                                .with(ApprovalRequest::getApprovalType, is(CHANGE))
                        ))));

        saveDraftResultCommand.getTarget().setResultLines(singletonList(
                standardResultLineTemplate(randomUUID(), resultDefinitionID, orderedDate)
                        .build()));
        testSaveDraftResult(saveDraftResultCommand);

        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getApprovalsRequested, hasSize(Is.is(1)))
                        .with(Hearing::getApprovalsRequested, first(isBean(ApprovalRequest.class)
                                .with(ApprovalRequest::getHearingId, is(hearingId))
                                .with(ApprovalRequest::getUserId, is(userId))
                                .with(ApprovalRequest::getApprovalType, is(CHANGE))
                        ))));

        makeCommand(getRequestSpec(), "hearing.request-approval")
                .ofType("application/vnd.hearing.request-approval+json")
                .withCppUserId(getLoggedInUser())
                .withPayload(
                        RequestApprovalCommand.newBuilder()
                                .withHearingId(hearingId)
                                .withUserId(userId)
                                .withRequestApprovalTime(requestApprovalTime)
                                .withApprovalType(APPROVAL)
                                .build()
                )
                .executeSuccessfully();


        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getApprovalsRequested, hasSize(Is.is(1)))
                        .with(Hearing::getApprovalsRequested, first(isBean(ApprovalRequest.class)
                                        .with(ApprovalRequest::getHearingId, is(hearingId))
                                        .with(ApprovalRequest::getUserId, is(userId))
                                        .with(ApprovalRequest::getApprovalType, is(APPROVAL))
                                )
                        )
                ));

        makeCommand(getRequestSpec(), "hearing.validate-result-amendments")
                .ofType("application/vnd.hearing.validate-result-amendments+json")
                .withCppUserId(getLoggedInUser())
                .withPayload(
                        ValidateResultAmendmentsCommand.newBuilder()
                                .withId(hearingId)
                                .withUserId(randomUUID())
                                .withValidateAmendmentsTime(ZonedDateTime.now())
                                .build()
                )
                .executeSuccessfully();
        Queries.getHearingPollForMatch(hearingId, DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearingId))
                        .with(Hearing::getApprovalsRequested, hasSize(Is.is(0))
                        )));


    }

    public void stubCourtRooms(final CommandHelpers.InitiateHearingCommandHelper commandHelper) {
        changeCourtRoomsStubWithAdding(createObjectBuilder()
                .add("id", commandHelper.getHearing().getCourtCentre().getId().toString())
                .add("oucode", "AAAAAA01")
                .add("courtrooms", createArrayBuilder()
                        .add(createObjectBuilder()
                                .add("id", commandHelper.getHearing().getCourtCentre().getRoomId().toString())
                                .build())
                        .add(createObjectBuilder()
                                .add("id", "0c329efc-0c9a-4057-b119-e45147b82591")
                                .build())
                        .build())
                .build());
    }

    private void testSaveDraftResult(final SaveDraftResultCommand saveDraftResultCommand) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final Target target = saveDraftResultCommand.getTarget();
        final List<ResultLine> resultLines = target.getResultLines();
        // currently not sending result lines in draft
        target.setResultLines(null);
        final BeanMatcher<PublicHearingDraftResultSaved> beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(target.getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(target.getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(target.getDefendantId()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(target.getOffenceId()));

        final String expectedMetaDataContextUser = getLoggedInUser().toString();
        final String expectedMetaDataName = PUBLIC_HEARING_DRAFT_RESULT_SAVED;
        try (final Utilities.EventListener publicEventResulted = listenFor(PUBLIC_HEARING_DRAFT_RESULT_SAVED)
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser)) {

            makeCommand(getRequestSpec(), "hearing.save-draft-result")
                    .ofType("application/vnd.hearing.save-draft-result+json")
                    .withArgs(saveDraftResultCommand.getTarget().getHearingId())
                    .withPayload(saveDraftResultCommand.getTarget())
                    .executeSuccessfully();

            publicEventResulted.waitFor();
        }
        target.setResultLines(resultLines);
    }

    private UUID getResultDefinitionId(final CommandHelpers.AllNowsReferenceDataHelper allNows, final boolean mandatory) {
        return allNows.it().getNows().get(0).getResultDefinitions().stream()
                .filter(rd -> rd.getMandatory() == mandatory)
                .map(NowResultDefinitionRequirement::getId).findFirst().orElseThrow(() -> new RuntimeException("invalid test data"));
    }

    private CommandHelpers.AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate) {
        AllNows allnows = AllNows.allNows()
                .setNows(Arrays.asList(NowDefinition.now()
                                .setId(NOTICE_OF_FINANCIAL_PENALTY_NOW_DEFINITION_ID)
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(RD_FINE)
                                                .setMandatory(true)
                                                .setWelshText("Welsh Text Primary")
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setWelshText("NowLevel/" + STRING.next() + " Welsh")
                                .setWelshName("Welsh Name")
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false),
                        NowDefinition.now()
                                .setId(ATTACHMENT_OF_EARNINGS_NOW_DEFINITION_ID)
                                .setResultDefinitions(asList(NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(fromString("de946ddc-ad77-44b1-8480-8bbc251cdcfb")) // FIDICI
                                                .setWelshText("Welsh Text Primary")
                                                .setMandatory(true)
                                                .setPrimary(true),
                                        NowResultDefinitionRequirement.resultDefinitions()
                                                .setId(randomUUID())
                                                .setMandatory(false)
                                                .setPrimary(false)
                                                .setWelshText("Welsh Text Not Primary")
                                ))
                                .setName(STRING.next())
                                .setText("NowLevel/" + STRING.next())
                                .setTemplateName(STRING.next())
                                .setRank(INTEGER.next())
                                .setJurisdiction("B")
                                .setRemotePrintingRequired(false)
                                .setText(STRING.next())
                                .setWelshText("welshText")
                                .setWelshName("welshName")
                ));
        return setupNowsReferenceData(referenceDate, allnows);
    }


    private CommandHelpers.AllNowsReferenceDataHelper setupNowsReferenceData(final LocalDate referenceDate, final AllNows data) {
        final CommandHelpers.AllNowsReferenceDataHelper allNows = h(data);
        stubGetAllNowsMetaData(referenceDate, allNows.it());
        return allNows;
    }

}