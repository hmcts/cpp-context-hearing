package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.util.UUID;

@Event("hearing.offence-verdict-updated")
public class OffenceVerdictUpdated {

    private UUID caseId;
    private UUID originHearingId;
    private UUID offenceId;
    private UUID verdictId;
    private UUID verdictValueId;
    private String category;
    private String code;
    private String description;

    public OffenceVerdictUpdated() {

    }

    public OffenceVerdictUpdated(UUID caseId,
                                 UUID originHearingId,
                                 UUID offenceId,
                                 UUID verdictId,
                                 UUID verdictValueId,
                                 String category,
                                 String code,
                                 String description) {
        this.caseId = caseId;
        this.originHearingId = originHearingId;
        this.offenceId = offenceId;
        this.verdictId = verdictId;
        this.verdictValueId = verdictValueId;
        this.category = category;
        this.code = code;
        this.description = description;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getOriginHearingId() {
        return originHearingId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public UUID getVerdictId() {
        return verdictId;
    }

    public UUID getVerdictValueId() {
        return verdictValueId;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
