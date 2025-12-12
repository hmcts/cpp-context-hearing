package uk.gov.moj.cpp.hearing.message.shareResults;

import uk.gov.moj.cpp.hearing.command.result.CourtClerk;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedResultLine {

    private UUID id;
    private ZonedDateTime lastSharedDateTime;
    private LocalDate orderedDate;
    private UUID caseId;
    private UUID defendantId;
    private UUID offenceId;
    private String level;
    private String label;
    private Integer rank;
    private CourtClerk courtClerk;

    private List<Prompt> prompts = new ArrayList<>();

    public static SharedResultLine sharedResultLine() {
        return new SharedResultLine();
    }

    public UUID getId() {
        return id;
    }

    public SharedResultLine setId(final UUID id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getLastSharedDateTime() {
        return lastSharedDateTime;
    }

    public SharedResultLine setLastSharedDateTime(final ZonedDateTime lastSharedDateTime) {
        this.lastSharedDateTime = lastSharedDateTime;
        return this;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public SharedResultLine setCaseId(final UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public SharedResultLine setDefendantId(final UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public SharedResultLine setOffenceId(final UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public SharedResultLine setLevel(final String level) {
        this.level = level;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public SharedResultLine setLabel(final String label) {
        this.label = label;
        return this;
    }

    public Integer getRank() {
        return rank;
    }

    public SharedResultLine setRank(final Integer rank) {
        this.rank = rank;
        return this;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public SharedResultLine setPrompts(final List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public CourtClerk getCourtClerk() {
        return courtClerk;
    }

    public SharedResultLine setCourtClerk(final CourtClerk courtClerk) {
        this.courtClerk = courtClerk;
        return this;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public SharedResultLine setOrderedDate(final LocalDate orderedDate) {
        this.orderedDate = orderedDate;
        return this;
    }
}

