package uk.gov.moj.cpp.hearing.command.initiate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.UUID;

public class InitiateHearingOffencePleaCommand {

    private UUID offenceId;
    private UUID caseId;
    private UUID defendantId;
    private UUID hearingId;
    private UUID originHearingId;
    private LocalDate pleaDate;
    private String value;

    @JsonCreator
    public InitiateHearingOffencePleaCommand(@JsonProperty("offenceId") UUID offenceId,
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
