package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;
import uk.gov.moj.cpp.hearing.eventlog.HearingApplicationDetail;
import uk.gov.moj.cpp.hearing.eventlog.HearingCaseDetail;
import uk.gov.moj.cpp.hearing.eventlog.HearingDefendantDetail;
import uk.gov.moj.cpp.util.HearingDetailUtil;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings({"pmd:BeanMembersShouldSerialize", "PMD:BeanMembersShouldSerialize"})
public class HearingTrialTypeDelegate implements Serializable {

    private static final long serialVersionUID = 1L;

    private final HearingAggregateMomento momento;

    public HearingTrialTypeDelegate(final HearingAggregateMomento momento) {
        this.momento = momento;
    }

    public void handleVacateTrialTypeSetForHearing(final HearingTrialVacated hearingTrialType) {
        if (nonNull(hearingTrialType.getVacatedTrialReasonId())) {
            this.momento.getHearing().setCrackedIneffectiveTrial(CrackedIneffectiveTrial.crackedIneffectiveTrial()
                    .withId(hearingTrialType.getVacatedTrialReasonId())
                    .withCode(hearingTrialType.getCode())
                    .withDescription(hearingTrialType.getDescription())
                    .withType(hearingTrialType.getType())
                    .build());
            this.momento.getHearing().setIsVacatedTrial(true);
            this.momento.getHearing().setIsEffectiveTrial(null);
        }
    }

    public void handleTrialTypeSetForHearing(final HearingTrialType hearingTrialType) {
        if (nonNull(hearingTrialType.getTrialTypeId())) {
            this.momento.getHearing().setCrackedIneffectiveTrial(CrackedIneffectiveTrial.crackedIneffectiveTrial()
                    .withId(hearingTrialType.getTrialTypeId())
                    .withCode(hearingTrialType.getCode())
                    .withDescription(hearingTrialType.getDescription())
                    .withType(hearingTrialType.getType())
                    .build());
            this.momento.getHearing().setIsVacatedTrial(false);
            this.momento.getHearing().setIsEffectiveTrial(null);
        }
    }

    public void handleEffectiveTrailHearing(final HearingEffectiveTrial hearingEffectiveTrial) {
        if (nonNull(hearingEffectiveTrial.getIsEffectiveTrial()) && hearingEffectiveTrial.getIsEffectiveTrial()) {
            this.momento.getHearing().setIsEffectiveTrial(hearingEffectiveTrial.getIsEffectiveTrial());
            this.momento.getHearing().setCrackedIneffectiveTrial(null);
            this.momento.getHearing().setIsVacatedTrial(false);
        }
    }

    public Stream<Object> setTrialType(final HearingTrialType hearingTrialType) {
        return Stream.of(hearingTrialType);
    }

    public Stream<Object> setTrialType(final HearingEffectiveTrial hearingEffectiveTrial) {
        return Stream.of(hearingEffectiveTrial);
    }

    public Stream<Object> setTrialType(final HearingTrialVacated hearingTrialVacated) {

        final List<HearingCaseDetail> caseDetails = HearingDetailUtil.getCaseDetails(this.momento.getHearing());
        final List<HearingApplicationDetail> applicationDetails = HearingDetailUtil.getApplicationDetails(this.momento.getHearing());
        final ZonedDateTime hearingDate = this.momento.getHearing().getHearingDays().get(0).getSittingDay();
        final UUID courtCentreId = momento.getHearing().getCourtCentre().getId();

        hearingTrialVacated.setHearingDay(hearingDate);
        hearingTrialVacated.setCaseDetails(caseDetails);
        hearingTrialVacated.setApplicationDetails(applicationDetails);
        hearingTrialVacated.setHasInterpreter(hasInterpreterNeeded(caseDetails, applicationDetails));
        hearingTrialVacated.setJurisdictionType(this.momento.getHearing().getJurisdictionType());
        hearingTrialVacated.setCourtCentreId(courtCentreId);
        return Stream.of(hearingTrialVacated);
    }

    private boolean hasInterpreterNeeded(final List<HearingCaseDetail> caseDetails, final List<HearingApplicationDetail> applicationDetails) {
        boolean hasInterpreter = false;
        if (!caseDetails.isEmpty()) {
            hasInterpreter = caseDetails.stream().filter(Objects::nonNull)
                    .flatMap(hearingCaseDetail -> hearingCaseDetail.getDefendantDetails() == null ? null : hearingCaseDetail.getDefendantDetails().stream())
                    .filter(Objects::nonNull)
                    .map(HearingDefendantDetail::getInterpreterLanguageNeeds)
                    .anyMatch(interpreterLanguage -> interpreterLanguage != null && !EMPTY.equals(interpreterLanguage.trim()));
        }

        if (!hasInterpreter && !applicationDetails.isEmpty()) {
            hasInterpreter = applicationDetails.stream().filter(Objects::nonNull)
                    .map(hearingApplicationDetail -> hearingApplicationDetail.getSubject() == null ? EMPTY : hearingApplicationDetail.getSubject().getInterpreterLanguageNeeds())
                    .anyMatch(interpreterLanguage -> interpreterLanguage != null && !EMPTY.equals(interpreterLanguage.trim()));
        }
        return hasInterpreter;
    }
}
