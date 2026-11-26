package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.DISM;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.G;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.REOPEN;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.RFSD;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.STAT_DEC;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.STAT_DEC_SJP;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.WDRN;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.resultsShared;

import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.SubjectType;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateAdded;
import uk.gov.moj.cpp.hearing.domain.event.ConvictionDateRemoved;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CHD-3370 - on REOPEN application offences, the conviction date follows a plea or verdict entered
 * at the hearing over the values cloned from the case, and the application result never changes them. The
 * rule is REOPEN only: CHD-3361 (STAT DEC) has no acceptance criterion for it. Each AC runs for an individual
 * and a company subject (CHD-3370 AC11). Not covered: CHD-3370 AC7 (new plea only), still TBC
 * in the ticket.
 */
class InheritedConvictionDateTest {

    @ParameterizedTest(name = "CHD-3370 AC8 guilty verdict on an inherited conviction date - {0}")
    @EnumSource(SubjectType.class)
    void guiltyVerdictOnInheritedValuesSetsConvictionDateToVerdictDate(final SubjectType subject) {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, subject, 1).initiate();

        final List<Object> events = fixture.enterVerdict(0, 0, GUILTY);

        final List<ConvictionDateAdded> added = ofType(events, ConvictionDateAdded.class);
        assertThat(added, hasSize(1));
        assertThat(added.get(0).getOffenceId(), is(fixture.offenceId(0, 0)));
        assertThat(added.get(0).getCourtApplicationId(), is(fixture.applicationId(0)));
        assertThat(added.get(0).getConvictionDate(), is(fixture.hearingDate()));

        final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationOffenceResult(0, 0))));
        assertThat(fixture.sharedApplicationOffence(shared, 0, 0).getConvictionDate(), is(fixture.hearingDate()));
    }

    @ParameterizedTest(name = "guilty verdict on a STAT DEC ({0}) inherited conviction date is not re-dated")
    @ValueSource(strings = {STAT_DEC, STAT_DEC_SJP})
    void guiltyVerdictOnAStatDecInheritedConvictionDateDoesNotReDateIt(final String applicationType) {
        // the rule is REOPEN only: CHD-3361 has no acceptance criterion for it
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(applicationType, SubjectType.INDIVIDUAL, 1).initiate();

        assertThat(ofType(fixture.enterVerdict(0, 0, GUILTY), ConvictionDateAdded.class), is(empty()));
    }

    @ParameterizedTest(name = "CHD-3370 AC10 Proved SJP offence, guilty plea and verdict entered - {0}")
    @EnumSource(SubjectType.class)
    void guiltyPleaAndVerdictOnAProvedSjpOffenceSetTheConvictionDateToTheHearingDate(final SubjectType subject) {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withClonedProvedSjpOffences()
                .withApplication(REOPEN, subject, 1).initiate();

        fixture.enterPlea(0, 0, GUILTY);
        fixture.enterVerdict(0, 0, GUILTY);

        assertThat(ofType(fixture.appliedEvents(), ConvictionDateRemoved.class), is(empty()));
        final Offence shared = fixture.sharedApplicationOffence(resultsShared(fixture.share(singletonList(fixture.applicationOffenceResult(0, 0)))), 0, 0);
        assertThat(shared.getPlea().getPleaValue(), is(GUILTY));
        assertThat(shared.getPlea().getOriginatingHearingId(), is(fixture.hearingId()));
        assertThat(shared.getVerdict().getVerdictType().getCategoryType(), is(GUILTY));
        assertThat(shared.getVerdict().getOriginatingHearingId(), is(fixture.hearingId()));
        assertThat(shared.getConvictionDate(), is(fixture.hearingDate()));
    }

    @ParameterizedTest(name = "CHD-3370 AC3 verdict on selected offences changes only those offences - {0}")
    @EnumSource(SubjectType.class)
    void verdictOnSelectedOffencesChangesOnlyThoseOffences(final SubjectType subject) {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, subject, 2).initiate();

        final List<Object> events = fixture.enterVerdict(0, 0, GUILTY);

        assertThat(ofType(events, ConvictionDateAdded.class).stream().map(ConvictionDateAdded::getOffenceId).collect(toList()),
                is(singletonList(fixture.offenceId(0, 0))));
        final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationOffenceResult(0, 0))));
        assertThat(fixture.sharedApplicationOffence(shared, 0, 0).getConvictionDate(), is(fixture.hearingDate()));
        assertUnchangedClonedValues(fixture, shared, 0, 1);
    }

    @ParameterizedTest(name = "CHD-3370 AC2 sentence-only change keeps cloned values - {0}")
    @EnumSource(SubjectType.class)
    void sentenceOnlyChangeKeepsClonedValues(final SubjectType subject) {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, subject, 1).initiate();

        final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationOffenceResult(0, 0))));

        assertUnchangedClonedValues(fixture, shared, 0, 0);
    }

    static Stream<Arguments> reopenApplicationResultsAndSubjects() {
        // the outcome results that would clear or restore a STAT DEC; any other result (ROPENED, ADJ) is a no-op
        // for every application type, so it adds nothing here
        return Stream.of(
                        Arguments.of("G", G), Arguments.of("RFSD", RFSD), Arguments.of("WDRN", WDRN), Arguments.of("DISM", DISM))
                .flatMap(result -> Stream.of(SubjectType.values())
                        .map(subject -> Arguments.of(result.get()[0], result.get()[1], subject)));
    }

    @ParameterizedTest(name = "CHD-3370 AC4 reopen resulted {0} with no plea or verdict keeps cloned values - {2}")
    @MethodSource("reopenApplicationResultsAndSubjects")
    void reopenResultedWithoutPleaOrVerdictKeepsClonedValues(final String shortCode, final UUID resultDefinitionId, final SubjectType subject) {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, subject, 1).initiate();

        final List<Object> events = fixture.share(singletonList(fixture.applicationResult(0, resultDefinitionId)));

        assertUnchangedClonedValues(fixture, resultsShared(events), 0, 0);
    }

    @Test
    void guiltyVerdictOnACaseOffenceWithAnEarlierConvictionDateDoesNotReDateIt() {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, SubjectType.INDIVIDUAL, 1)
                .withCaseOffenceCarryingValues().initiate();

        assertThat(ofType(fixture.enterCaseOffenceVerdict(GUILTY), ConvictionDateAdded.class), is(empty()));
    }

    @Test
    void secondGuiltyVerdictAtThisHearingDoesNotReDateAgain() {
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, SubjectType.INDIVIDUAL, 1).initiate();
        assertThat(ofType(fixture.enterVerdict(0, 0, GUILTY), ConvictionDateAdded.class), hasSize(1));

        assertThat(ofType(fixture.enterVerdict(0, 0, GUILTY), ConvictionDateAdded.class), is(empty()));
    }

    @Test
    void verdictPassedOnFromAnAssociatedHearingDoesNotReDateTheInheritedConvictionDate() {
        // CHD-3370 AC8: the legal adviser enters the verdict at this hearing (hearing.update-verdict); a verdict
        // passed on from an associated hearing is not that
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, SubjectType.INDIVIDUAL, 1).initiate();

        assertThat(ofType(fixture.inheritVerdict(0, 0, GUILTY), ConvictionDateAdded.class), is(empty()));
    }

    @Test
    void applicationOffenceSharingACaseOffenceIdLeavesTheCaseOffenceConvictionDateAlone() {
        // CHD-3370 AC8 (original case offence unaffected): when ids collide, the conviction-date event would
        // land on the case offence
        final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, SubjectType.INDIVIDUAL, 1)
                .withCaseOffenceCarryingValues().withApplicationOffenceSharingCaseOffenceId().initiate();

        assertThat(ofType(fixture.enterVerdict(0, 0, GUILTY), ConvictionDateAdded.class), is(empty()));
        final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.caseOffenceResult())));
        assertThat(fixture.sharedCaseOffence(shared).getConvictionDate(), is(fixture.originalConvictionDate()));
    }

    private static void assertUnchangedClonedValues(final ClonedValuesFixture fixture, final ResultsSharedV3 shared, final int application, final int offence) {
        final Offence sharedOffence = fixture.sharedApplicationOffence(shared, application, offence);
        assertThat(sharedOffence.getConvictionDate(), is(fixture.originalConvictionDate()));
        assertThat(sharedOffence.getPlea().getOriginatingHearingId(), is(fixture.originatingHearingId()));
        assertThat(sharedOffence.getPlea().getPleaValue(), is(GUILTY));
        assertThat(sharedOffence.getVerdict().getOriginatingHearingId(), is(fixture.originatingHearingId()));
        assertThat(sharedOffence.getVerdict().getVerdictType().getCategoryType(), is(GUILTY));
    }

    private static <T> List<T> ofType(final List<Object> events, final Class<T> type) {
        return events.stream().filter(type::isInstance).map(type::cast).collect(toList());
    }
}
