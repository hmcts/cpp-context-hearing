package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.ClonedPleaDate.NONE;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.ClonedPleaDate.SAME_DAY_AS_APPLICATION;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.DISM;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.G;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.NOT_GUILTY;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.REOPEN;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.RFSD;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.STAT_DEC;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.STAT_DEC_SJP;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.STDEC;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.WDRN;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.clonedValuesAdjusted;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.indexOf;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.indexOfClonedValuesAdjusted;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.resultsShared;
import static uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.toJson;

import uk.gov.justice.core.courts.Offence;
import uk.gov.moj.cpp.hearing.command.result.SharedResultsCommandResultLineV2;
import uk.gov.moj.cpp.hearing.domain.aggregate.hearing.ClonedValuesFixture.SubjectType;
import uk.gov.moj.cpp.hearing.domain.event.result.ResultsSharedV3;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * CHD-3361 (STAT DEC) - at hearing.command.share-days-results the shared copy of a STAT DEC application's
 * cloned offences is cleared when granted and restored when not granted. Each system adjustment is recorded
 * as hearing.events.application-cloned-values-adjusted (no Jira AC), and aggregate state keeps the values as
 * entered (work order design D1).
 */
class ApplicationClonedValuesShareTest {

    @Nested
    class ClearOnGrant {

        static Stream<Arguments> grantResults() {
            return Stream.of(STAT_DEC, STAT_DEC_SJP).flatMap(type -> Stream.of(
                    Arguments.of(type, "G + STDEC", List.of(G, STDEC)),
                    Arguments.of(type, "G", List.of(G)),
                    Arguments.of(type, "STDEC", List.of(STDEC))));
        }

        @ParameterizedTest(name = "CHD-3361 AC2, AC7, AC15 {1} on {0} clears every cloned offence")
        @MethodSource("grantResults")
        void grantClearsClonedPleaVerdictAndConvictionDate(final String type, final String label, final List<UUID> results) {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(type, SubjectType.INDIVIDUAL, 2).initiate();
            final List<SharedResultsCommandResultLineV2> lines = new ArrayList<>();
            results.forEach(result -> lines.add(fixture.applicationResult(0, result)));

            final ResultsSharedV3 shared = resultsShared(fixture.share(lines));

            assertCleared(fixture.sharedApplicationOffence(shared, 0, 0));
            assertCleared(fixture.sharedApplicationOffence(shared, 0, 1));
        }

        @ParameterizedTest(name = "CHD-3361 AC4, scenario 3.1 adviser's not-guilty plea on offence 1 is kept on grant - {0}")
        @ValueSource(strings = {STAT_DEC, STAT_DEC_SJP})
        void enteredPleaIsKeptAndOtherClonedValuesClearedOnGrant(final String type) {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(type, SubjectType.INDIVIDUAL, 2).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G))));

            final Offence first = fixture.sharedApplicationOffence(shared, 0, 0);
            assertThat(first.getPlea().getPleaValue(), is(NOT_GUILTY));
            assertThat(first.getPlea().getOriginatingHearingId(), is(fixture.hearingId()));
            assertThat(first.getPlea().getPleaDate(), is(fixture.hearingDate()));
            assertThat(first.getVerdict(), is(nullValue()));
            assertThat(first.getConvictionDate(), is(nullValue()));
            assertCleared(fixture.sharedApplicationOffence(shared, 0, 1));
        }

        @ParameterizedTest(name = "CHD-3361 AC8 adjourned with no application result keeps cloned values - {0}")
        @ValueSource(strings = {STAT_DEC, STAT_DEC_SJP})
        void adjournedWithoutApplicationResultKeepsClonedValues(final String type) {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(type, SubjectType.INDIVIDUAL, 1).initiate();

            // ADJ result definition id is not in the work order; any non-outcome application result is a no-op.
            final List<Object> events = fixture.share(asList(fixture.applicationResult(0, randomUUID()), fixture.applicationOffenceResult(0, 0)));

            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(resultsShared(events), 0, 0));
        }

        @Test
        void caseOffencesAreNeverChanged() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1)
                    .withCaseOffenceCarryingValues().initiate();

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G))));

            final Offence caseOffence = fixture.sharedCaseOffence(shared);
            assertThat(caseOffence.getPlea(), is(notNullValue()));
            assertThat(caseOffence.getVerdict(), is(notNullValue()));
            assertThat(caseOffence.getConvictionDate(), is(fixture.originalConvictionDate()));
        }

        @ParameterizedTest(name = "CHD-3361 AC11, AC12 only the granted STAT DEC subject's cloned offences change - {0}")
        @EnumSource(value = SubjectType.class)
        void onlyTheGrantedApplicationsOffencesChange(final SubjectType subject) {
            final SubjectType otherSubject = subject == SubjectType.INDIVIDUAL ? SubjectType.COMPANY : SubjectType.INDIVIDUAL;
            final ClonedValuesFixture fixture = new ClonedValuesFixture()
                    .withApplication(STAT_DEC, subject, 1)
                    .withApplication(STAT_DEC, otherSubject, 1)
                    .initiate();

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G))));

            assertCleared(fixture.sharedApplicationOffence(shared, 0, 0));
            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(shared, 1, 0));
        }

        @Test
        void multiDayGrantSharedOnDayOneStillClearsOnDayTwoWithoutANewEvent() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1)
                    .withHearingDays(2).initiate();
            fixture.share(0, singletonList(fixture.applicationResult(0, G)));

            final List<Object> dayTwoEvents = fixture.share(1, singletonList(fixture.caseOffenceResult()));

            final ResultsSharedV3 dayTwo = resultsShared(dayTwoEvents);
            assertThat(dayTwo.getHearingDay(), is(fixture.hearingDay(1)));
            assertCleared(fixture.sharedApplicationOffence(dayTwo, 0, 0));
            assertThat(clonedValuesAdjusted(dayTwoEvents), is(empty()));
        }

        @Test
        void pleaDatedOnTheApplicationReceivedDateIsTreatedAsHearingEnteredAndKept() {
            // work order OQ-6 working assumption: same-day boundary is hearing-entered (rule is strictly "earlier than").
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withClonedPleaDate(SAME_DAY_AS_APPLICATION)
                    .withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G))));

            final Offence offence = fixture.sharedApplicationOffence(shared, 0, 0);
            assertThat(offence.getPlea(), is(notNullValue()));
            assertThat(offence.getVerdict(), is(nullValue()));
        }

        @Test
        void datelessMigratedPleaIsTreatedAsClonedAndCleared() {
            // work order OQ-7 working assumption: a dateless plea from another hearing is cloned.
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withClonedPleaDate(NONE)
                    .withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G))));

            assertCleared(fixture.sharedApplicationOffence(shared, 0, 0));
        }

        @Test
        void aggregateKeepsTheValuesAsEnteredAfterAGrantIsShared() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 2).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);

            fixture.share(singletonList(fixture.applicationResult(0, G)));

            assertAggregateStateAsEntered(fixture);
            assertAggregateStateAsEntered(fixture.replayed());
        }

        private void assertAggregateStateAsEntered(final ClonedValuesFixture fixture) {
            final Offence entered = fixture.aggregateApplicationOffence(0, 0);
            assertThat(entered.getPlea().getPleaValue(), is(NOT_GUILTY));
            assertThat(entered.getVerdict(), is(notNullValue()));
            assertClonedValuesPresent(fixture, fixture.aggregateApplicationOffence(0, 1));
        }
    }

    @Nested
    class RestoreWhenNotGranted {

        static Stream<Arguments> notGrantedResults() {
            return Stream.of(Arguments.of("RFSD", RFSD), Arguments.of("DISM", DISM), Arguments.of("WDRN", WDRN));
        }

        @ParameterizedTest(name = "CHD-3361 AC5, AC6 {0} restores the case values on every cloned offence")
        @MethodSource("notGrantedResults")
        void notGrantedRestoresClonedValuesOverAnEnteredPlea(final String label, final UUID result) {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 2).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, result))));

            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(shared, 0, 0));
            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(shared, 0, 1));
        }

        @ParameterizedTest(name = "CHD-3361 AC14 grant amended to RFSD and reshared restores cloned values and records it - {0}")
        @ValueSource(strings = {STAT_DEC, STAT_DEC_SJP})
        void grantAmendedToRefusalRestoresClonedValuesAndAppendsOneRestoredEvent(final String type) {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(type, SubjectType.INDIVIDUAL, 1).initiate();
            final UUID grantLineId = randomUUID();
            fixture.share(singletonList(fixture.applicationResult(0, G, grantLineId, false, null)));

            final List<Object> events = fixture.share(amendGrantToRefusal(fixture, grantLineId));

            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(resultsShared(events), 0, 0));
            final List<JsonNode> adjusted = clonedValuesAdjusted(events);
            assertThat(adjusted, hasSize(1));
            assertThat(adjusted.get(0).path("outcome").asText(), is("NOT_GRANTED"));
            assertThat(adjusted.get(0).path("action").asText(), is("RESTORED"));
            assertThat(indexOfClonedValuesAdjusted(events), lessThan(indexOf(events, ResultsSharedV3.class)));
        }

        @Test
        void reshareOfARefusalWithNoChangeIsIdentical() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 2).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);
            final List<SharedResultsCommandResultLineV2> refusal = singletonList(fixture.applicationResult(0, RFSD));

            final ResultsSharedV3 first = resultsShared(fixture.share(refusal));
            final ResultsSharedV3 second = resultsShared(fixture.share(refusal));

            assertThat(toJson(second.getHearing().getCourtApplications()), is(toJson(first.getHearing().getCourtApplications())));
        }

        @Test
        void offenceAddedToTheApplicationAfterInitiationIsRestoredWhenNotGranted() {
            // CHD-3361 AC6 (review finding): the restore covers offences that join the application after initiation
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final int added = fixture.addClonedOffenceToApplication(0);
            fixture.enterPlea(0, added, NOT_GUILTY);

            final ResultsSharedV3 shared = resultsShared(fixture.share(singletonList(fixture.applicationResult(0, RFSD))));

            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(shared, 0, added));
        }

        @Test
        void grantedApplicationJoiningAtShareKeepsItsValuesAndIsRestoredWhenAmendedToRefusal() {
            // CHD-3361 AC14 (review finding M2): an application that first arrives with the share has no as-loaded snapshot
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplicationJoiningAtShare(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final UUID grantLineId = randomUUID();
            assertCleared(fixture.sharedApplicationOffence(resultsShared(fixture.share(singletonList(fixture.applicationResult(0, G, grantLineId, false, null)))), 0, 0));

            // the aggregate holds the values the application arrived with, live and rebuilt from the events
            assertClonedValuesPresent(fixture, fixture.aggregateApplicationOffence(0, 0));
            final ClonedValuesFixture replayed = fixture.replayed();
            assertClonedValuesPresent(replayed, replayed.aggregateApplicationOffence(0, 0));

            final ResultsSharedV3 reshared = resultsShared(fixture.share(amendGrantToRefusal(fixture, grantLineId)));

            assertClonedValuesPresent(fixture, fixture.sharedApplicationOffence(reshared, 0, 0));
        }
    }

    @Nested
    class AdjustmentEvent {

        @Test
        void grantWithClonedValuesAppendsOneClearedEventBeforeResultsShared() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 2).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);

            final List<Object> events = fixture.share(singletonList(fixture.applicationResult(0, G)));

            final List<JsonNode> adjusted = clonedValuesAdjusted(events);
            assertThat(adjusted, hasSize(1));
            assertThat(indexOfClonedValuesAdjusted(events), lessThan(indexOf(events, ResultsSharedV3.class)));

            final JsonNode event = adjusted.get(0);
            assertThat(event.path("hearingId").asText(), is(fixture.hearingId().toString()));
            assertThat(event.path("hearingDay").asText(), is(fixture.hearingDay(0).toString()));
            assertThat(event.path("sharedTime").isMissingNode(), is(false));
            assertThat(event.path("applicationId").asText(), is(fixture.applicationId(0).toString()));
            assertThat(event.path("applicationTypeCode").asText(), is(STAT_DEC));
            assertThat(event.path("outcome").asText(), is("GRANTED"));
            assertThat(event.path("action").asText(), is("CLEARED"));
            assertThat(event.path("offences").size(), is(2));

            final JsonNode first = offenceEntry(event, fixture.offenceId(0, 0));
            assertThat(fieldsChanged(first), containsInAnyOrder("verdict", "convictionDate"));
            assertThat(first.path("previous").path("verdict").isMissingNode(), is(false));
            assertThat(first.path("previous").path("convictionDate").asText(), is(fixture.originalConvictionDate().toString()));
            assertThat(first.path("adjusted").path("plea").path("pleaValue").asText(), is(NOT_GUILTY));
            assertThat(first.path("adjusted").path("verdict").isMissingNode() || first.path("adjusted").path("verdict").isNull(), is(true));

            final JsonNode second = offenceEntry(event, fixture.offenceId(0, 1));
            assertThat(fieldsChanged(second), containsInAnyOrder("plea", "verdict", "convictionDate"));
            assertThat(second.path("previous").path("plea").path("pleaValue").asText(), is(GUILTY));
        }

        @Test
        void reshareWithAnUnchangedOutcomeAppendsNoEvent() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final List<SharedResultsCommandResultLineV2> grant = singletonList(fixture.applicationResult(0, G));
            assertThat(clonedValuesAdjusted(fixture.share(grant)), hasSize(1));

            assertThat(clonedValuesAdjusted(fixture.share(grant)), is(empty()));
        }

        @Test
        void regrantAfterAGrantWasAmendedToAnAdjournmentRecordsTheClear() {
            // review finding (Major): every system clear is recorded, including one following a share with no outcome
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final UUID grantLineId = randomUUID();
            final UUID adjournmentLineId = randomUUID();
            final UUID adjournment = randomUUID();
            final ZonedDateTime amendedAt = ZonedDateTime.now();
            final ZonedDateTime regrantedAt = amendedAt.plusMinutes(1);
            fixture.share(singletonList(fixture.applicationResult(0, G, grantLineId, false, null)));
            fixture.share(asList(
                    fixture.applicationResult(0, G, grantLineId, true, amendedAt),
                    fixture.applicationResult(0, adjournment, adjournmentLineId, false, amendedAt)));

            final List<Object> events = fixture.share(asList(
                    fixture.applicationResult(0, G, grantLineId, true, amendedAt),
                    fixture.applicationResult(0, adjournment, adjournmentLineId, true, regrantedAt),
                    fixture.applicationResult(0, G, randomUUID(), false, regrantedAt)));

            assertCleared(fixture.sharedApplicationOffence(resultsShared(events), 0, 0));
            final List<JsonNode> adjusted = clonedValuesAdjusted(events);
            assertThat(adjusted, hasSize(1));
            assertThat(adjusted.get(0).path("action").asText(), is("CLEARED"));
            final JsonNode offence = offenceEntry(adjusted.get(0), fixture.offenceId(0, 0));
            assertThat(fieldsChanged(offence), containsInAnyOrder("plea", "verdict", "convictionDate"));
            assertThat(offence.path("previous").path("plea").path("pleaValue").asText(), is(GUILTY));
        }

        @Test
        void firstReshareAfterDeployRecordsTheClearOfAGrantSharedBeforeDeploy() {
            // review finding M1: a STAT DEC already shared as granted before deploy has no recorded clear
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final List<SharedResultsCommandResultLineV2> grant = singletonList(fixture.applicationResult(0, G));
            fixture.sharePreDeploy(grant);

            final List<Object> events = fixture.share(grant);

            assertCleared(fixture.sharedApplicationOffence(resultsShared(events), 0, 0));
            final List<JsonNode> adjusted = clonedValuesAdjusted(events);
            assertThat(adjusted, hasSize(1));
            assertThat(adjusted.get(0).path("action").asText(), is("CLEARED"));
            assertThat(fieldsChanged(offenceEntry(adjusted.get(0), fixture.offenceId(0, 0))), containsInAnyOrder("plea", "verdict", "convictionDate"));
        }

        @Test
        void grantResharedAfterTheAdviserEntersAPleaRecordsNoSystemChange() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final List<SharedResultsCommandResultLineV2> grant = singletonList(fixture.applicationResult(0, G));
            fixture.share(grant);
            fixture.enterPlea(0, 0, NOT_GUILTY);

            final List<Object> events = fixture.share(grant);

            assertThat(fixture.sharedApplicationOffence(resultsShared(events), 0, 0).getPlea().getPleaValue(), is(NOT_GUILTY));
            assertThat(clonedValuesAdjusted(events), is(empty()));
        }

        @Test
        void reinitiatedHearingRecordsTheFirstClearAgain() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            final List<SharedResultsCommandResultLineV2> grant = singletonList(fixture.applicationResult(0, G));
            fixture.share(grant);
            fixture.reinitiate();

            assertThat(clonedValuesAdjusted(fixture.share(grant)), hasSize(1));
        }

        @Test
        void grantedStatDecWithNothingClonedLeftToClearAppendsNoEvent() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();
            fixture.enterPlea(0, 0, NOT_GUILTY);
            fixture.enterVerdict(0, 0, NOT_GUILTY);

            assertThat(clonedValuesAdjusted(fixture.share(singletonList(fixture.applicationResult(0, G)))), is(empty()));
        }

        @Test
        void refusalSharedFirstRestoresNothingAndAppendsNoEvent() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(STAT_DEC, SubjectType.INDIVIDUAL, 1).initiate();

            assertThat(clonedValuesAdjusted(fixture.share(singletonList(fixture.applicationResult(0, RFSD)))), is(empty()));
        }

        @Test
        void otherApplicationTypeAppendsNoEvent() {
            final ClonedValuesFixture fixture = new ClonedValuesFixture().withApplication(REOPEN, SubjectType.INDIVIDUAL, 1).initiate();

            assertThat(clonedValuesAdjusted(fixture.share(singletonList(fixture.applicationResult(0, G)))), is(empty()));
        }

        private JsonNode offenceEntry(final JsonNode event, final UUID offenceId) {
            for (final JsonNode entry : event.path("offences")) {
                if (offenceId.toString().equals(entry.path("offenceId").asText())) {
                    return entry;
                }
            }
            throw new AssertionError("no offence entry for " + offenceId);
        }

        private List<String> fieldsChanged(final JsonNode entry) {
            final List<String> fields = new ArrayList<>();
            entry.path("fieldsChanged").forEach(field -> fields.add(field.asText()));
            return fields;
        }
    }

    private static List<SharedResultsCommandResultLineV2> amendGrantToRefusal(final ClonedValuesFixture fixture, final UUID grantLineId) {
        final ZonedDateTime amendedAt = ZonedDateTime.now();
        return asList(
                fixture.applicationResult(0, G, grantLineId, true, amendedAt),
                fixture.applicationResult(0, RFSD, randomUUID(), false, amendedAt));
    }

    private static void assertCleared(final Offence offence) {
        assertThat(offence.getPlea(), is(nullValue()));
        assertThat(offence.getVerdict(), is(nullValue()));
        assertThat(offence.getConvictionDate(), is(nullValue()));
    }

    private static void assertClonedValuesPresent(final ClonedValuesFixture fixture, final Offence offence) {
        assertThat(offence.getPlea().getOriginatingHearingId(), is(fixture.originatingHearingId()));
        assertThat(offence.getPlea().getPleaValue(), is(GUILTY));
        assertThat(offence.getVerdict().getOriginatingHearingId(), is(fixture.originatingHearingId()));
        assertThat(offence.getConvictionDate(), is(fixture.originalConvictionDate()));
    }
}
