package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.hearing.courts.HearingCases;

import java.time.LocalDate;
import java.util.List;

public class GetHearingCaseTransformer {

    public HearingCases.Builder hearingCases(final Hearing hearing, final LocalDate date) {

        return HearingCases.hearingCases()
                .withHearingId(hearing.getId())
                .withCourtCentreId(hearing.getCourtCentre().getId())
                .withCourtRoomId(hearing.getCourtCentre().getRoomId())
                .withHearingDate(getHearingDate(hearing.getHearingDays(), date))
                .withProsecutionCases(
                        hearing.getProsecutionCases() == null ? emptyList() :
                                hearing.getProsecutionCases().stream()
                                        .filter(pc -> shouldCaseBeIncluded(hearing, pc))
                                        .map(ProsecutionCase::getId)
                                        .collect(toList())
                );
    }

    private String getHearingDate(final List<HearingDay> hearingDays, final LocalDate date) {
        if (isNotEmpty(hearingDays)) {
            return hearingDays.stream()
                    .map(hd -> hd.getSittingDay().toLocalDate())
                    .filter(date::equals)
                    .findFirst()
                    .map(LocalDate::toString)
                    .orElse(hearingDays.get(0).getSittingDay().toLocalDate().toString()); // defensive fallback; shouldn't hit since the repo already filtered on this date
        }
        return null;
    }

    private boolean shouldCaseBeIncluded(final Hearing hearing, final ProsecutionCase pc) {
        if (nonNull(hearing.getIsGroupProceedings()) && hearing.getIsGroupProceedings()) {
            if (nonNull(pc.getIsGroupMaster()) && pc.getIsGroupMaster()) {
                return true;
            } else {
                return isNull(pc.getIsGroupMember()) || !(pc.getIsGroupMember());
            }
        } else {
            return true;
        }
    }
}
