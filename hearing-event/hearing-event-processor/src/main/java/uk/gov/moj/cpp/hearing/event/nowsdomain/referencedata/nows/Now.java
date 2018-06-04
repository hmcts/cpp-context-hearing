package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

public class Now {

    private java.util.UUID id;

    private String version;

    private String name;

    private String templateName;

    private String jurisdiction;

    private Integer rank;

    private String text;

    private String welshText;

    private Integer urgentTimeLimitInMinutes;

    private java.util.List<ResultDefinitions> resultDefinitions;

    public static Now now() {
        return new Now();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Now setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public String getVersion() {
        return this.version;
    }

    public Now setVersion(String version) {
        this.version = version;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Now setName(String name) {
        this.name = name;
        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public Now setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public Now setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public Now setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getText() {
        return this.text;
    }

    public Now setText(String text) {
        this.text = text;
        return this;
    }

    public String getWelshText() {
        return this.welshText;
    }

    public Now setWelshText(String welshText) {
        this.welshText = welshText;
        return this;
    }

    public Integer getUrgentTimeLimitInMinutes() {
        return this.urgentTimeLimitInMinutes;
    }

    public Now setUrgentTimeLimitInMinutes(Integer urgentTimeLimitInMinutes) {
        this.urgentTimeLimitInMinutes = urgentTimeLimitInMinutes;
        return this;
    }

    public java.util.List<ResultDefinitions> getResultDefinitions() {
        return this.resultDefinitions;
    }

    public Now setResultDefinitions(java.util.List<ResultDefinitions> resultDefinitions) {
        this.resultDefinitions = resultDefinitions;
        return this;
    }
}
