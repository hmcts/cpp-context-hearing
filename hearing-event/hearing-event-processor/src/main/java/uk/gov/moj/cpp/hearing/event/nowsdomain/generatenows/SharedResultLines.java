package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class SharedResultLines {

    private String id;

    private java.util.UUID caseId;

    private java.util.UUID defendantId;

    private java.util.UUID offenceId;

    private String level;

    private String label;

    private Integer rank;

    private java.util.List<Prompts> prompts;

    public static SharedResultLines sharedResultLines() {
        return new SharedResultLines();
    }

    public String getId() {
        return this.id;
    }

    public SharedResultLines setId(String id) {
        this.id = id;
        return this;
    }

    public java.util.UUID getCaseId() {
        return this.caseId;
    }

    public SharedResultLines setCaseId(java.util.UUID caseId) {
        this.caseId = caseId;
        return this;
    }

    public java.util.UUID getDefendantId() {
        return this.defendantId;
    }

    public SharedResultLines setDefendantId(java.util.UUID defendantId) {
        this.defendantId = defendantId;
        return this;
    }

    public java.util.UUID getOffenceId() {
        return this.offenceId;
    }

    public SharedResultLines setOffenceId(java.util.UUID offenceId) {
        this.offenceId = offenceId;
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

    public java.util.List<Prompts> getPrompts() {
        return this.prompts;
    }

    public SharedResultLines setPrompts(java.util.List<Prompts> prompts) {
        this.prompts = prompts;
        return this;
    }
}
