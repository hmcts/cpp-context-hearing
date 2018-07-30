package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class SharedResultLines implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private UUID defendantId;

    private UUID caseId;

    private UUID offenceId;

    private ZonedDateTime sharedDate;

    private LocalDate orderedDate;

    private String level;

    private String label;

    private Integer rank;

    private List<Prompts> prompts;

    public static SharedResultLines sharedResultLines() {
        return new SharedResultLines();
    }

    public UUID getId() {
        return this.id;
    }

    public SharedResultLines setId(UUID id) {
        this.id = id;
        return this;
    }

    public UUID getDefendantId() {
        return this.defendantId;
    }

    public SharedResultLines setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getCaseId() {
        return this.caseId;
    }

    public SharedResultLines setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getOffenceId() {
        return this.offenceId;
    }

    public SharedResultLines setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public ZonedDateTime getSharedDate() {
        return this.sharedDate;
    }

    public SharedResultLines setSharedDate(ZonedDateTime sharedDate) {
        this.sharedDate = sharedDate;
        return this;
    }

    public LocalDate getOrderedDate() {
        return this.orderedDate;
    }

    public SharedResultLines setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
        return this;
    }

    public String getLevel() {
        return this.level;
    }

    public SharedResultLines setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public SharedResultLines setLabel(String label) {
        this.label = label;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public SharedResultLines setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public List<Prompts> getPrompts() {
        return this.prompts;
    }

    public SharedResultLines setPrompts(List<Prompts> prompts) {
        this.prompts = prompts;
        return this;
    }
}
