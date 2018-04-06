package uk.gov.moj.cpp.hearing.domain.event;

import uk.gov.justice.domain.annotation.Event;

import java.time.LocalDate;
import java.util.UUID;

@Event("hearing.offence-verdict-updated")
public class OffenceVerdictUpdated {

    private UUID caseId;
    private UUID hearingId;
    private UUID offenceId;
    private UUID verdictId;
    private UUID verdictValueId;
    private String category;
    private String code;
    private String description;
    private Integer numberOfJurors;
    private Integer numberOfSplitJurors;
    private Boolean unanimous;
    private LocalDate verdictDate;

    public OffenceVerdictUpdated() {

    }

    public OffenceVerdictUpdated(UUID caseId,
                                 UUID hearingId,
                                 UUID offenceId,
                                 UUID verdictId,
                                 UUID verdictValueId,
                                 String category,
                                 String code,
                                 String description,
                                 Integer numberOfJurors,
                                 Integer numberOfSplitJurors,
                                 Boolean unanimous,
                                 LocalDate verdictDate
                                 ) {
        this.caseId = caseId;
        this.hearingId = hearingId;
        this.offenceId = offenceId;
        this.verdictId = verdictId;
        this.verdictValueId = verdictValueId;
        this.category = category;
        this.code = code;
        this.description = description;
        this.numberOfJurors = numberOfJurors;
        this.numberOfSplitJurors = numberOfSplitJurors;
        this.unanimous = unanimous;
        this.verdictDate = verdictDate;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getHearingId() {
        return hearingId;
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

    public Integer getNumberOfJurors() {
        return numberOfJurors;
    }

    public Integer getNumberOfSplitJurors() {
        return numberOfSplitJurors;
    }

    public Boolean getUnanimous() {
        return unanimous;
    }

    public LocalDate getVerdictDate() {
        return verdictDate;
    }
}
