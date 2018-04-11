package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.external.domain.progression.sendingsheetcompleted.Hearing;

import java.time.LocalDate;
import java.util.UUID;

/*
* must contain sufficient information to initiate a hearing
 */
@Event("hearing.newmags-court-hearing-recorded")
public class NewMagsCourtHearingRecorded {

    private Hearing originatingHearing;
    private LocalDate convictionDate;
    private UUID hearingId;

    @JsonCreator
    public NewMagsCourtHearingRecorded(@JsonProperty(value = "originatingHearing") final Hearing originatingHearing,
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
