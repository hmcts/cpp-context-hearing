package uk.gov.moj.cpp.hearing.query.view.service;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import uk.gov.justice.core.courts.Hearing;
import uk.gov.justice.core.courts.HearingDay;
import uk.gov.justice.core.courts.ProsecutionCase;
import uk.gov.justice.hearing.courts.HearingCases;
import uk.gov.justice.hearing.courts.ProsecutionCases;

import java.util.List;

public class GetHearingCaseTransformer {

    public HearingCases.Builder hearingCases(final Hearing hearing) {

        return HearingCases.hearingCases()
                .withHearingId(hearing.getId())
                .withCourtCentreId(hearing.getCourtCentre().getId())
                .withCourtRoomId(hearing.getCourtCentre().getRoomId())
                .withHearingDate(getHearingDate(hearing.getHearingDays()))
                .withProsecutionCases(
                        hearing.getProsecutionCases() == null ? emptyList() :
                                hearing.getProsecutionCases().stream()
                                        .filter(pc -> shouldCaseBeIncluded(hearing, pc))
                                        .map(pc -> caseDetailForToday(pc).build())
                                        .collect(toList())
                );
    }

    private String getHearingDate(final List<HearingDay> hearingDays) {
        if (isNotEmpty(hearingDays)) {
            return hearingDays.get(0).getSittingDay().toLocalDate().toString();
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

    private ProsecutionCases.Builder caseDetailForToday(final ProsecutionCase prosecutionCase) {
        return ProsecutionCases.prosecutionCases()
                .withCaseId(prosecutionCase.getId())
                .withProsecutionCaseIdentifier(prosecutionCase.getProsecutionCaseIdentifier());
    }

}
