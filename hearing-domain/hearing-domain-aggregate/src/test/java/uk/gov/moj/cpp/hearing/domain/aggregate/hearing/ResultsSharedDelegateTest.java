package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;


import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static uk.gov.justice.services.test.utils.core.random.RandomGenerator.STRING;
import static uk.gov.justice.services.test.utils.core.reflection.ReflectionUtil.setField;

import uk.gov.justice.core.courts.DelegatedPowers;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.YouthCourt;
import uk.gov.moj.cpp.hearing.command.result.NewAmendmentResult;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.moj.cpp.hearing.command.result.SharedResultLineId;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.HearingAggregate;
import uk.gov.moj.cpp.hearing.domain.event.result.DaysResultLinesStatusUpdated;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV2;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;


public class ResultsSharedDelegateTest {

    private HearingAggregateMomento hearingAggregateMomento;
    private ResultsSharedDelegate resultsSharedDelegate;
    private HearingAggregate hearingAggregate;


    @Before
    public void setup() {

        hearingAggregateMomento = new HearingAggregateMomento();
        hearingAggregateMomento.setHearing(Hearing.hearing()
                .withHearingDays(Arrays.asList(HearingDay.hearingDay()
                        .withSittingDay(ZonedDateTime.now())
                        .build()))
                .build());
        resultsSharedDelegate = new ResultsSharedDelegate(hearingAggregateMomento);
        hearingAggregate = new HearingAggregate();
        setField(this.hearingAggregate, "resultsSharedDelegate",resultsSharedDelegate);
        setField(this.hearingAggregate, "momento",hearingAggregateMomento);
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
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(shared2Time)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resharedResultLines, emptyList());

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
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared2Time, resharedResultLines, emptyList());

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
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);
        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();
        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared1Time, resultLines, emptyList());

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));
        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getNewAmendmentResults().size(), is(2));
        assertThat(resultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).containsAll(Arrays.asList(resultLine1Id, resultLine2Id)), is(true));
        hearingAggregate.apply(resultsSharedV2);

        final SharedResultsCommandResultLineV2 resharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 resharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> resharedResultLines = Arrays.asList(resharedResultLine1, resharedResultLine2);


        final Stream<Object> resharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared2Time, resharedResultLines, emptyList());

        final List<Object> resharedEventCollection = resharedEventStreams.collect(toList());
        assertThat(resharedEventCollection.size(), is(1));
        final ResultsSharedV2 resharedResultsSharedV2 = (ResultsSharedV2) resharedEventCollection.get(0);
        assertThat(resharedResultsSharedV2.getNewAmendmentResults().size(), is(0));

        final SharedResultsCommandResultLineV2 secondResharedResultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();
        final SharedResultsCommandResultLineV2 secondResharedResultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(shared3Time)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();
        final List<SharedResultsCommandResultLineV2> secondResharedResultLines = Arrays.asList(secondResharedResultLine1, secondResharedResultLine2);


        final Stream<Object> secondResharedEventStreams = resultsSharedDelegate.shareResultsV2(hearingId, courtClerk, shared3Time, secondResharedResultLines, emptyList());

        final List<Object> secondResharedEventCollection = secondResharedEventStreams.collect(toList());
        assertThat(secondResharedEventCollection.size(), is(1));
        final ResultsSharedV2 secondResharedResultsSharedV2 = (ResultsSharedV2) secondResharedEventCollection.get(0);
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().size(), is(1));
        assertThat(secondResharedResultsSharedV2.getNewAmendmentResults().stream().map(NewAmendmentResult::getId).collect(toList()).contains(resultLine2Id), is(true));
    }

    @Test
    public void shouldAddNewResultsForTheGivenDay() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID target1Id = randomUUID();
        final UUID target2Id = randomUUID();
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();

        final Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        final List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));

        final ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getTargets().size(), is(2));
        assertThat(resultsSharedV2.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getTargets().get(1).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getIsReshare(), is(false));

    }

    @Test
    public void shouldAddNewResultsForHearingDayWhichWasSharedAlready() {

        final UUID hearingId = randomUUID();
        final LocalDate hearingDay = LocalDate.of(2022, 02, 02);
        final ZonedDateTime sharedTime = ZonedDateTime.now();
        final YouthCourt youthCourt = YouthCourt.youthCourt()
                .withYouthCourtId(randomUUID())
                .build();

        final UUID target1Id = randomUUID();
        final UUID target2Id = randomUUID();
        final UUID resultLine1Id = randomUUID();
        final UUID resultLine2Id = randomUUID();

        final SharedResultsCommandResultLineV2 resultLine1 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine1Id)
                .withTargetId(target1Id)
                .build();

        final SharedResultsCommandResultLineV2 resultLine2 = SharedResultsCommandResultLineV2.sharedResultsCommandResultLine()
                .withAmendmentDate(sharedTime)
                .withLevel("OFFENCE")
                .withPrompts(emptyList())
                .withResultLineId(resultLine2Id)
                .withTargetId(target2Id)
                .build();

        final List<SharedResultsCommandResultLineV2> resultLines = Arrays.asList(resultLine1, resultLine2);

        final DelegatedPowers courtClerk = DelegatedPowers.delegatedPowers().withFirstName(STRING.next())
                .withLastName(STRING.next())
                .withUserId(randomUUID())
                .build();


        Stream<Object> eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);

        List<Object> eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));

        ResultsSharedV2 resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getHearingDay(), is(hearingDay));

        assertThat(resultsSharedV2.getTargets().size(), is(2));
        assertThat(resultsSharedV2.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getTargets().get(1).getHearingDay(), is(hearingDay));

        // Not previously shared hence no saved targets and result line status
        assertThat(resultsSharedV2.getIsReshare(), is(false));
        assertThat(resultsSharedV2.getSavedTargets().size(), is(0));
        assertThat(resultsSharedV2.getCompletedResultLinesStatus().size(), is(0));

        DaysResultLinesStatusUpdated daysResultLinesStatusUpdated = DaysResultLinesStatusUpdated.builder()
                .withHearingDay(hearingDay)
                .withHearingId(hearingId)
                .withSharedResultLines(Arrays.asList(SharedResultLineId.builder()
                        .withSharedResultLineId(randomUUID())
                        .build()))
                .build();
        hearingAggregate.apply(resultsSharedV2);
        hearingAggregate.apply(daysResultLinesStatusUpdated);

        eventStreams = resultsSharedDelegate.shareResultForDay(hearingId, courtClerk, sharedTime, resultLines, emptyList(), youthCourt, hearingDay);
        eventCollection = eventStreams.collect(toList());
        assertThat(eventCollection.size(), is(1));

        resultsSharedV2 = (ResultsSharedV2) eventCollection.get(0);
        assertThat(resultsSharedV2.getHearingDay(), is(hearingDay));

        assertThat(resultsSharedV2.getTargets().size(), is(2));
        assertThat(resultsSharedV2.getTargets().get(0).getHearingDay(), is(hearingDay));
        assertThat(resultsSharedV2.getTargets().get(1).getHearingDay(), is(hearingDay));

        // Previously shared hence saved targets and result line status
        assertThat(resultsSharedV2.getIsReshare(), is(true));
        assertThat(resultsSharedV2.getSavedTargets().size(), is(2));
        assertThat(resultsSharedV2.getCompletedResultLinesStatus().size(), is(1));

    }
}
