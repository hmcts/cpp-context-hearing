package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class NowTypes {

    private java.util.UUID id;

    private String description;

    private String templateName;

    private Integer rank;

    private String staticText;

    private String staticTextWelsh;

    private String priority;

    private String jurisdiction;

    public static NowTypes nowTypes() {
        return new NowTypes();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public NowTypes setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public NowTypes setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public NowTypes setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public Integer getRank() {
        return this.rank;
    }

    public NowTypes setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getStaticText() {
        return this.staticText;
    }

    public NowTypes setStaticText(String staticText) {
        this.staticText = staticText;
        return this;
    }

    public String getStaticTextWelsh() {
        return this.staticTextWelsh;
    }

    public NowTypes setStaticTextWelsh(String staticTextWelsh) {
        this.staticTextWelsh = staticTextWelsh;
        return this;
    }

    public String getPriority() {
        return this.priority;
    }

    public NowTypes setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public NowTypes setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }
}
