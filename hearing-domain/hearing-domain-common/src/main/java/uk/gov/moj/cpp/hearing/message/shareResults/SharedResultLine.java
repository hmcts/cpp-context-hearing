package uk.gov.moj.cpp.hearing.message.shareResults;

import uk.gov.moj.cpp.hearing.command.result.CourtClerk;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedResultLine {

    private UUID id;
    private ZonedDateTime lastSharedDateTime;
    private UUID caseId;
    private UUID defendantId;
    private UUID offenceId;
    private String level;
    private String label;
    private Integer rank;
    private CourtClerk courtClerk;

    private List<Prompt> prompts = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public String getLevel() {
        return level;
    }

    public String getLabel() {
        return label;
    }

    public Integer getRank() {
        return rank;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public SharedResultLine setId(UUID id) {
        this.id = id;
        return this;
    }

    public SharedResultLine setLastSharedDateTime(ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
        return this;
    }

    public SharedResultLine setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public SharedResultLine setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public SharedResultLine setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public SharedResultLine setLevel(String level) {
        this.level = level;
        return this;
    }

    public SharedResultLine setLabel(String label) {
        this.label = label;
        return this;
    }

    public SharedResultLine setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public SharedResultLine setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public SharedResultLine setCourtClerk(final CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public static SharedResultLine sharedResultLine() {
        return new SharedResultLine();
    }
}

