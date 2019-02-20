package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.CrownCourtHearing;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.sending-sheet-recorded")
public class SendingSheetCompletedRecorded implements Serializable {

    private static final long serialVersionUID = 1L;

    private final CrownCourtHearing crownCourtHearing;

    private final Hearing hearing;

    @JsonCreator
    public SendingSheetCompletedRecorded(
            @JsonProperty(value = "crownCourtHearing") final CrownCourtHearing crownCourtHearing,
            @JsonProperty(value = "hearing") final Hearing hearing) {
        this.crownCourtHearing = crownCourtHearing;
        this.hearing = hearing;
    }

    public static Builder sendingSheetRecorded() {
        return new Builder();
    }

    public static Builder builder() {
        return new Builder();
    }

    public CrownCourtHearing getCrownCourtHearing() {
        return crownCourtHearing;
    }

    public Hearing getHearing() {
        return hearing;
    }

    public static class Builder {
        private CrownCourtHearing crownCourtHearing;

        private Hearing hearing;

        public Builder withCrownCourtHearing(final CrownCourtHearing crownCourtHearing) {
            this.crownCourtHearing = crownCourtHearing;
            return this;
        }

        public Builder withHearing(final Hearing hearing) {
            this.hearing = hearing;
            return this;
        }

        public SendingSheetCompletedRecorded build() {
            return new SendingSheetCompletedRecorded(crownCourtHearing, hearing);
        }
    }
}
