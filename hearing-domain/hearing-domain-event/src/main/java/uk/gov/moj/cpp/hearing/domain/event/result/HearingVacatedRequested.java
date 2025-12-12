package uk.gov.moj.cpp.hearing.domain.event.result;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings({"squid:S2384", "PMD.BeanMembersShouldSerialize"})
@Event("hearing.hearing-vacated-requested")
public class HearingVacatedRequested {

    private UUID hearingIdToBeVacated;

    private String vacatedTrialReasonShortDesc;

    @JsonCreator
    private HearingVacatedRequested(@JsonProperty("hearingIdToBeVacated") final UUID hearingIdToBeVacated,
                                    @JsonProperty("vacatedTrialReasonShortDesc") final String vacatedTrialReasonShortDesc) {
        this.hearingIdToBeVacated = hearingIdToBeVacated;
        this.vacatedTrialReasonShortDesc = vacatedTrialReasonShortDesc;
    }

    public static HearingVacatedRequested.Builder builder() {
        return new HearingVacatedRequested.Builder();
    }


    public UUID getHearingIdToBeVacated() {
        return hearingIdToBeVacated;
    }

    public void setHearingIdToBeVacated(final UUID hearingIdToBeVacated) {
        this.hearingIdToBeVacated = hearingIdToBeVacated;
    }

    public String getVacatedTrialReasonShortDesc() {
        return vacatedTrialReasonShortDesc;
    }

    public void setVacatedTrialReasonShortDesc(final String vacatedTrialReasonShortDesc) {
        this.vacatedTrialReasonShortDesc = vacatedTrialReasonShortDesc;
    }

    @SuppressWarnings("PMD:BeanMembersShouldSerialize")
    public static final class Builder {

        private UUID hearingIdToBeVacated;

        private String vacatedTrialReasonShortDesc;

        public HearingVacatedRequested.Builder withHearingIdToBeVacated(final UUID hearingIdToBeVacated) {
            this.hearingIdToBeVacated = hearingIdToBeVacated;
            return this;
        }

        public HearingVacatedRequested.Builder withVacatedTrialReasonShortDesc(final String vacatedTrialReasonShortDesc) {
            this.vacatedTrialReasonShortDesc = vacatedTrialReasonShortDesc;
            return this;
        }

        public HearingVacatedRequested build() {
            return new HearingVacatedRequested(hearingIdToBeVacated, vacatedTrialReasonShortDesc);
        }
    }

}
