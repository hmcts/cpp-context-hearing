package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import java.time.LocalDate;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Event("hearing.mags-court-hearing-recorded")
public class MagsCourtHearingRecorded {

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


}
