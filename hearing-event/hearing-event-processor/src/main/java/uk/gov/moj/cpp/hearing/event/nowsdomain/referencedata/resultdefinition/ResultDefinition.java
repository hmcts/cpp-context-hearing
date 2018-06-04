package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

public class ResultDefinition {

    private java.util.UUID id;

    private String label;

    private String shortCode;

    private String level;

    private Integer rank;

    private String version;

    private java.util.List<Prompt> prompts;

    private java.util.Date startDate;

    private java.util.Date endDate;

    private java.util.List<String> userGroups;

    public static ResultDefinition resultDefinition() {
        return new ResultDefinition();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public ResultDefinition setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public ResultDefinition setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getShortCode() {
        return this.shortCode;
    }

    public ResultDefinition setShortCode(String shortCode) {
        this.shortCode = shortCode;
        return this;
    }

    public String getLevel() {
        return this.level;
    }

    public ResultDefinition setLevel(String level) {
        this.level = level;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public ResultDefinition setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public ResultDefinition setVersion(String version) {
        this.version = version;
        return this;
    }

    public java.util.List<Prompt> getPrompts() {
        return this.prompts;
    }

    public ResultDefinition setPrompts(java.util.List<Prompt> prompts) {
        this.prompts = prompts;
        return this;
    }

    public java.util.Date getStartDate() {
        return this.startDate;
    }

    public ResultDefinition setStartDate(java.util.Date startDate) {
        this.startDate = startDate;
        return this;
    }

    public java.util.Date getEndDate() {
        return this.endDate;
    }

    public ResultDefinition setEndDate(java.util.Date endDate) {
        this.endDate = endDate;
        return this;
    }

    public java.util.List<String> getUserGroups() {
        return this.userGroups;
    }

    public ResultDefinition setUserGroups(java.util.List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }
}
