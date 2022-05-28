package uk.gov.moj.cpp.hearing.domain.aggregate.hearing;

import static java.util.Objects.nonNull;

import uk.gov.justice.core.courts.CrackedIneffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingEffectiveTrial;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialType;
import uk.gov.moj.cpp.hearing.domain.event.HearingTrialVacated;

import java.io.Serializable;
import java.util.UUID;
import java.util.stream.Stream;

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

    public Stream<Object> setTrialType(final UUID hearingId, final UUID vacatedTrialReasonId, final String code, final String description, final String type) {
        final UUID courtCentreId = momento.getHearing().getCourtCentre().getId();
        return Stream.of(new HearingTrialVacated(hearingId, vacatedTrialReasonId, code, description, type, courtCentreId));
    }
}
