package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.gov.justice.core.courts.CourtApplication;
import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.core.courts.ProsecutionCaseIdentifier;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;

public class HearingEventDelegateTest {

    private HearingAggregateMomento momento = new HearingAggregateMomento();
    private HearingEventDelegate hearingDelegate = new HearingEventDelegate(momento);
    private String courtApplicationReference = "TEST-APPLICATION-REFERENCE";
    private String caseURN = "39GD9692220";


    @Test
    public void handleMomentoHearingWhenCourtApplicationsAreEmpty() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), caseURN, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(caseURN));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsArePresented() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), caseURN, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(caseURN));
    }


    @Test
    public void handleMomentoHearingWhenCourtApplicationsArePresentedCaseURNIsEmpty() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is("REF12345"));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsEmptyCaseURNIsEmpty() {

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, "REF12345")))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is("REF12345"));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsCaseURNAndReferenceIsEmpty() {
        final CourtApplication courtApplication = createCourtApplication(UUID.randomUUID());

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of(courtApplication))
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, null)))
                .build());

        String reference = hearingDelegate.getReference();
        assertThat(reference, is(courtApplicationReference));
    }

    @Test
    public void handleMomentoHearingWhenCourtApplicationsEmptyCaseURNAndReferenceIsEmpty() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withCourtApplications(List.of())
                .withProsecutionCases(List.of(createProsecutionCase(UUID.randomUUID(), null, null)))
                .build());

        String reference = hearingDelegate.getReference();
        Assert.assertNull(reference);
    }


    // ------------------------------------------------------------------------
    // findHearingDayFor: per-day resolution used by the override / log-event path
    // ------------------------------------------------------------------------

    @Test
    public void findHearingDayFor_shouldReturnEmpty_whenEventTimeIsNull() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withHearingDays(List.of(hearingDayAt(ZonedDateTime.now(ZoneOffset.UTC))))
                .build());

        assertThat(hearingDelegate.findHearingDayFor(null), is(Optional.empty()));
    }

    @Test
    public void findHearingDayFor_shouldReturnEmpty_whenMomentoHasNoHearing() {
        // momento.getHearing() is null (no setHearing call)
        assertThat(hearingDelegate.findHearingDayFor(ZonedDateTime.now(ZoneOffset.UTC)), is(Optional.empty()));
    }

    @Test
    public void findHearingDayFor_shouldReturnEmpty_whenHearingDaysIsNull() {
        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withHearingDays(null)
                .build());

        assertThat(hearingDelegate.findHearingDayFor(ZonedDateTime.now(ZoneOffset.UTC)), is(Optional.empty()));
    }

    @Test
    public void findHearingDayFor_shouldReturnDay_whoseSittingDayMatchesEventTimeDate() {
        final ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);
        final HearingDay yesterdayDay = hearingDayAt(today.minusDays(1));
        final HearingDay todayDay = hearingDayAt(today);
        final HearingDay tomorrowDay = hearingDayAt(today.plusDays(1));

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withHearingDays(List.of(yesterdayDay, todayDay, tomorrowDay))
                .build());

        final Optional<HearingDay> matched = hearingDelegate.findHearingDayFor(today);

        assertThat(matched.isPresent(), is(true));
        assertThat(matched.get().getSittingDay().toLocalDate(), is(today.toLocalDate()));
    }

    @Test
    public void findHearingDayFor_shouldReturnEmpty_whenNoDayMatchesEventTimeDate() {
        final ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withHearingDays(List.of(hearingDayAt(today.minusDays(1)), hearingDayAt(today.plusDays(1))))
                .build());

        assertThat(hearingDelegate.findHearingDayFor(today), is(Optional.empty()));
    }

    @Test
    public void findHearingDayFor_shouldSkipDays_whoseSittingDayIsNull_andReturnMatchAmongstThem() {
        final ZonedDateTime today = ZonedDateTime.now(ZoneOffset.UTC);
        final HearingDay nullSittingDay = HearingDay.hearingDay().build();
        final HearingDay todayDay = hearingDayAt(today);

        momento.setHearing(Hearing.hearing()
                .withId(UUID.randomUUID())
                .withHearingDays(List.of(nullSittingDay, todayDay))
                .build());

        final Optional<HearingDay> matched = hearingDelegate.findHearingDayFor(today);

        assertThat(matched.isPresent(), is(true));
        assertThat(matched.get().getSittingDay().toLocalDate(), is(today.toLocalDate()));
    }

    private HearingDay hearingDayAt(final ZonedDateTime sittingDay) {
        return HearingDay.hearingDay()
                .withSittingDay(sittingDay)
                .build();
    }

    private CourtApplication createCourtApplication(final UUID id) {
        return new CourtApplication.Builder()
                .withId(id)
                .withApplicationReference(courtApplicationReference)
                .build();
    }

    private ProsecutionCase createProsecutionCase(final UUID id, final String caseURN, final String reference) {
        return new ProsecutionCase.Builder()
                .withId(id)
                .withProsecutionCaseIdentifier(ProsecutionCaseIdentifier.prosecutionCaseIdentifier().withCaseURN(caseURN).withProsecutionAuthorityReference(reference).build())
                .build();
    }
}
