package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.mags-court-hearing-recorded")
public class MagsCourtHearingRecorded implements Serializable {

    private static final long serialVersionUID = 1L;

    private Hearing originatingHearing;
    private LocalDate convictionDate;
    private UUID hearingId;

    @JsonCreator
    public MagsCourtHearingRecorded(@JsonProperty(value = "originatingHearing") final Hearing originatingHearing,
                                    @JsonProperty(value = "convictionDate") final LocalDate convictionDate,
                                    @JsonProperty(value = "hearingId") final UUID hearingId) {
        this.originatingHearing = originatingHearing;
        this.convictionDate = convictionDate;
        this.hearingId = hearingId;
    }

    public Hearing getOriginatingHearing() {
        return originatingHearing;
    }

    public LocalDate getConvictionDate() {
        return convictionDate;
    }

    public UUID getHearingId() {
        return hearingId;
    }


    public static class Builder {

        private Hearing hearing;

        private LocalDate convictionDate;

        private UUID hearingId;

        public Builder withHearing(final Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public Builder withConvictionDate(LocalDate convictionDate) {
            this.convictionDate = convictionDate;
            return this;
        }

        public Builder withHearingId(UUID hearingId) {
            this.hearingId = hearingId;
            return this;
        }

        public MagsCourtHearingRecorded build() {
            return new MagsCourtHearingRecorded(hearing, convictionDate, hearingId);
        }
    }

    public static Builder builder(){
        return new Builder();
    }
}
