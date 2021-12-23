package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static java.util.Collections.singletonList;
import static java.util.UUID.fromString;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.beans.HasPropertyWithValue.hasProperty;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.UseCases.setTrialType;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithMultidayHearing;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithPastMultidayHearing;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.ShareResultsCommandTemplates.basicShareResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.UpdateDefendantAttendanceCommandTemplates.updateDefendantAttendanceTemplate;
import static uk.gov.moj.cpp.hearing.test.TestUtilities.with;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.first;
import static uk.gov.moj.cpp.hearing.test.matchers.ElementAtListMatcher.second;
import static uk.gov.moj.cpp.hearing.test.matchers.MapStringToTypeMatcher.convertStringTo;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.CRACKED_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.INEFFECTIVE_TRIAL_TYPE_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_CODE;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.VERDICT_TYPE_GUILTY_ID;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetAllResultDefinitions;
import static uk.gov.moj.cpp.hearing.utils.ReferenceDataStub.stubGetReferenceDataCourtRooms;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_MILLIS;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.ResultDefinitionUtil.getCategoryForResultDefinition;
import static uk.gov.moj.cpp.hearing.utils.StubNowsReferenceData.setupNowsReferenceData;

import uk.gov.justice.core.courts.AttendanceDay;
import uk.gov.justice.core.courts.AttendanceType;
import uk.gov.justice.core.courts.CourtCentre;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Prompt;
import uk.gov.justice.core.courts.ResultLine;
import uk.gov.justice.hearing.courts.GetHearings;
import uk.gov.justice.hearing.courts.HearingSummaries;
import uk.gov.moj.cpp.hearing.command.TrialType;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.domain.event.DefendantAttendanceUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.PublicHearingResulted;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows.CrackedIneffectiveVacatedTrialType;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.AllResultDefinitions;
import uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.ResultDefinition;
import uk.gov.moj.cpp.hearing.it.Utilities.EventListener;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.HearingDetailsResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.AllNowsReferenceDataHelper;
import uk.gov.moj.cpp.hearing.test.TestTemplates;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

public class CrackedAndIneffectiveHearingIT extends AbstractIT {
    private LocalDate orderDate;
    private AllNowsReferenceDataHelper allNowsReferenceDataHelper;

    @Before
    public void setUp() {
        orderDate = PAST_LOCAL_DATE.next();
        allNowsReferenceDataHelper = setupNowsReferenceData(orderDate);
    }

    @Test
    public void shouldCancelRemainingHearingDaysWhenMultidayHearingIsIneffective() {
        shouldCancelRemainingDaysWhenMultidayHearing(INEFFECTIVE_TRIAL_TYPE_ID, null, true);
    }

    @Test
    public void shouldCancelRemainingHearingDaysWhenMultidayHearingIsCracked() {
        shouldCancelRemainingDaysWhenMultidayHearing(CRACKED_TRIAL_TYPE_ID, null, true);
    }

    @Test
    public void shouldNotCancelRemainingHearingDaysWhenMultidayHearingIsIneffective() {
        cleanDatabase("ha_hearing");
        shouldNotCancelAfterResultSharedDaysWhenMultidayHearing(INEFFECTIVE_TRIAL_TYPE_ID, null, null);
    }

    @Test
    public void shouldCancelRemainingHearingDaysAndRaisePublicEventWhenMultidayHearingIsCracked() {
        shouldCancelRemainingDaysAndRaisePublicEventWhenMultidayHearing(CRACKED_TRIAL_TYPE_ID);
    }

    @Test
    public void shouldNotCancelADayOfSingleDayHearing() {
        shouldNotCancelWhenSingleDayHearing(INEFFECTIVE_TRIAL_TYPE_ID, null);
    }

    private void shouldCancelRemainingDaysWhenMultidayHearing(final UUID trialTypeId,
                                                              final Boolean expectedFirstHearingDay,
                                                              final Boolean expectedSecondHearingDay) {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithMultidayHearing();
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        updateDefendantAndChangeVerdict(initiateHearingCommandHelper);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        final List<HearingDay> hearingDays = hearing.getHearingDays();
        final HearingDay crackedHearingDay = hearingDays.get(0);
        final HearingDay cancelledHearingDay = hearingDays.get(1);
        final TrialType trialType = TrialType.builder().withTrialTypeId(trialTypeId).build();

        SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, LocalDate.now());
        setTrialType(getRequestSpec(), hearing.getId(), trialType, false);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getHearingDays, first(hasProperty("isCancelled", is(expectedFirstHearingDay))))
                                .with(Hearing::getHearingDays, second(hasProperty("isCancelled", is(expectedSecondHearingDay))))
                                .with(Hearing::getHasSharedResults, is(true)
                                ))));

        shareResults(allNowsReferenceDataHelper, initiateHearingCommandHelper, saveDraftResultCommand, orderDate);
        publicEventResulted.waitFor();

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), crackedHearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getIsCancelled, nullValue())
                                ))))
        );

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), cancelledHearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .withValue(GetHearings::getHearingSummaries, null)
        );

        Queries.getHearingPollForMatch(hearing.getId(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(HearingDetailsResponse.class)
                .with(HearingDetailsResponse::getHearing, isBean(Hearing.class)
                        .with(Hearing::getId, is(hearing.getId()))
                        .with(Hearing::getHearingDays, hasSize(2))
                        .with(Hearing::getHearingDays, first(isBean(HearingDay.class)
                                .with(HearingDay::getIsCancelled, is(expectedFirstHearingDay))))
                        .with(Hearing::getHearingDays, second(isBean(HearingDay.class)
                                .with(HearingDay::getIsCancelled, is(expectedSecondHearingDay))))
                )
        );
    }

    private void shouldCancelRemainingDaysAndRaisePublicEventWhenMultidayHearing(final UUID trialTypeId) {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithMultidayHearing();
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        updateDefendantAndChangeVerdict(initiateHearingCommandHelper);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        final TrialType trialType = TrialType.builder().withTrialTypeId(trialTypeId).build();

        SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, LocalDate.now());
        setTrialType(getRequestSpec(), hearing.getId(), trialType, false);
        shareResults(allNowsReferenceDataHelper, initiateHearingCommandHelper, saveDraftResultCommand, orderDate);

        final EventListener publicDaysCancelled = listenFor("public.hearing.hearing-days-cancelled")
                .withFilter(isJson(withJsonPath("$.hearingId", is(hearing.getId().toString()))))
                .withFilter(isJson(withJsonPath("$.hearingDays", hasSize(2))))
                .withFilter(isJson(withoutJsonPath("$.hearingDays.[0].isCancelled")))
                .withFilter(isJson(withJsonPath("$.hearingDays.[1].isCancelled", is(Boolean.TRUE))));

        publicDaysCancelled.waitFor();
    }

    private void shouldNotCancelAfterResultSharedDaysWhenMultidayHearing(final UUID trialTypeId,
                                                                         final Boolean expectedFirstHearingDay,
                                                                         final Boolean expectedSecondHearingDay) {

        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithPastMultidayHearing(ZonedDateTime.now());
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        updateDefendantAndChangeVerdict(initiateHearingCommandHelper);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        final List<HearingDay> hearingDays = hearing.getHearingDays();
        final HearingDay firstHearingDay = hearingDays.get(0);
        final HearingDay secondHearingDay = hearingDays.get(1);
        final TrialType trialType = TrialType.builder().withTrialTypeId(trialTypeId).build();

        SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, LocalDate.now());
        setTrialType(getRequestSpec(), hearing.getId(), trialType, false);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getHearingDays, first(hasProperty("isCancelled", is(expectedFirstHearingDay))))
                                .with(Hearing::getHearingDays, second(hasProperty("isCancelled", is(expectedSecondHearingDay))))
                                .with(Hearing::getHasSharedResults, is(true)
                                ))));

        shareResults(allNowsReferenceDataHelper, initiateHearingCommandHelper, saveDraftResultCommand, orderDate);
        publicEventResulted.waitFor();

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), firstHearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getIsCancelled, nullValue())
                                ))))
        );

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), secondHearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getIsCancelled, nullValue())
                                ))))
        );
    }

    private void shouldNotCancelWhenSingleDayHearing(final UUID trialTypeId,
                                                     final Boolean expectedHearingDay) {
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplate();
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));
        updateDefendantAndChangeVerdict(initiateHearingCommandHelper);
        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        final List<HearingDay> hearingDays = hearing.getHearingDays();
        final HearingDay crackedHearingDay = hearingDays.get(0);
        final TrialType trialType = TrialType.builder().withTrialTypeId(trialTypeId).build();

        SaveDraftResultCommand saveDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, LocalDate.now());
        setTrialType(getRequestSpec(), hearing.getId(), trialType, false);

        final EventListener publicEventResulted = listenFor("public.hearing.resulted")
                .withFilter(convertStringTo(PublicHearingResulted.class, isBean(PublicHearingResulted.class)
                        .with(PublicHearingResulted::getHearing, isBean(Hearing.class)
                                .with(Hearing::getId, is(initiateHearingCommandHelper.getHearingId()))
                                .with(Hearing::getHearingDays, hasSize(1))
                                .with(Hearing::getHearingDays, first(hasProperty("isCancelled", is(expectedHearingDay))))
                                .with(Hearing::getHasSharedResults, is(true)
                                ))));

        shareResults(allNowsReferenceDataHelper, initiateHearingCommandHelper, saveDraftResultCommand, orderDate);
        publicEventResulted.waitFor();

        Queries.getHearingsByDatePollForMatch(hearing.getCourtCentre().getId(), hearing.getCourtCentre().getRoomId(), crackedHearingDay.getSittingDay().withZoneSameInstant(ZoneId.of("UTC")).toLocalDate().toString(), "00:00", "23:59", DEFAULT_POLL_TIMEOUT_IN_SEC,
                isBean(GetHearings.class)
                        .with(GetHearings::getHearingSummaries, first(isBean(HearingSummaries.class)
                                .with(HearingSummaries::getId, is(hearing.getId()))
                                .with(HearingSummaries::getHearingDays, first(isBean(HearingDay.class)
                                        .with(HearingDay::getIsCancelled, is(expectedHearingDay))
                                ))))
        );
    }

    private List<CrackedIneffectiveVacatedTrialType> buildTrialTypes(final UUID reasonId, final String reasonCode, final String trialType) {
        List<CrackedIneffectiveVacatedTrialType> trialList = new ArrayList<>();
        trialList.add(new CrackedIneffectiveVacatedTrialType(reasonId, reasonCode, trialType,"On the date of trial the prosecution "
                ,"On the date of trial the prosecution agrees to the defendant being bound-over having previously rejected the offer.", null));
        return trialList;
    }

    private void updateDefendantAndChangeVerdict(CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper) {
        updateDefendantAttendance(initiateHearingCommandHelper);

        final Hearing hearing = initiateHearingCommandHelper.getHearing();

        stubCourtCentre(hearing);

        ProsecutionCounselIT.createFirstProsecutionCounsel(initiateHearingCommandHelper);

        changeVerdict(initiateHearingCommandHelper, fromString(VERDICT_TYPE_GUILTY_ID), VERDICT_TYPE_GUILTY_CODE);
    }

    private void updateDefendantAttendance(final CommandHelpers.InitiateHearingCommandHelper hearingOne) {
        final UUID hearingId = hearingOne.getHearingId();
        final UUID defendantId = hearingOne.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getId();
        final LocalDate dateOfAttendance = hearingOne.getHearing().getHearingDays().get(0).getSittingDay().toLocalDate();

        try (final EventListener publicDefendantAttendanceUpdated = listenFor("public.hearing.defendant-attendance-updated", DEFAULT_POLL_TIMEOUT_IN_MILLIS)
                .withFilter(convertStringTo(DefendantAttendanceUpdated.class, isBean(DefendantAttendanceUpdated.class)
                        .with(DefendantAttendanceUpdated::getHearingId, is(hearingId))
                        .with(DefendantAttendanceUpdated::getDefendantId, is(defendantId))
                        .with(DefendantAttendanceUpdated::getAttendanceDay, isBean(AttendanceDay.class)
                                .with(AttendanceDay::getDay, is(dateOfAttendance))
                                .with(AttendanceDay::getAttendanceType, is(AttendanceType.IN_PERSON)))))) {


            h(UseCases.updateDefendantAttendance(getRequestSpec(), updateDefendantAttendanceTemplate(hearingId, defendantId, dateOfAttendance, AttendanceType.IN_PERSON)));

            publicDefendantAttendanceUpdated.waitFor();
        }
    }

    private void stubCourtCentre(final Hearing hearing) {
        final CourtCentre courtCentre = hearing.getCourtCentre();
        hearing.getProsecutionCases().forEach(prosecutionCase -> stubLjaDetails(courtCentre, prosecutionCase.getProsecutionCaseIdentifier().getProsecutionAuthorityId()));
        stubGetReferenceDataCourtRooms(courtCentre, hearing.getHearingLanguage(), ouId3, ouId4);
    }

    private CommandHelpers.UpdateVerdictCommandHelper changeVerdict(final CommandHelpers.InitiateHearingCommandHelper hearingOne, final UUID verdictTypeId, final String verdictCode) {

        final EventListener verdictUpdatedPublicEventListener = listenFor("public.hearing.verdict-updated")
                .withFilter(isJson(allOf(
                        withJsonPath("$.hearingId", is(hearingOne.getHearingId().toString()))
                        ))
                );

        final CommandHelpers.UpdateVerdictCommandHelper updateVerdict = h(UseCases.updateVerdict(getRequestSpec(), hearingOne.getHearingId(),
                TestTemplates.UpdateVerdictCommandTemplates.updateVerdictTemplate(
                        hearingOne.getHearingId(),
                        hearingOne.getFirstOffenceForFirstDefendantForFirstCase().getId(),
                        TestTemplates.VerdictCategoryType.GUILTY,
                        verdictTypeId,
                        verdictCode)
        ));

        verdictUpdatedPublicEventListener.waitFor();
        return updateVerdict;
    }

    private void shareResults(AllNowsReferenceDataHelper allNows, CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper, SaveDraftResultCommand saveDraftResultCommand, LocalDate orderDate) {
        setResultLine(saveDraftResultCommand.getTarget().getResultLines().get(0),
                findPrompt(setupResultDefinitionsReferenceData(orderDate, singletonList(allNows.getFirstPrimaryResultDefinitionId())),
                        allNows.getFirstPrimaryResultDefinitionId()), allNows.getFirstPrimaryResultDefinitionId(), orderDate);


        UseCases.shareResults(getRequestSpec(), initiateHearingCommandHelper.getHearingId(), with(
                basicShareResultsCommandTemplate(),
                command -> command.setCourtClerk(getCourtClerk())
        ), singletonList(saveDraftResultCommand.getTarget()));
    }

    private void setResultLine(final ResultLine resultLine, final uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt prompt, final UUID resultDefId, final LocalDate orderDate) {
        resultLine.setResultLineId(randomUUID());
        resultLine.setResultDefinitionId(resultDefId);
        resultLine.setOrderedDate(orderDate);
        resultLine.setPrompts(singletonList(Prompt.prompt()
                .withLabel(prompt.getLabel())
                .withFixedListCode("fixedListCode")
                .withValue("value1")
                .withWelshValue("wvalue1")
                .withId(prompt.getId())
                .build()));
    }

    private uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt findPrompt(final CommandHelpers.AllResultDefinitionsReferenceDataHelper refDataHelper, final UUID resultDefId) {
        final ResultDefinition resultDefinition =
                refDataHelper.it().getResultDefinitions().stream()
                        .filter(rd -> rd.getId().equals(resultDefId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("invalid test data")
                        );
        return resultDefinition.getPrompts().get(0);
    }

    private CommandHelpers.AllResultDefinitionsReferenceDataHelper setupResultDefinitionsReferenceData(LocalDate referenceDate, List<UUID> resultDefinitionIds) {
        final String LISTING_OFFICER_USER_GROUP = "Listing Officer";

        final CommandHelpers.AllResultDefinitionsReferenceDataHelper allResultDefinitions = h(AllResultDefinitions.allResultDefinitions()
                .setResultDefinitions(
                        resultDefinitionIds.stream().map(
                                resultDefinitionId ->
                                        ResultDefinition.resultDefinition()
                                                .setId(resultDefinitionId)
                                                .setRank(1)
                                                .setIsAvailableForCourtExtract(true)
                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                .setFinancial("Y")
                                                .setCategory(getCategoryForResultDefinition(resultDefinitionId))
                                                .setPrompts(singletonList(uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition.Prompt.prompt()
                                                                .setId(randomUUID())
                                                                .setMandatory(true)
                                                                .setLabel(STRING.next())
                                                                .setWelshLabel(STRING.next())
                                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                                                                .setReference(STRING.next())
                                                        )
                                                )
                                                .setLabel(STRING.next())
                                                .setWelshLabel(STRING.next())
                                                .setUserGroups(singletonList(LISTING_OFFICER_USER_GROUP))
                        ).collect(Collectors.toList())
                ));

        stubGetAllResultDefinitions(referenceDate, allResultDefinitions.it());
        return allResultDefinitions;
    }

    private DelegatedPowers getCourtClerk() {
        return DelegatedPowers.delegatedPowers()
                .withFirstName(STRING.next()).withLastName(STRING.next())
                .withUserId(randomUUID()).build();
    }
}
