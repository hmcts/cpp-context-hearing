package uk.gov.moj.cpp.hearing.domain.event;

import java.time.LocalDate;
import java.util.UUID;

import uk.gov.justice.domain.annotation.Event;
import uk.gov.moj.cpp.hearing.domain.HearingTypeEnum;

@Event("hearing.events.hearing-listed")
public class HearingListed {

    private UUID hearingId;

    private UUID caseId;

    private HearingTypeEnum hearingType;

    private String courtCentreName;

    private LocalDate startDateOfHearing;

    private Integer duration;

    public HearingListed(UUID hearingId, UUID caseId, HearingTypeEnum hearingType, String courtCentreName,
            LocalDate startDateOfHearing, Integer duration) {
        super();
        this.hearingId = hearingId;
        this.caseId = caseId;
        this.hearingType = hearingType;
        this.courtCentreName = courtCentreName;
        this.startDateOfHearing = startDateOfHearing;
        this.duration = duration;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public HearingTypeEnum getHearingType() {
        return hearingType;
    }

    public String getCourtCentreName() {
        return courtCentreName;
    }

    public LocalDate getStartDateOfHearing() {
        return startDateOfHearing;
    }

    public Integer getDuration() {
        return duration;
    }

}
