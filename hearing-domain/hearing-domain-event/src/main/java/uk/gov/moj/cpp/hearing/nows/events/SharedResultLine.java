package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SharedResultLine implements Serializable {
    private final static long serialVersionUID = 2L;

    private UUID id;
    private UUID caseId;
    private UUID defendantId;
    private UUID offenceId;
    private ZonedDateTime sharedDate;
    private LocalDate orderedDate;
    private String level;
    private String label;
    private Integer rank;
    private List<Prompt> prompts = new ArrayList<Prompt>();

    public static SharedResultLine sharedResultLine() {
        return new SharedResultLine();
    }

    public UUID getId() {
        return id;
    }

    public SharedResultLine setId(UUID id) {
        this.id = id;
        return this;
    }

    public ZonedDateTime getSharedDate() {
        return sharedDate;
    }

    public SharedResultLine setSharedDate(ZonedDateTime sharedDate) {
        this.sharedDate = sharedDate;
        return this;
    }

    public LocalDate getOrderedDate() {
        return orderedDate;
    }

    public SharedResultLine setOrderedDate(LocalDate orderedDate) {
        this.orderedDate = orderedDate;
        return this;
    }

    public UUID getCaseId() {
        return caseId;
    }

    public SharedResultLine setCaseId(UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public UUID getDefendantId() {
        return defendantId;
    }

    public SharedResultLine setDefendantId(UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public UUID getOffenceId() {
        return offenceId;
    }

    public SharedResultLine setOffenceId(UUID offenceId) {
        this.offenceId = offenceId;
        return this;
    }

    public String getLevel() {
        return level;
    }

    public SharedResultLine setLevel(String level) {
        this.level = level;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public SharedResultLine setLabel(String label) {
        this.label = label;
        return this;
    }

    public Integer getRank() {
        return rank;
    }

    public SharedResultLine setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public List<Prompt> getPrompts() {
        return prompts;
    }

    public SharedResultLine setPrompts(List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

}
