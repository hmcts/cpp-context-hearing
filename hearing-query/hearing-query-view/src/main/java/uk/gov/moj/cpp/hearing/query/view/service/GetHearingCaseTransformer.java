package uk.gov.moj.cpp.hearing.query.view.service;

import static java.lang.Boolean.TRUE;

import uk.gov.justice.hearing.courts.HearingCases;
import uk.gov.moj.cpp.hearing.dto.HearingCaseForDayRow;

import java.time.LocalDate;
import java.util.List;

public class GetHearingCaseTransformer {

    /**
     * Builds the HearingCases for one hearing from its projection rows. All rows must share the same hearing id.
     */
    public HearingCases.Builder hearingCases(final List<HearingCaseForDayRow> rowsForHearing, final LocalDate date) {
        final HearingCaseForDayRow hearing = rowsForHearing.get(0);

        return HearingCases.hearingCases()
                .withHearingId(hearing.getHearingId())
                .withCourtCentreId(hearing.getCourtCentreId())
                .withCourtRoomId(hearing.getCourtRoomId())
                .withHearingDate(date.toString())
                .withProsecutionCases(
                        rowsForHearing.stream()
                                .filter(this::shouldCaseBeIncluded)
                                .map(HearingCaseForDayRow::getCaseId)
                                .distinct()
                                .toList()
                );
    }

    private boolean shouldCaseBeIncluded(final HearingCaseForDayRow row) {
        if (TRUE.equals(row.getIsGroupProceedings())) {
            return TRUE.equals(row.getIsGroupMaster()) || !TRUE.equals(row.getIsGroupMember());
        }
        return true;
    }
}
