package uk.gov.moj.cpp.hearing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.domain.annotation.Event;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.events.found-plea-for-hearing-to-inherit")
public class FoundPleaForHearingToInherit implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private String value;

    @JsonCreator
    public FoundPleaForHearingToInherit(@JsonProperty("offenceId") UUID offenceId,
                                        @JsonProperty("caseId") UUID caseId,
                                        @JsonProperty("defendantId") UUID defendantId,
                                        @JsonProperty("hearingId") UUID hearingId,
                                        @JsonProperty("originHearingId") UUID originHearingId,
                                        @JsonProperty("pleaDate") LocalDate pleaDate,
                                        @JsonProperty("value") String value) {
        this.offenceId = offenceId;
        this.caseId = caseId;
        this.defendantId = defendantId;
        this.hearingId = hearingId;
        this.originHearingId = originHearingId;
        this.pleaDate = pleaDate;
        this.value = value;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getHearingId() {
        return hearingId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public LocalDate getPleaDate() {
        return pleaDate;
    }

    public String getValue() {
        return value;
    }
}
