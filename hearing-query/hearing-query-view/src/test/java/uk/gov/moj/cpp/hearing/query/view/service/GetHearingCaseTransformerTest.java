package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static uk.gov.justice.core.courts.CourtCentre.courtCentre;
import static uk.gov.justice.core.courts.Hearing.hearing;
import static uk.gov.justice.core.courts.ProsecutionCase.prosecutionCase;
import static uk.gov.justice.core.courts.ProsecutionCaseIdentifier.prosecutionCaseIdentifier;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.hearing.courts.HearingCases;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class GetHearingCaseTransformerTest {

    private final GetHearingCaseTransformer transformer = new GetHearingCaseTransformer();

    @Test
    void shouldTransformHearingWithNullProsecutionCasesAndHearingDate() {
        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final Hearing hearing = hearing().withId(hearingId)
                .withCourtCentre(courtCentre().withId(courtCentreId).withRoomId(roomId).build())
                .withProsecutionCases(null)
                .build();

        final HearingCases result = transformer.hearingCases(hearing, LocalDate.now()).build();

        assertThat(result.getHearingId(), equalTo(hearingId));
        assertThat(result.getCourtCentreId(), equalTo(courtCentreId));
        assertThat(result.getCourtRoomId(), equalTo(roomId));
        assertThat(result.getHearingDate(), is(nullValue()));
        assertThat(result.getProsecutionCases(), is(emptyList()));
    }

    @Test
    void shouldTransformHearing() {
        final UUID hearingId = randomUUID();
        final UUID courtCentreId = randomUUID();
        final UUID roomId = randomUUID();
        final UUID caseId = randomUUID();
        final String caseUrn = "caseUrn";
        final ZonedDateTime sittingDay = ZonedDateTime.now();
        final Hearing hearing = hearing().withId(hearingId)
                .withCourtCentre(courtCentre().withId(courtCentreId).withRoomId(roomId).build())
                .withHearingDays(List.of(HearingDay.hearingDay().withSittingDay(sittingDay).build()))
                .withProsecutionCases(List.of(
                        prosecutionCase().withId(caseId)
                                .withProsecutionCaseIdentifier(prosecutionCaseIdentifier().withCaseURN(caseUrn).build())
                                .build()
                ))
                .build();

        final HearingCases result = transformer.hearingCases(hearing, sittingDay.toLocalDate()).build();

        assertThat(result.getHearingId(), equalTo(hearingId));
        assertThat(result.getCourtCentreId(), equalTo(courtCentreId));
        assertThat(result.getCourtRoomId(), equalTo(roomId));
        assertThat(result.getHearingDate(), is(LocalDate.now().toString()));
        assertThat(result.getProsecutionCases().get(0).getCaseId(), equalTo(caseId));
        assertThat(result.getProsecutionCases().get(0).getProsecutionCaseIdentifier().getCaseURN(), equalTo(caseUrn));
    }

    @Test
    void shouldReturnHearingDateMatchingTheQueriedDayForMultiDayHearing() {
        final ZonedDateTime today = ZonedDateTime.now();
        final ZonedDateTime tomorrow = today.plusDays(1);
        final Hearing hearing = hearing().withId(randomUUID())
                .withCourtCentre(courtCentre().withId(randomUUID()).withRoomId(randomUUID()).build())
                .withHearingDays(List.of(
                        HearingDay.hearingDay().withSittingDay(today).build(),
                        HearingDay.hearingDay().withSittingDay(tomorrow).build()))
                .withProsecutionCases(List.of())
                .build();

        final HearingCases result = transformer.hearingCases(hearing, tomorrow.toLocalDate()).build();

        assertThat(result.getHearingDate(), is(tomorrow.toLocalDate().toString()));
    }
}