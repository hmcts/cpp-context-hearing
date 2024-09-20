package uk.gov.moj.cpp.hearing.it;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.core.courts.IndicatedPleaValue.INDICATED_NOT_GUILTY;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.INTEGER;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.PAST_LOCAL_DATE;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.moj.cpp.hearing.it.Queries.getDraftResultsForHearingDayPollForMatch;
import static uk.gov.moj.cpp.hearing.it.Queries.waitForFewSeconds;
import static uk.gov.moj.cpp.hearing.it.UseCases.initiateHearing;
import static uk.gov.moj.cpp.hearing.it.Utilities.listenFor;
import static uk.gov.moj.cpp.hearing.it.Utilities.makeCommand;
import static uk.gov.moj.cpp.hearing.steps.HearingStepDefinitions.givenAUserHasLoggedInAsACourtClerk;
import static uk.gov.moj.cpp.hearing.test.CommandHelpers.h;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.InitiateHearingCommandTemplates.standardInitiateHearingTemplateWithMultidayHearing;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveDraftResultCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.SaveDraftResultsCommandTemplates.saveMultipleDraftResultsCommandTemplate;
import static uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher.isBean;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_POLL_TIMEOUT_IN_SEC;
import static uk.gov.moj.cpp.hearing.utils.RestUtils.DEFAULT_WAIT_TIME_IN_SEC;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.IndicatedPleaValue;
import uk.gov.justice.core.courts.Offence;
import uk.gov.justice.core.courts.Target;
import uk.gov.moj.cpp.hearing.command.initiate.InitiateHearingCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveDraftResultCommand;
import uk.gov.moj.cpp.hearing.command.result.SaveMultipleDaysResultsCommand;
import uk.gov.moj.cpp.hearing.event.PublicHearingDraftResultSaved;
import uk.gov.moj.cpp.hearing.query.view.response.hearingresponse.TargetListResponse;
import uk.gov.moj.cpp.hearing.test.CommandHelpers;
import uk.gov.moj.cpp.hearing.test.CommandHelpers.InitiateHearingCommandHelper;
import uk.gov.moj.cpp.hearing.test.CoreTestTemplates;
import uk.gov.moj.cpp.hearing.test.matchers.BeanMatcher;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S2699")
@Disabled("Temporarily disabled as Feature Toggle tests are not working on Jenkins master pipeline")
public class AmendAndReshareIT extends AbstractIT {

    private static final String PUBLIC_HEARING_DRAFT_RESULT_SAVED = "public.hearing.draft-result-saved";
    private static final String PUBLIC_HEARING_MULTIPLE_DRAFT_RESULTS_SAVED = "public.hearing.multiple-draft-results-saved";

    @Test
    public void testEmptyDraftResultWhenNoDraftResultSavedForSingleDayHearing() {

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(UseCases.initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), LocalDate.now().toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, is(empty())));
    }

    @Test
    public void testEmptyDraftResultWhenNoDraftResultSavedForMultiDayHearing() {

        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplateWithMultidayHearing()));

        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), PAST_LOCAL_DATE.next().toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, is(empty())));
    }

    @Test
    public void shouldSaveDraftResultForSingleDay() {
        waitForFewSeconds(DEFAULT_WAIT_TIME_IN_SEC);
        LocalDate orderDate = LocalDate.now();

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), standardInitiateHearingTemplate()));

        SaveDraftResultCommand saveSingleDayDraftResultCommand = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDate, LocalDate.now());

        final List<Target> targets = saveDraftResults(saveSingleDayDraftResultCommand, orderDate);

        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), orderDate.toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingDay(), targets.get(0).getHearingDay())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getTargetId(), targets.get(0).getTargetId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDraftResult(), targets.get(0).getDraftResult())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingId(), targets.get(0).getHearingId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDefendantId(), targets.get(0).getDefendantId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getOffenceId(), targets.get(0).getOffenceId())
        );

    }

    @SuppressWarnings("squid:S1607")
    @Test
    public void shouldSaveDraftResultForMultiDay() {
        waitForFewSeconds(DEFAULT_WAIT_TIME_IN_SEC);
        final InitiateHearingCommand initiateHearingCommand = standardInitiateHearingTemplateWithMultidayHearing();
        final CommandHelpers.InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearingCommand));

        final Hearing hearing = initiateHearingCommandHelper.getHearing();
        final List<HearingDay> hearingDays = hearing.getHearingDays();

        final LocalDate orderDateDay1 = hearingDays.get(0).getSittingDay().toLocalDate();
        final LocalDate orderDateDay2 = hearingDays.get(1).getSittingDay().toLocalDate();

        final SaveDraftResultCommand saveMultiDayDraftResultCommandDay1 = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDateDay1, orderDateDay1);
        final List<Target> day1Target = saveDraftResults(saveMultiDayDraftResultCommandDay1, orderDateDay1);

        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), orderDateDay1.toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingDay(), day1Target.get(0).getHearingDay())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getTargetId(), day1Target.get(0).getTargetId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDraftResult(), day1Target.get(0).getDraftResult())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingId(), day1Target.get(0).getHearingId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDefendantId(), day1Target.get(0).getDefendantId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getOffenceId(), day1Target.get(0).getOffenceId())
        );

        waitForFewSeconds(DEFAULT_WAIT_TIME_IN_SEC);
        final SaveDraftResultCommand saveMultiDayDraftResultCommandDay2 = saveDraftResultCommandTemplate(initiateHearingCommandHelper.it(), orderDateDay2, orderDateDay2);
        final List<Target> day2Target = saveDraftResults(saveMultiDayDraftResultCommandDay2, orderDateDay2);
        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), orderDateDay2.toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingDay(), day2Target.get(0).getHearingDay())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getTargetId(), day2Target.get(0).getTargetId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDraftResult(), day2Target.get(0).getDraftResult())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getHearingId(), day2Target.get(0).getHearingId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getDefendantId(), day2Target.get(0).getDefendantId())
                .withValue(targetListResponse -> targetListResponse.getTargets().get(0).getOffenceId(), day2Target.get(0).getOffenceId())
        );

    }

    @Test
    public void shouldSaveMultipleDraftResultsForSingleDay() throws Exception {

        LocalDate orderDate = LocalDate.now();

        final InitiateHearingCommand initiateHearing = standardInitiateHearingTemplate();

        addOffenceToInitiateHearingCommand(initiateHearing, INDICATED_NOT_GUILTY);

        final InitiateHearingCommandHelper initiateHearingCommandHelper = h(initiateHearing(getRequestSpec(), initiateHearing));

        SaveMultipleDaysResultsCommand saveSingleDayMultipleDraftResultCommand = saveMultipleDraftResultsCommandTemplate(initiateHearingCommandHelper.it(), orderDate, orderDate);

        final List<Target> targets = saveMultipleDraftResults(saveSingleDayMultipleDraftResultCommand, orderDate);

        final List<UUID> offenceIdList = Arrays.asList(targets.get(0).getOffenceId(), targets.get(1).getOffenceId());
        final List<UUID> targetIdList = Arrays.asList(targets.get(0).getTargetId(), targets.get(1).getTargetId());

        getDraftResultsForHearingDayPollForMatch(initiateHearingCommandHelper.getHearingId(), orderDate.toString(), DEFAULT_POLL_TIMEOUT_IN_SEC, isBean(TargetListResponse.class)
                .with(TargetListResponse::getTargets, hasSize(2))
                .with(targetListResponse ->
                        targetListResponse.getTargets().stream().map(t -> t.getOffenceId()).collect(Collectors.toList()).containsAll(offenceIdList), is(true))
                .with(targetListResponse ->
                        targetListResponse.getTargets().stream().map(t -> t.getTargetId()).collect(Collectors.toList()).containsAll(targetIdList), is(true))
                .with(targetListResponse ->
                        targetListResponse.getTargets().stream().filter(t -> t.getDraftResult().equals(targets.get(0).getDraftResult())).collect(Collectors.toList()), hasSize(2))
                .with(targetListResponse ->
                        targetListResponse.getTargets().stream().filter(t -> t.getDefendantId().equals(targets.get(0).getDefendantId())).collect(Collectors.toList()), hasSize(2))
                .with(targetListResponse ->
                        targetListResponse.getTargets().stream().filter(t -> t.getHearingDay().equals(targets.get(0).getHearingDay())).collect(Collectors.toList()), hasSize(2))

        );
    }

    private List<Target> saveDraftResults(SaveDraftResultCommand saveSingleDayDraftResultCommand, LocalDate orderDate) {

        final List<Target> targets = new ArrayList<>();
        targets.add(saveSingleDayDraftResultCommand.getTarget());

        saveDraftResultForHearingDay(saveSingleDayDraftResultCommand);

        return targets;
    }

    private List<Target> saveMultipleDraftResults(SaveMultipleDaysResultsCommand saveDraftResultsCommand, LocalDate orderDate) throws IOException {

        final List<Target> targets = saveDraftResultsCommand.getTargets();

        saveMultipleDraftResultsForHearingDay(saveDraftResultsCommand);

        return targets;
    }

    private void saveDraftResultForHearingDay(final SaveDraftResultCommand saveDraftResultCommand) {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final Target target = saveDraftResultCommand.getTarget();

        final BeanMatcher<PublicHearingDraftResultSaved> beanMatcher = isBean(PublicHearingDraftResultSaved.class)
                .with(PublicHearingDraftResultSaved::getTargetId, is(target.getTargetId()))
                .with(PublicHearingDraftResultSaved::getHearingId, is(target.getHearingId()))
                .with(PublicHearingDraftResultSaved::getDefendantId, is(target.getDefendantId()))
                .with(PublicHearingDraftResultSaved::getOffenceId, is(target.getOffenceId()));

        final String expectedMetaDataContextUser = getLoggedInUser().toString();
        final String expectedMetaDataName = PUBLIC_HEARING_DRAFT_RESULT_SAVED;
        try (final Utilities.EventListener publicEventResulted = listenFor(PUBLIC_HEARING_DRAFT_RESULT_SAVED)
                .withFilter(beanMatcher, expectedMetaDataName, expectedMetaDataContextUser)) {

            makeCommand(getRequestSpec(), "hearing.save-days-draft-result")
                    .ofType("application/vnd.hearing.draft-result+json")
                    .withArgs(target.getHearingId(), target.getHearingDay())
                    .withPayload(target)
                    .executeSuccessfully();

            publicEventResulted.waitFor();
        }

    }

    private void saveMultipleDraftResultsForHearingDay(final SaveMultipleDaysResultsCommand saveDraftResultsCommand) throws IOException {
        givenAUserHasLoggedInAsACourtClerk(getLoggedInUser());

        final List<Target> targets = saveDraftResultsCommand.getTargets();

        try (final Utilities.EventListener publicEventMultipleResultsSaved = listenFor(PUBLIC_HEARING_MULTIPLE_DRAFT_RESULTS_SAVED)
                .withFilter(isJson(withJsonPath("$.numberOfTargets", is(2))))
        ) {

            final String eventPayloadString = getStringFromResource("hearing.save-days-draft-results.json")
                    .replaceAll("TARGET1_ID", targets.get(0).getTargetId().toString())
                    .replaceAll("TARGET2_ID", targets.get(1).getTargetId().toString())
                    .replaceAll("DEFENDANT_ID", targets.get(0).getDefendantId().toString())
                    .replaceAll("OFFENCE1_ID", targets.get(0).getOffenceId().toString())
                    .replaceAll("OFFENCE2_ID", targets.get(1).getOffenceId().toString())
                    .replaceAll("HEARING_DAY", saveDraftResultsCommand.getHearingDay().toString())
                    .replaceAll("HEARING_ID", saveDraftResultsCommand.getHearingId().toString());

            makeCommand(getRequestSpec(), "hearing.save-days-draft-results")
                    .ofType("application/vnd.hearing.draft-results+json")
                    .withArgs(saveDraftResultsCommand.getHearingId(), saveDraftResultsCommand.getHearingDay())
                    .withPayload(eventPayloadString)
                    .executeSuccessfully();

            publicEventMultipleResultsSaved.waitFor();
        }

    }

    private void addOffenceToInitiateHearingCommand(final InitiateHearingCommand initiateHearingCommand, final IndicatedPleaValue indicatedPleaValue) {
        final UUID offenceId = randomUUID();
        initiateHearingCommand.getHearing().getProsecutionCases().get(0).getDefendants().get(0).getOffences().add(Offence.offence()
                .withId(offenceId)
                .withStartDate(PAST_LOCAL_DATE.next())
                .withEndDate(PAST_LOCAL_DATE.next())
                .withArrestDate(PAST_LOCAL_DATE.next())
                .withChargeDate(PAST_LOCAL_DATE.next())
                .withOffenceDefinitionId(randomUUID())
                .withOffenceTitle(STRING.next())
                .withOffenceCode(STRING.next())
                .withOffenceTitleWelsh(STRING.next())
                .withOffenceLegislation(STRING.next())
                .withOffenceLegislationWelsh(STRING.next())
                .withIndicatedPlea(CoreTestTemplates.indicatedPlea(offenceId, indicatedPleaValue).build())
                .withNotifiedPlea(CoreTestTemplates.notifiedPlea(offenceId).build())
                .withWording(STRING.next())
                .withCount(INTEGER.next())
                .withWordingWelsh(STRING.next())
                .withModeOfTrial(STRING.next())
                .withOrderIndex(INTEGER.next()).build());
    }

}
