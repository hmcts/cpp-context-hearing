package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static uk.gov.justice.core.courts.Target.target;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;
import static uk.gov.moj.cpp.hearing.test.TestTemplates.VariantDirectoryTemplates.standardVariantTemplate;


import java.util.Objects;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.Target;
import uk.gov.justice.core.courts.Target2;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.moj.cpp.hearing.command.nowsdomain.variants.Variant;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandPrompt;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.HearingState;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultDeletedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.DraftResultSaved;
import uk.gov.moj.cpp.hearing.domain.event.result.HearingVacatedRequested;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedSuccess;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResultsSharedDelegateTest {

    public static final String OFFENCE = "OFFENCE";
    private HearingAggregateMomento hearingAggregateMomento;
    private ResultsSharedDelegate resultsSharedDelegate;
    private HearingAggregate hearingAggregate;
    private HearingDaySharedResults hearingDaySharedResults;

    private static final String HEARING_VACATED_RESULT_DEFINITION_ID = "8cdc7be1-fc94-485b-83ee-410e710f6665";
    private static final String VACATED_TRIAL_REASON_ID = "05d90ca2-2009-40aa-b5a1-5b7720807e54";


    @BeforeEach
    public void setup() {
        hearingDaySharedResults = new HearingDaySharedResults();
        hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                        .withSittingDay(ZonedDateTime.now())
                        .build()))
                .build());
        resultsSharedDelegate = new ResultsSharedDelegate(hearingAggregateMomento);
        hearingAggregate = new HearingAggregate();
        setField(this.hearingAggregate, "resultsSharedDelegate", resultsSharedDelegate);
        setField(this.hearingAggregate, "momento", hearingAggregateMomento);
    }

    @Test
    public void shouldAddNewResultToNewAmendedResultsWhenAmendedDateIsAdded() {
        final UUID hearingId = randomUUID();
        final ZonedDateTime shared1Time = ZonedDateTime.now();
        final ZonedDateTime shared2Time = ZonedDateTime.now().plusMinutes(1);
        final UUID target1Id = randomUUID();
        final UUID target2Id = randomUUID();
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList(), LocalDate.now());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(shared2Time)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resharedResultLines, emptyList(), LocalDate.now());

        final List<Object> resharedEventCollection = resharedEventStreams.collect(toList());
        assertThat(resharedEventCollection.size(), is(1));
        final ResultsSharedV2 resharedResultsSharedV2 = (ResultsSharedV2) resharedEventCollection.get(0);
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().size(), is(1));
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).contains(resultLine2Id), is(true));
    }

    @Test
    public void shouldNotAddResultToNewAmendedResultsWhenResultIsAlreadySharedButNotAmended() {
        final UUID hearingId = randomUUID();
        final ZonedDateTime shared1Time = ZonedDateTime.now();
        final ZonedDateTime shared2Time = ZonedDateTime.now().plusMinutes(1);
        final UUID target1Id = randomUUID();
        final UUID target2Id = randomUUID();
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = getDelegatedPowers();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList(), LocalDate.now());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared2Time, resharedResultLines, emptyList(), LocalDate.now());

        final List<Object> resharedEventCollection = resharedEventStreams.collect(toList());
        assertThat(resharedEventCollection.size(), is(1));
        final ResultsSharedV2 resharedResultsSharedV2 = (ResultsSharedV2) resharedEventCollection.get(0);
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().size(), is(0));
    }

    @Test
    public void shouldAddResultToNewAmendedResultsWhenResultIsAlreadySharedAndAmendedDateIsChanged() {
        final UUID hearingId = randomUUID();
        final ZonedDateTime shared1Time = ZonedDateTime.now();
        final ZonedDateTime shared2Time = ZonedDateTime.now().plusMinutes(1);
        final ZonedDateTime shared3Time = ZonedDateTime.now().plusMinutes(3);
        final UUID target1Id = randomUUID();
        final UUID target2Id = randomUUID();
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList(), LocalDate.now());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared2Time, resharedResultLines, emptyList(), LocalDate.now());

        final List<Object> resharedEventCollection = resharedEventStreams.collect(toList());
        assertThat(resharedEventCollection.size(), is(1));
        final ResultsSharedV2 resharedResultsSharedV2 = (ResultsSharedV2) resharedEventCollection.get(0);
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().size(), is(0));

        final SharedResultsCommandResultLineV2 secondResharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 secondResharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(shared3Time)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> secondResharedResultLines = Arrays.asList(secondResharedResultLine1, secondResharedResultLine2);


        final Stream<Object> secondResharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared3Time, secondResharedResultLines, emptyList(), LocalDate.now());

        final List<Object> secondResharedEventCollection = secondResharedEventStreams.collect(toList());
        assertThat(secondResharedEventCollection.size(), is(1));
        final ResultsSharedV2 secondResharedResultsSharedV2 = (ResultsSharedV2) secondResharedEventCollection.get(0);
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().size(), is(1));
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).contains(resultLine2Id), is(true));
    }

    @Test
    public void shouldAddRemoveDuplicateApplications() {
        final UUID hearingId = randomUUID();
        final UUID duplicatedApplicationId = randomUUID();
        final CourtApplication courtApplicationOne = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        final CourtApplication courtApplicationTwo = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        final CourtApplication courtApplicationThree = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        hearingAggregateMomento.getHearing().setCourtApplications(Arrays.asList(courtApplicationOne, courtApplicationTwo, courtApplicationThree));
        final ZonedDateTime shared1Time = ZonedDateTime.now();
        final ZonedDateTime shared2Time = ZonedDateTime.now().plusMinutes(1);
        final ZonedDateTime shared3Time = ZonedDateTime.now().plusMinutes(3);
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList(), LocalDate.now());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getHearing().getCourtApplications().size(), is(1));

        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared2Time, resharedResultLines, emptyList(), LocalDate.now());

        final List<Object> resharedEventCollection = resharedEventStreams.collect(toList());
        assertThat(resharedEventCollection.size(), is(1));
        final ResultsSharedV2 resharedResultsSharedV2 = (ResultsSharedV2) resharedEventCollection.get(0);
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().size(), is(0));

        final SharedResultsCommandResultLineV2 secondResharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .build();
        final SharedResultsCommandResultLineV2 secondResharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(shared3Time)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> secondResharedResultLines = Arrays.asList(secondResharedResultLine1, secondResharedResultLine2);


        final Stream<Object> secondResharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared3Time, secondResharedResultLines, emptyList(), LocalDate.now());

        final List<Object> secondResharedEventCollection = secondResharedEventStreams.collect(toList());
        assertThat(secondResharedEventCollection.size(), is(1));
        final ResultsSharedV2 secondResharedResultsSharedV2 = (ResultsSharedV2) secondResharedEventCollection.get(0);
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().size(), is(1));
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).contains(resultLine2Id), is(true));
    }

    @Test
    public void shouldDeleteDraftResult() {
        UUID userId = UUID.randomUUID();
        UUID hearingId = UUID.randomUUID();
        LocalDate hearingDay = LocalDate.now();

        Stream stream = resultsSharedDelegate.deleteDraftResultV2(hearingId, hearingDay, userId);
        assertThat(stream.findFirst().get().getClass().getCanonicalName(), is(DraftResultDeletedV2.class.getCanonicalName()));

    }


    @Test
    public void shouldAddNewResultsForTheGivenDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccess= (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(1));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        assertNotNull(resultsSharedSuccess);

    }

    @Test
    public void shouldAddNewResultsForHearingDayWhichWasSharedAlready() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();


        Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));

        assertThat(resultsSharedV3.getTargets().size(), is(1));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));

        // Not previously shared hence no saved targets and result line status
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        assertThat(resultsSharedV3.getSavedTargets().size(), is(0));
        assertThat(resultsSharedV3.getCompletedResultLinesStatus().size(), is(0));

        DaysResultLinesStatusUpdated daysResultLinesStatusUpdated = DaysResultLinesStatusUpdated.builder()
                .withHearingDay(hearingDay)
                .withHearingId(hearingId)
                .withSharedResultLines(Arrays.asList(SharedResultLineId.builder()
                        .withSharedResultLineId(randomUUID())
                        .build()))
                .build();
        hearingAggregate.apply(resultsSharedV3);
        hearingAggregate.apply(daysResultLinesStatusUpdated);

        eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);
        eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));

        assertThat(resultsSharedV3.getTargets().size(), is(1));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));


        // Previously shared hence saved targets and result line status
        assertThat(resultsSharedV3.getIsReshare(), is(true));
        assertThat(resultsSharedV3.getSavedTargets().size(), is(1));
        assertThat(resultsSharedV3.getCompletedResultLinesStatus().size(), is(1));

    }

    @Test
    public void shouldAddMultipleDraftResultSavedThenMomentoShouldHaveMultipleTargets() {
        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.parse("2019-12-18");
        final Target target = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withHearingDay(hearingDay)
                .build();
        final Target target2 = target().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withHearingDay(hearingDay)
                .withResultLines(new ArrayList<>())
                .build();

        final DraftResultSaved draftResultSaved = new DraftResultSaved(target, HearingState.SHARED, randomUUID());
        final DraftResultSaved draftResultSaved1 = new DraftResultSaved(target2, HearingState.SHARED, randomUUID());
        resultsSharedDelegate.handleDraftResultSaved(draftResultSaved);
        resultsSharedDelegate.handleDraftResultSaved(draftResultSaved1);
        assertThat(hearingAggregateMomento.getMultiDayTargets().get(hearingDay).size(), is(2));
        assertThat(hearingAggregateMomento.getMultiDayTargets().get(hearingDay).get(target.getTargetId()), notNullValue());
        assertThat(hearingAggregateMomento.getMultiDayTargets().get(hearingDay).get(target2.getTargetId()), notNullValue());
    }

    @Test
    public void shouldFilterDuplicateApplications() {
        final UUID duplicatedApplicationId = randomUUID();
        final CourtApplication courtApplicationOne = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        final CourtApplication courtApplicationTwo = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        final CourtApplication courtApplicationThree = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        hearingAggregateMomento.getHearing().setCourtApplications(Arrays.asList(courtApplicationOne, courtApplicationTwo, courtApplicationThree));
        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final Target2 target = Target2.target2().withTargetId(randomUUID())
                .withApplicationId(randomUUID())
                .withHearingId(hearingId)
                .withResultLines(new ArrayList<>())
                .withHearingDay(hearingDay)
                .build();
        final Map<LocalDate, Map<UUID, Target2>> multiDayTargets = new HashMap<>();
        final Map<UUID, Target2> variantTarget = new HashMap<>();
        variantTarget.put(randomUUID(),target);
        multiDayTargets.put(hearingDay, variantTarget);
        final List<Variant> variantDirectory = singletonList(
                standardVariantTemplate(randomUUID(), hearingId, randomUUID()));
        hearingAggregateMomento.setVariantDirectory(variantDirectory);
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccess= (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearing().getCourtApplications().size(), is(1));
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(1));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        assertNotNull(resultsSharedSuccess);
    }

    @Test
    public void shouldFilterDuplicateApplicationsOnlyKeepingUniqueOnes() {
        final UUID duplicatedApplicationId = randomUUID();
        final UUID uniqueApplicationId = randomUUID();
        final CourtApplication courtApplicationOne = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        final CourtApplication courtApplicationTwo = CourtApplication.courtApplication()
                .withId(uniqueApplicationId)
                .build();
        final CourtApplication courtApplicationThree = CourtApplication.courtApplication()
                .withId(duplicatedApplicationId)
                .build();
        hearingAggregateMomento.getHearing().setCourtApplications(Arrays.asList(courtApplicationOne, courtApplicationTwo, courtApplicationThree));
        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccessV3= (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearing().getCourtApplications().size(), is(2));
        assertThat(resultsSharedV3.getHearing().getCourtApplications().get(0).getId(), is(duplicatedApplicationId));
        assertThat(resultsSharedV3.getHearing().getCourtApplications().get(1).getId(), is(uniqueApplicationId));
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(1));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        assertNotNull(resultsSharedSuccessV3);
    }

    @Test
    public void shouldRaiseHearingVacatedRequestedEvent() {


        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final UUID APPLICATION_ID = randomUUID();
        final UUID HEARING_ID_TO_BE_VACATED = randomUUID();


        Hearing hearing = hearingAggregateMomento.getHearing();
        CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(APPLICATION_ID)
                .withHearingIdToBeVacated(HEARING_ID_TO_BE_VACATED)
                .build();
        hearing.setCourtApplications(Arrays.asList(courtApplication));


        final String REASON = "random reason";
        final SharedResultsCommandPrompt prompt = new SharedResultsCommandPrompt(UUID.fromString(VACATED_TRIAL_REASON_ID), null, null, REASON, null, null, "reasonForVacatingTrial");


        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(singletonList(prompt))
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.fromString(HEARING_VACATED_RESULT_DEFINITION_ID))
                .withApplicationIds(APPLICATION_ID)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(3));

        final ResultsSharedSuccess resultsSharedSuccessV3= (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(2));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));

        final HearingVacatedRequested hearingVacatedRequested = (HearingVacatedRequested) eventCollection.get(2);
        assertThat(hearingVacatedRequested.getHearingIdToBeVacated(), is(HEARING_ID_TO_BE_VACATED));
        assertThat(hearingVacatedRequested.getVacatedTrialReasonShortDesc(), is(REASON));
        assertNotNull(resultsSharedSuccessV3);


    }

    @Test
    public void shouldNotRaiseHearingVacatedRequestedEventWhitOutApplication() {


        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final UUID APPLICATION_ID = randomUUID();


        final String REASON = "random reason";
        final SharedResultsCommandPrompt prompt = new SharedResultsCommandPrompt(UUID.fromString(VACATED_TRIAL_REASON_ID),null,null, REASON,null,null,"reasonForVacatingTrial");


        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(Arrays.asList(prompt))
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.fromString(HEARING_VACATED_RESULT_DEFINITION_ID))
                .withApplicationIds(APPLICATION_ID)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccess = (ResultsSharedSuccess) eventCollection.get(0);
        assertThat(resultsSharedSuccess.getHearingId(), is(hearingId));

        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(2));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));


    }

    @Test
    public void shouldNotRaiseHearingVacatedRequestedEvent() {


        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();
        final UUID APPLICATION_ID = randomUUID();
        final UUID HEARING_ID_TO_BE_VACATED = randomUUID();


        Hearing hearing = hearingAggregateMomento.getHearing();
        CourtApplication courtApplication = CourtApplication.courtApplication()
                .withId(APPLICATION_ID)
                .withHearingIdToBeVacated(HEARING_ID_TO_BE_VACATED)
                .build();
        hearing.setCourtApplications(Arrays.asList(courtApplication));


        final String REASON = "random reason";
        final SharedResultsCommandPrompt prompt = new SharedResultsCommandPrompt(UUID.fromString(VACATED_TRIAL_REASON_ID), null, null, REASON, null, null, "reasonForVacatingTrial");


        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(Arrays.asList(prompt))
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.fromString(UUID.randomUUID().toString()))
                .withApplicationIds(APPLICATION_ID)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccessV3 = (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(2));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        assertNotNull(resultsSharedSuccessV3);

    }

    @Test
    public void shouldSetHearingDaySharedResultsV3TrueIfHearingDaySameAsSittingDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.now();

        final List<HearingDay> hearingDays = new ArrayList<>();
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build());
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(1)).build());
        hearingAggregateMomento.getHearing().setHearingDays(hearingDays);

        final ZonedDateTime sharedTime = ZonedDateTime.now();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, getDelegatedPowers(), sharedTime, getSharedResultsCommandResultLineV2s(sharedTime), emptyList(), getYouthCourt(), hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccessV3 = (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getHearing().getHearingDays().get(0).getHasSharedResults(), is(true));
        assertThat(resultsSharedV3.getHearing().getHearingDays().get(1).getHasSharedResults(), is(false));
        assertNotNull(resultsSharedSuccessV3);
    }

    @Test
    public void shouldSetHearingDaySharedResultsV3TrueIfHearingDayDifferentToSittingDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.now().minusDays(10);

        final List<HearingDay> hearingDays = new ArrayList<>();
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build());
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(1)).build());
        hearingAggregateMomento.getHearing().setHearingDays(hearingDays);

        final ZonedDateTime sharedTime = ZonedDateTime.now();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, getDelegatedPowers(), sharedTime, getSharedResultsCommandResultLineV2s(sharedTime), emptyList(), getYouthCourt(), hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccess = (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getHearing().getHearingDays().get(0).getHasSharedResults(), is(false));
        assertThat(resultsSharedV3.getHearing().getHearingDays().get(1).getHasSharedResults(), is(false));
        assertNotNull(resultsSharedSuccess);
    }

    @Test
    public void shouldSetHearingDaySharedResultsV2TrueIfHearingDaySameAsSittingDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.now();

        final List<HearingDay> hearingDays = new ArrayList<>();
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build());
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(1)).build());
        hearingAggregateMomento.getHearing().setHearingDays(hearingDays);

        final ZonedDateTime sharedTime = ZonedDateTime.now();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, getDelegatedPowers(), sharedTime, getSharedResultsCommandResultLineV2s(sharedTime), emptyList(), hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));

        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getHearing().getHearingDays().get(0).getHasSharedResults(), is(true));
        assertThat(resultsSharedV2.getHearing().getHearingDays().get(1).getHasSharedResults(), is(false));
    }

    @Test
    public void shouldSetHearingDaySharedResultsV2TrueIfHearingDayDifferentToSittingDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.now().minusDays(10);

        final List<HearingDay> hearingDays = new ArrayList<>();
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now()).build());
        hearingDays.add(HearingDay.hearingDay().withSittingDay(ZonedDateTime.now().minusDays(1)).build());
        hearingAggregateMomento.getHearing().setHearingDays(hearingDays);

        final ZonedDateTime sharedTime = ZonedDateTime.now();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, getDelegatedPowers(), sharedTime, getSharedResultsCommandResultLineV2s(sharedTime), emptyList(), hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));

        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getHearing().getHearingDays().get(0).getHasSharedResults(), is(false));
        assertThat(resultsSharedV2.getHearing().getHearingDays().get(1).getHasSharedResults(), is(false));
    }

    @Test
    public void shouldSetShadowOffenceFlagWhenTargetBelongsApplication() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withOffenceId(randomUUID())
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(UUID.randomUUID())
                .withShadowListed(true)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withApplicationIds(randomUUID())
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withResultDefinitionId(UUID.randomUUID())
                .withShadowListed(true)
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(2));

        final ResultsSharedSuccess resultsSharedSuccess= (ResultsSharedSuccess) eventCollection.get(0);
        final ResultsSharedV3 resultsSharedV3 = (ResultsSharedV3) eventCollection.get(1);
        assertThat(resultsSharedV3.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getTargets().size(), is(2));
        assertThat(resultsSharedV3.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV3.getIsReshare(), is(false));
        int applicationResultIndex;
        int caseResulIndex;
        if(Objects.nonNull(resultsSharedV3.getTargets().get(1).getApplicationId())){
            applicationResultIndex = 1;
            caseResulIndex = 0;
        } else{
            applicationResultIndex = 0;
            caseResulIndex = 1;
        }
        assertThat(resultsSharedV3.getTargets().get(applicationResultIndex).getShadowListed(), is(false));
        assertThat(resultsSharedV3.getTargets().get(applicationResultIndex).getResultLines().get(0).getShadowListed(), is(false));
        assertThat(resultsSharedV3.getTargets().get(caseResulIndex).getShadowListed(), is(true));
        assertThat(resultsSharedV3.getTargets().get(caseResulIndex).getResultLines().get(0).getShadowListed(), is(true));
        assertNotNull(resultsSharedSuccess);

    }

    private YouthCourt getYouthCourt() {
        return YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();
    }

    private DelegatedPowers getDelegatedPowers() {
        return DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
    }

    private List<SharedResultsCommandResultLineV2> getSharedResultsCommandResultLineV2s(final ZonedDateTime sharedTime) {
        final UUID resultLine1Id = randomUUID();
        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel(OFFENCE)
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withResultDefinitionId(randomUUID())
                .build();


        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1);
        return resultLines;
    }

}
