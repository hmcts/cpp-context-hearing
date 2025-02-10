package uk.gov.moj.cpp.hearing.it;

import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.justice.services.test.utils.core.messaging.MetadataBuilderFactory.metadataOf;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Queries.getCalculatedCustodyTimeLimitExpiryDate;
import static uk.gov.moj.cpp.hearing.it.Queries.getHearingPollForMatch;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResults;
import static uk.gov.moj.cpp.hearing.it.UseCases.shareResultsPerDay;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.CoreTestTemplates.CoreTemplateArguments.toMap;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.customStructureInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.standardResultLineTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandV2Template;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.getPublicTopicInstance;
import static uk.gov.moj.cpp.hearing.utils.QueueUtil.sendMessage;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubOrganisationUnit;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubPublicHolidays;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;

import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.CustodyTimeLimit;
import uk.gov.justice.core.courts.Defendant;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Level;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.services.common.converter.StringToJsonObjectConverter;
import uk.gov.justice.services.common.util.UtcClock;
import uk.gov.justice.services.test.utils.core.http.ResponseData;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.ShareDaysResultsCommand;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResultedV2;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.TestUtilities;
import uk.gov.moj.cpp.hearing.utils.ReferenceDataStub;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.json.JsonObject;

import io.restassured.path.json.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AutoPopulateCTLExpiryDateIT extends AbstractIT {
    private static final String FIELD_CUSTODY_TIME_LIMIT = "custodyTimeLimit";

    private final String REMAND_STATUS_RESULT_DEFINITION_ID = "d0a369c9-5a28-40ec-99cb-da7943550b18";
    private final String CTL_RESULT_DEFINITION_ID = "311c8e5c-6f22-44a7-8c97-f72324cb05c4";
    private final String CTLE_RESULT_DEFINITON_ID = "68737dc2-8d10-45e0-8bc1-21a523100fa2";

    private final String TV_LINK_AT_NEXT_HEARING_PROMPT_ID = "21ecdc07-7397-4eac-9c26-e66dfeb32d01";
    private final String TV_LINK_PRE_HEARING_PROMPT_ID = "343de039-f33b-4770-b61c-4d6db4ae369c";
    private final String REMAND_BASIS = "d0099592-a901-466a-930e-c1f12f77c95c";
    private final String PRISON_PROMPT_ID = "1b278d3d-83bd-442e-b3da-843f3e15e41f";
    private final String RISK_OR_VULNERABILITY_FACTORS_PROMPT_ID = "513a9dfd-8a44-44a7-880a-6f1d2d725fe3";
    private final String BAIL_EXCEPTION_PROMPT_ID = "7b2e6219-ddcb-4afa-8615-8de978e60af1";
    private final String BAIL_EXCEPTION_REASON_PROMPT_ID = "53ac8d08-49a9-4495-ab3a-0ef94ca9e560";
    private final String ADDITIONAL_REASONS_PROMPT_ID = "18c7f2fb-19bd-4a22-9c4d-5852a626daca";

    private final String CTL_EXPIRES_PROMPT_ID = "5348fd0e-0670-4811-997f-1baa78b06d62";
    private final String TIME_SPEND_IN_CUSTODY_PROMPT_ID = "4483c255-2761-4549-a70a-314aa311d052";

    private final String CTL_EXPIRES_UNTIL_PROMPT_ID = "3c915c3b-57d8-45f4-972e-a5d2b5f91bfa";
    private final String REASON_FOR_EXTN_PROMPT_ID = "3578eb56-309b-4521-a468-33139d6a3536";

    private static final UUID WITHDRAWN_RESULT_ID = UUID.fromString("eb2e4c4f-b738-4a4d-9cce-0572cecb7cb8");
    private static final UUID REMAND_STATUS_PROMPT_ID = UUID.fromString("9403f0d7-90b5-4377-84b4-f06a77811362");
    private static final UUID CROWN_COURT_RESULT_DEFINITION_ID = UUID.fromString("fbed768b-ee95-4434-87c8-e81cbc8d24c8");
    private final String WITHDRAWN_PROMPT_ID = "1d20aad5-9dc2-42a2-9498-5687f3e5ce33";

    private final UtcClock utcClock = new UtcClock();

    @BeforeEach
    public void setup() {
        stubPublicHolidays();
    }

    @Test
    public void shouldShareResultsWithCTLExtendedResult() {
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();
        final String ctlExtendedDate = "2021-06-04";

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setProceedingsConcluded(false);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setVerdict(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPlea(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAllocationDecision().setMotReasonDescription("Defendant consents to summary trial");

        final Hearing hearing = createHearing(initiateHearing);

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);

        SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForCTLE(saveSingleDayDraftResultCommand, ctlExtendedDate);

        List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResultWithCourtClerk(hearing, targets);

        assertHearingResultsAreShared(hearing);

        try (final EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            final DelegatedPowers courtClerk = getDelegatedPowers();

            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.isCtlExtended"), is("true"));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.timeLimit"), is(ctlExtendedDate));
        }
    }

    @Test
    public void shouldUpdateHearingsWithExtendedCustodyTimeLimit() throws IOException {
        final UUID offenceId = randomUUID();

        final String ctlExtendedDate = "2021-06-04";

        final Hearing hearing1 = createHearingWithOffence(offenceId);
        final Hearing hearing2 = createHearingWithOffence(offenceId);

        final String eventPayloadString = getStringFromResource("public.events.progression.custody-time-limit-extended.json")
                .replaceAll("HEARING_ID1", hearing1.getId().toString())
                .replaceAll("HEARING_ID2", hearing2.getId().toString())
                .replaceAll("OFFENCE_ID", offenceId.toString())
                .replaceAll("EXTENDED_CTL_DATE", ctlExtendedDate);

        sendMessage(getPublicTopicInstance().createProducer(),
                "public.events.progression.custody-time-limit-extended",
                new StringToJsonObjectConverter().convert(eventPayloadString),
                metadataOf(randomUUID(), "public.events.progression.custody-time-limit-extended")
                        .withUserId(randomUUID().toString())
                        .build()
        );

        final LocalDate ctlExtendedDateLD = LocalDate.parse(ctlExtendedDate);
        assertHearingHasOffenceWithExtendedCTL(hearing1, offenceId, ctlExtendedDateLD);
        assertHearingHasOffenceWithExtendedCTL(hearing2, offenceId, ctlExtendedDateLD);
    }

    @Test
    public void shouldShareResultsWithAutoCalculatedCTLExpiryDateForAdult() {

        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setProceedingsConcluded(false);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setVerdict(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPlea(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAllocationDecision().setMotReasonDescription("Defendant consents to summary trial");

        final Hearing hearing = createHearing(initiateHearing);

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);


        SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForRemandStatus(saveSingleDayDraftResultCommand);

        List<Target> targets = getTargets(saveSingleDayDraftResultCommand);


        final ResponseData responseData = getCalculatedCustodyTimeLimitExpiryDate(initiateHearing.getHearing().getId(), orderDate.toString(), offenceId, "C");

        final String ctlExpiryDate = new StringToJsonObjectConverter().convert(responseData.getPayload()).getString(FIELD_CUSTODY_TIME_LIMIT);

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForCTL(saveSingleDayDraftResultCommand, ctlExpiryDate);

        targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final DelegatedPowers courtClerk = getDelegatedPowers();


        try (final EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.isCtlExtended"), is("false"));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.timeLimit"), is(ctlExpiryDate));

        }

    }

    @Test
    public void shouldShareResultsWithAutoCalculatedCTLExpiryDateForAdultWithExtendedCTL() {
        final LocalDate orderDate = now();
        final LocalDate extendedCTLDate = now().plusDays(10);
        final UUID offenceId = randomUUID();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setProceedingsConcluded(false);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setVerdict(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPlea(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(extendedCTLDate)
                .withIsCtlExtended(true)
                .build());
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAllocationDecision().setMotReasonDescription("Defendant consents to summary trial");

        final Hearing hearing = createHearing(initiateHearing);

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);


        SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForRemandStatus(saveSingleDayDraftResultCommand);

        final ResponseData responseData = getCalculatedCustodyTimeLimitExpiryDate(initiateHearing.getHearing().getId(), orderDate.toString(), offenceId, "C");

        final String ctlExpiryDate = new StringToJsonObjectConverter().convert(responseData.getPayload()).getString(FIELD_CUSTODY_TIME_LIMIT);

        assertThat(ctlExpiryDate, is(extendedCTLDate.toString()));

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForCTL(saveSingleDayDraftResultCommand, ctlExpiryDate);

        List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final DelegatedPowers courtClerk = getDelegatedPowers();

        try (final EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.isCtlExtended"), is("false"));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.timeLimit"), is(extendedCTLDate.toString()));

        }

    }

    @Test
    public void shouldShareResultsWithAutoCalculatedCTLExpiryDateForYouth() {
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).setIsYouth(true);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setModeOfTrial("Indictable");
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setProceedingsConcluded(false);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setVerdict(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPlea(null);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getAllocationDecision().setMotReasonDescription("Youth - Court directs trial by jury (grave crime)");

        final Hearing hearing = createHearing(initiateHearing);

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);

        SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForRemandStatus(saveSingleDayDraftResultCommand);

        List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        final ResponseData responseData = getCalculatedCustodyTimeLimitExpiryDate(initiateHearing.getHearing().getId(), orderDate.toString(), offenceId, "C");

        final String ctlExpiryDate = new StringToJsonObjectConverter().convert(responseData.getPayload()).getString(FIELD_CUSTODY_TIME_LIMIT);

        saveSingleDayDraftResultCommand = setPromptForSaveDraftResultCommandForCTL(saveSingleDayDraftResultCommand, ctlExpiryDate);

        targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final DelegatedPowers courtClerk = getDelegatedPowers();

        try (final EventListener publicEventResultedListener = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResultedV2.class, isBean(PublicHearingResultedV2.class)
                        .with(PublicHearingResultedV2::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(hearing.getId())))))) {

            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            final JsonPath publicHearingResulted = publicEventResultedListener.waitFor();

            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.isCtlExtended"), is("false"));
            assertThat(publicHearingResulted.getString("hearing.prosecutionCases[0].defendants[0].offences[0].custodyTimeLimit.timeLimit"), is(ctlExpiryDate));

        }

    }

    @Test
    public void shouldStopCTLClockWhenRemandStatusOnBail() {
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(now().plusDays(10))
                .build());

        final Hearing hearing = createHearing(initiateHearing);
        final SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());

        saveSingleDayDraftResultCommand.getTarget().getResultLines().get(0).setResultDefinitionId(CROWN_COURT_RESULT_DEFINITION_ID);
        setPromptForSaveDraftResultCommandForOnBailRemandStatus(saveSingleDayDraftResultCommand);
        final List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResultWithCourtClerk(hearing, targets);

        assertHearingResultsAreShared(hearing);

        assertCTLClockStopped(hearing);

        final ResponseData responseData = getCalculatedCustodyTimeLimitExpiryDate(initiateHearing.getHearing().getId(), orderDate.toString(), offenceId, "C");

        final JsonObject ctlExpiryDate = new StringToJsonObjectConverter().convert(responseData.getPayload());
        assertThat(ctlExpiryDate.isEmpty(), is(true));

    }

    @Test
    public void shouldStopCTLClockWhenFinalResultIsWithDrawn() {
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(now().plusDays(10))
                .build());

        final Hearing hearing = createHearing(initiateHearing);

        final SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearing, orderDate, now());
        saveSingleDayDraftResultCommand.getTarget().getResultLines().get(0).setResultDefinitionId(WITHDRAWN_RESULT_ID);
        setPromptForWithDrawnResultCommandForCTL(saveSingleDayDraftResultCommand, orderDate.toString());

        final List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        shareResultWithCourtClerk(hearing, targets);

        assertHearingResultsAreShared(hearing);

        assertCTLClockStopped(hearing);

        final ResponseData responseData = getCalculatedCustodyTimeLimitExpiryDate(initiateHearing.getHearing().getId(), orderDate.toString(), offenceId, "C");

        final JsonObject ctlExpiryDate = new StringToJsonObjectConverter().convert(responseData.getPayload());
        assertThat(ctlExpiryDate.isEmpty(), is(true));

    }

    @Test
    public void shouldSetDateHeldInCustodyWhenFirstHearingInCustody() {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();
        final LocalDate hearingDay = now();

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        final InitiateHearingCommand initiateHearingCommand = initiateHearing(getRequestSpec(), initiateHearing);
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearingCommand);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);

        stubCourtRoom(hearing);

        final SaveDraftResultCommand saveSingleDayDraftResultCommand = getSaveDraftResultCommandForRemandInCustody(orderDate, initiateHearingCommandHelper);

        final List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        final DelegatedPowers courtClerk = getDelegatedPowers();

        try (final EventListener publicEventResulted = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getPreviousDaysHeldInCustody, nullValue())
                                                        .with(Offence::getDateHeldInCustodySince, is(hearingDay))))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {
            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(hearingDay);
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);
            publicEventResulted.waitFor();

        }
    }

    @Test
    public void shouldKeepPreviousDateHeldInCustodyWhenHearingAlreadyInCustody() {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();
        final LocalDate dateHeldInCustodySince = now().minusDays(10);
        final int previousDaysHeldInCustody = 3;

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getHearingDays().get(0).setSittingDay(utcClock.now());
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(now().plusDays(10))
                .build());

        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPreviousDaysHeldInCustody(previousDaysHeldInCustody);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setDateHeldInCustodySince(dateHeldInCustodySince);

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(getRequestSpec(), initiateHearing);
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearingCommand);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);
        stubCourtRoom(hearing);

        final SaveDraftResultCommand saveSingleDayDraftResultCommand = getSaveDraftResultCommandForRemandInCustody(orderDate, initiateHearingCommandHelper);

        final List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        final DelegatedPowers courtClerk = getDelegatedPowers();

        try (final EventListener publicEventResulted = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getPreviousDaysHeldInCustody, is(previousDaysHeldInCustody))
                                                        .with(Offence::getDateHeldInCustodySince, is(dateHeldInCustodySince))))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {
            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            publicEventResulted.waitFor();

        }
    }

    @Test
    public void shouldClearDateHeldInCustodySinceAndCalculateDaysSpentWhenResultIsOnBail() {

        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());
        final LocalDate orderDate = now();
        final UUID offenceId = randomUUID();
        final LocalDate dateHeldInCustodySince = now().minusDays(10);
        final int previousDaysHeldInCustody = 3;
        final int newPreviousDaysHeldInCustody = 13;

        final InitiateHearingCommand initiateHearing = customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId));
        initiateHearing.getHearing().getHearingDays().get(0).setSittingDay(utcClock.now());
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setCustodyTimeLimit(CustodyTimeLimit.custodyTimeLimit()
                .withTimeLimit(now().plusDays(182))
                .build());

        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setPreviousDaysHeldInCustody(previousDaysHeldInCustody);
        initiateHearing.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).setDateHeldInCustodySince(dateHeldInCustodySince);

        final InitiateHearingCommand initiateHearingCommand = initiateHearing(getRequestSpec(), initiateHearing);
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearingCommand);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();

        stubOrganisationUnit(hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .collect(Collectors.joining()));

        hearing.getHearingDays()
                .stream()
                .map(HearingDay::getCourtCentreId)
                .map(UUID::toString)
                .forEach(ReferenceDataStub::stubOrganisationUnit);
        stubCourtRoom(hearing);

        final SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, now());

        saveSingleDayDraftResultCommand.getTarget().setResultLines(asList(
                standardResultLineTemplate(randomUUID(), CROWN_COURT_RESULT_DEFINITION_ID, orderDate)
                        .withPrompts(singletonList(Prompt.prompt()
                                .withId(UUID.fromString("5f507153-6dc9-4ec0-94db-c821eff333f1"))
                                .withPromptRef("HCROOM")
                                .withLabel(STRING.next())
                                .withValue(randomUUID().toString())
                                .withWelshValue(STRING.next())
                                .build()
                        ))
                        .build(),
                standardResultLineTemplate(randomUUID(), UUID.fromString("bb90e801-0066-4bdf-85e6-8d64bc683f0c"), orderDate).withPrompts(
                        singletonList(Prompt.prompt()
                                .withId(UUID.fromString("21541638-82dd-4a9f-a05c-67589dc8dce9"))
                                .withPromptRef("courtOrPoliceStationWhichPreviouslyGrantedBail")
                                .withLabel(STRING.next())
                                .withValue("on Bail")
                                .withWelshValue(STRING.next())
                                .build())
                ).build()
        ));

        final List<Target> targets = getTargets(saveSingleDayDraftResultCommand);

        final DelegatedPowers courtClerk = getDelegatedPowers();

        try (final EventListener publicEventResulted = listenFor("public.events.hearing.hearing-resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                        .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                                .with(Defendant::getOffences, first(isBean(Offence.class)
                                                        .with(Offence::getPreviousDaysHeldInCustody, is(newPreviousDaysHeldInCustody))
                                                        .with(Offence::getDateHeldInCustodySince, nullValue())))))))
                                .with(Hearing::getCourtCentre, isBean(CourtCentre.class)
                                        .with(CourtCentre::getId, is(hearing.getCourtCentre().getId()))))))) {
            ShareDaysResultsCommand shareDaysResultsCommand = basicShareResultsCommandV2Template();
            shareDaysResultsCommand.setHearingDay(now());
            shareResultsPerDay(getRequestSpec(), hearing.getId(), with(
                    shareDaysResultsCommand,
                    command -> command.setCourtClerk(courtClerk)
            ), targets);

            publicEventResulted.waitFor();

        }
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommandForOnBailRemandStatus(final SaveDraftResultCommand saveDraftResultCommand) {
        saveDraftResultCommand.getTarget().getResultLines().get(0).setPrompts(getOnBailRemandStatusPrompts());
        saveDraftResultCommand.getTarget().getResultLines().get(0).setResultDefinitionId(fromString(REMAND_STATUS_RESULT_DEFINITION_ID));
        return saveDraftResultCommand;
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommandForRemandStatus(final SaveDraftResultCommand saveDraftResultCommand) {
        final List<ResultLine> resultLineList = new ArrayList<>();
        final ResultLine resultLine = ResultLine.resultLine()
                .withOrderedDate(now())
                .withResultLineId(UUID.fromString(REMAND_STATUS_RESULT_DEFINITION_ID))
                .withIsModified(false)
                .withIsDeleted(false)
                .withIsComplete(false)
                .withPrompts(getRemandStatusPrompts())
                .withLevel(Level.OFFENCE)
                .withResultDefinitionId(UUID.fromString(REMAND_STATUS_RESULT_DEFINITION_ID))
                .build();
        resultLineList.add(resultLine);
        saveDraftResultCommand.getTarget().setResultLines(resultLineList);
        return saveDraftResultCommand;
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommandForCTL(final SaveDraftResultCommand saveDraftResultCommand, final String ctlExpiryDate) {
        final List<ResultLine> resultLineList = new ArrayList<>();
        final ResultLine resultLine = ResultLine.resultLine()
                .withOrderedDate(now())
                .withResultLineId(UUID.fromString(CTL_RESULT_DEFINITION_ID))
                .withIsModified(false)
                .withIsComplete(false)
                .withIsDeleted(false)
                .withPrompts(getCTLPrompts(ctlExpiryDate))
                .withLevel(Level.OFFENCE)
                .withResultDefinitionId(UUID.fromString(CTL_RESULT_DEFINITION_ID))
                .build();
        resultLineList.add(resultLine);
        saveDraftResultCommand.getTarget().setResultLines(resultLineList);
        return saveDraftResultCommand;
    }

    private SaveDraftResultCommand setPromptForWithDrawnResultCommandForCTL(final SaveDraftResultCommand saveDraftResultCommand, final String ctlExpiryDate) {
        final List<ResultLine> resultLineList = new ArrayList<>();
        final ResultLine resultLine = ResultLine.resultLine()
                .withOrderedDate(now())
                .withResultLineId(WITHDRAWN_RESULT_ID)
                .withIsModified(false)
                .withIsComplete(false)
                .withIsDeleted(false)
                .withPrompts(getWithDrawnPrompts(ctlExpiryDate))
                .withLevel(Level.OFFENCE)
                .withResultDefinitionId(WITHDRAWN_RESULT_ID)
                .build();
        resultLineList.add(resultLine);
        saveDraftResultCommand.getTarget().setResultLines(resultLineList);
        return saveDraftResultCommand;
    }

    private List<Prompt> getOnBailRemandStatusPrompts() {
        return Arrays.asList(Prompt.prompt()
                .withId(REMAND_STATUS_PROMPT_ID)
                .withLabel(STRING.next())
                .withValue("Conditional Bail")
                .withWelshValue(STRING.next())
                .withPromptRef("remandStatus")
                .build());
    }

    private List<Prompt> getCTLPrompts(final String localDate) {
        final List<Prompt> ctlPromptList = new ArrayList();
        final Prompt custodyTimeLimitExpires = Prompt.prompt()
                .withId(fromString(CTL_EXPIRES_PROMPT_ID))
                .withPromptRef("CTLDATE")
                .withLabel("Custody time limit expires")
                .withValue(localDate)
                .withWelshValue(localDate)
                .build();

        final Prompt timeSpendInCustody = Prompt.prompt()
                .withId(fromString(TIME_SPEND_IN_CUSTODY_PROMPT_ID))
                .withPromptRef("CTLTIME")
                .withLabel("Time spent in custody (in days)")
                .withValue("0")
                .withWelshValue("0")
                .build();


        ctlPromptList.add(custodyTimeLimitExpires);
        ctlPromptList.add(timeSpendInCustody);
        return ctlPromptList;
    }

    private List<Prompt> getWithDrawnPrompts(final String localDate) {
        final List<Prompt> withDrawnPromptList = new ArrayList();
        final Prompt withDrawnPrompt = Prompt.prompt()
                .withId(fromString(WITHDRAWN_PROMPT_ID))
                .withLabel("Reasons")
                .withValue(localDate)
                .withWelshValue(localDate)
                .build();
        withDrawnPromptList.add(withDrawnPrompt);
        return withDrawnPromptList;
    }

    private HashMap<UUID, Map<UUID, List<UUID>>> getUuidMapForSingleCaseStructure(final UUID offenceId) {
        HashMap<UUID, Map<UUID, List<UUID>>> caseStructure = new HashMap<>();
        caseStructure.put(randomUUID(), toMap(randomUUID(), TestUtilities.asList(offenceId)));
        return caseStructure;
    }

    private SaveDraftResultCommand getSaveDraftResultCommandForRemandInCustody(final LocalDate orderDate, final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper) {
        final SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, now());

        saveSingleDayDraftResultCommand.getTarget().setResultLines(asList(
                standardResultLineTemplate(randomUUID(), UUID.fromString(REMAND_STATUS_RESULT_DEFINITION_ID), orderDate).withPrompts(
                        singletonList(Prompt.prompt()
                                .withId(UUID.fromString("53ac8d08-49a9-4495-ab3a-0ef94ca9e560"))
                                .withLabel(STRING.next())
                                .withValue("Bail Exception reason")
                                .withWelshValue(STRING.next())
                                .build())
                ).build()
        ));
        return saveSingleDayDraftResultCommand;
    }

    private List<Target> getTargets(final SaveDraftResultCommand saveSingleDayDraftResultCommand) {
        final List<Target> targets = new ArrayList<>();
        targets.add(saveSingleDayDraftResultCommand.getTarget());
        return targets;
    }

    private List<Prompt> getRemandStatusPrompts() {
        final List<Prompt> promptList = new ArrayList();

        final Prompt tvLinkAtNextHearing = Prompt.prompt()
                .withId(fromString(TV_LINK_AT_NEXT_HEARING_PROMPT_ID))
                .withPromptRef("PromptRef1")
                .withLabel("TV link at next hearing")
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();

        final Prompt tvLinkPreHearing = Prompt.prompt()
                .withId(fromString(TV_LINK_PRE_HEARING_PROMPT_ID))
                .withPromptRef("PromptRef2")
                .withLabel("TV link pre-hearing conference time")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();


        final Prompt remandBasis = Prompt.prompt()
                .withId(fromString(REMAND_BASIS))
                .withPromptRef("PromptRef3")
                .withLabel("Remand basis")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();

        final Prompt prison = Prompt.prompt()
                .withId(fromString(PRISON_PROMPT_ID))
                .withPromptRef("PromptRef4")
                .withLabel("Prison")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();

        final Prompt riskOrVulnerabilityFactors = Prompt.prompt()
                .withId(fromString(RISK_OR_VULNERABILITY_FACTORS_PROMPT_ID))
                .withPromptRef("PromptRef5")
                .withLabel("Risk or vulnerability factors")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();

        final Prompt bailException = Prompt.prompt()
                .withId(fromString(BAIL_EXCEPTION_PROMPT_ID))
                .withPromptRef("bailException")
                .withLabel("Bail exception")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();


        final Prompt bailExceptionReason = Prompt.prompt()
                .withId(fromString(BAIL_EXCEPTION_REASON_PROMPT_ID))
                .withPromptRef("bailExceptionReason")
                .withLabel("Bail exception reason")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();

        final Prompt additionalReasons = Prompt.prompt()
                .withId(fromString(ADDITIONAL_REASONS_PROMPT_ID))
                .withPromptRef("PromptRef8")
                .withLabel("Additional reasons")
                .withValue("value1")
                .withWelshValue(STRING.next())
                .build();


        promptList.add(tvLinkAtNextHearing);
        promptList.add(tvLinkPreHearing);
        promptList.add(remandBasis);
        promptList.add(prison);
        promptList.add(riskOrVulnerabilityFactors);
        promptList.add(bailException);
        promptList.add(bailExceptionReason);
        promptList.add(additionalReasons);

        return promptList;
    }

    private DelegatedPowers getDelegatedPowers() {
        return DelegatedPowers.delegatedPowers()
                .withFirstName("Andrew").withLastName("Eldritch")
                .withUserId(randomUUID()).build();
    }

    private void shareResultWithCourtClerk(final Hearing hearing, final List<Target> targets) {
        final DelegatedPowers courtClerk1 = getDelegatedPowers();

        shareResults(getRequestSpec(), hearing.getId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(courtClerk1)
        ), targets);
    }

    private void assertHearingResultsAreShared(final Hearing hearing) {
        getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHasSharedResults, is(true))));
    }

    private void stubCourtCentre(final Hearing hearing) {
        final CourtCentre courtCentre = hearing.getCourtCentre();
        hearing.getProsecutionCases().forEach(prosecutionCase -> stubLjaDetails(courtCentre, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId()));
        stubGetReferenceDataCourtRooms(courtCentre, hearing.getHearingLanguage(), ouId3, ouId4);
    }

    private void assertCTLClockStopped(final Hearing hearing) {
        getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(h -> h.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCtlClockStopped(), is(true))
                        .with(h -> h.getProsecutionCases().get(0).getDefendants().get(0).getOffences().get(0).getCustodyTimeLimit(), nullValue())));
    }

    private SaveDraftResultCommand setPromptForSaveDraftResultCommandForCTLE(final SaveDraftResultCommand saveDraftResultCommand,
                                                                             final String ctlExpiryDate) {
        final List<ResultLine> resultLineList = new ArrayList<>();
        final ResultLine resultLine = ResultLine.resultLine()
                .withOrderedDate(now())
                .withResultLineId(fromString(CTLE_RESULT_DEFINITON_ID))
                .withIsModified(false)
                .withIsComplete(false)
                .withIsDeleted(false)
                .withPrompts(getCTLEPrompts(ctlExpiryDate))
                .withLevel(Level.OFFENCE)
                .withResultDefinitionId(fromString(CTLE_RESULT_DEFINITON_ID))
                .build();
        resultLineList.add(resultLine);
        saveDraftResultCommand.getTarget().setResultLines(resultLineList);
        return saveDraftResultCommand;
    }

    private List<Prompt> getCTLEPrompts(final String extendedCTLDate) {
        final List<Prompt> ctlePromptList = new ArrayList();
        final Prompt custodyTimeLimitExpiresUntil = Prompt.prompt()
                .withId(fromString(CTL_EXPIRES_UNTIL_PROMPT_ID))
                .withLabel("Custody time limit extended until")
                .withValue(extendedCTLDate)
                .withWelshValue(extendedCTLDate)
                .build();
        final Prompt reasonForExtension = Prompt.prompt()
                .withId(fromString(REASON_FOR_EXTN_PROMPT_ID))
                .withLabel("Reason for extension")
                .withValue(extendedCTLDate)
                .withWelshValue(extendedCTLDate)
                .build();
        final Prompt custodyTimeLimitExpires = Prompt.prompt()
                .withId(fromString(CTL_EXPIRES_PROMPT_ID))
                .withLabel("Custody time limit expires")
                .withValue(extendedCTLDate)
                .withWelshValue(extendedCTLDate)
                .build();
        final Prompt timeSpendInCustody = Prompt.prompt()
                .withId(fromString(TIME_SPEND_IN_CUSTODY_PROMPT_ID))
                .withLabel("Time spent in custody (in days)")
                .withValue(extendedCTLDate)
                .withWelshValue(extendedCTLDate)
                .build();
        ctlePromptList.add(custodyTimeLimitExpiresUntil);
        ctlePromptList.add(reasonForExtension);
        ctlePromptList.add(custodyTimeLimitExpires);
        ctlePromptList.add(timeSpendInCustody);
        return ctlePromptList;
    }

    private Hearing createHearingWithOffence(final UUID offenceId) {
        return createHearing(customStructureInitiateHearingTemplate(getUuidMapForSingleCaseStructure(offenceId)));
    }

    private Hearing createHearing(final InitiateHearingCommand initiateHearing) {
        final InitiateHearingCommand initiateHearingCommand1 = initiateHearing(getRequestSpec(), initiateHearing);
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearingCommand1);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        stubCourtCentre(hearing);
        return hearing;
    }

    private void assertHearingHasOffenceWithExtendedCTL(final Hearing hearing, final UUID offenceId, final LocalDate ctlExtendedDateLD) {
        getHearingPollForMatch(hearing.getId(), isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getProsecutionCases, first(isBean(ProsecutionCase.class)
                                .with(ProsecutionCase::getDefendants, first(isBean(Defendant.class)
                                        .with(Defendant::getOffences, first(isBean(Offence.class)
                                                .with(Offence::getId, is(offenceId))
                                                .with(Offence::getCustodyTimeLimit, isBean(CustodyTimeLimit.class)
                                                        .withValue(CustodyTimeLimit::getIsCtlExtended, true)
                                                        .withValue(CustodyTimeLimit::getTimeLimit, ctlExtendedDateLD)
                                                )
                                        ))
                                ))
                        ))

                )
        );
    }
}
