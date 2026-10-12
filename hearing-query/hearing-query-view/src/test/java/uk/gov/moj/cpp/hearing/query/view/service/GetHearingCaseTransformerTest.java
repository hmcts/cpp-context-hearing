package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import uk.gov.justice.hearing.courts.HearingCases;
import uk.gov.moj.cpp.hearing.dto.HearingCaseForDayRow;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class GetHearingCaseTransformerTest {

    private final GetHearingCaseTransformer transformer = new GetHearingCaseTransformer();

    private final UUID hearingId = randomUUID();
    private final UUID courtCentreId = randomUUID();
    private final UUID roomId = randomUUID();

    @Test
    void shouldTransformHearing() {
        final UUID caseId = randomUUID();
        final LocalDate date = LocalDate.now();

        final HearingCases result = transformer.hearingCases(List.of(row(null, caseId, null, null)), date).build();

        assertThat(result.getHearingId(), equalTo(hearingId));
        assertThat(result.getCourtCentreId(), equalTo(courtCentreId));
        assertThat(result.getCourtRoomId(), equalTo(roomId));
        assertThat(result.getHearingDate(), is(date.toString()));
        assertThat(result.getProsecutionCases(), equalTo(List.of(caseId)));
    }

    @Test
    void shouldReturnQueriedDateAsHearingDate() {
        final LocalDate tomorrow = LocalDate.now().plusDays(1);

        final HearingCases result = transformer.hearingCases(List.of(row(false, randomUUID(), null, null)), tomorrow).build();

        assertThat(result.getHearingDate(), is(tomorrow.toString()));
    }

    @Test
    void shouldIncludeAllCasesOfHearingInRowOrder() {
        final UUID firstCaseId = randomUUID();
        final UUID secondCaseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(
                row(false, firstCaseId, null, null),
                row(false, secondCaseId, null, null)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(firstCaseId, secondCaseId)));
    }

    @Test
    void shouldDeduplicateCaseIds() {
        final UUID caseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(
                row(false, caseId, null, null),
                row(false, caseId, null, null)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(caseId)));
    }

    @Test
    void shouldIncludeGroupMemberCaseWhenHearingIsNotGroupProceedings() {
        final UUID caseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(row(false, caseId, false, true)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(caseId)));
    }

    @Test
    void shouldIncludeGroupMasterCaseWhenHearingIsGroupProceedings() {
        final UUID masterCaseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(row(true, masterCaseId, true, true)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(masterCaseId)));
    }

    @Test
    void shouldIncludeNonGroupMemberCaseWhenHearingIsGroupProceedings() {
        final UUID caseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(row(true, caseId, false, null)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(caseId)));
    }

    @Test
    void shouldExcludeGroupMemberCaseWhenHearingIsGroupProceedings() {
        final UUID memberCaseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(row(true, memberCaseId, false, true)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), is(emptyList()));
    }

    @Test
    void shouldKeepMasterAndDropMembersForGroupProceedings() {
        final UUID masterCaseId = randomUUID();

        final HearingCases result = transformer.hearingCases(List.of(
                row(true, randomUUID(), false, true),
                row(true, masterCaseId, true, true),
                row(true, randomUUID(), null, true)), LocalDate.now()).build();

        assertThat(result.getProsecutionCases(), equalTo(List.of(masterCaseId)));
    }

    private HearingCaseForDayRow row(final Boolean isGroupProceedings, final UUID caseId, final Boolean isGroupMaster, final Boolean isGroupMember) {
        return new HearingCaseForDayRow(hearingId, courtCentreId, roomId, isGroupProceedings, caseId, isGroupMaster, isGroupMember);
    }
}
