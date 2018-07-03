package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;
import java.lang.String;

public class NowTypes  implements Serializable {

    private UUID id;

    private String templateName;

    private String description;

    private Integer rank;

    private String staticText;

    private String staticTextWelsh;

    private String priority;

    private String jurisdiction;

    public UUID getId() {
        return this.id;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getRank() {
        return this.rank;
    }

    public String getStaticText() {
        return this.staticText;
    }

    public String getStaticTextWelsh() {
        return this.staticTextWelsh;
    }

    public String getPriority() {
        return this.priority;
    }

    public String getJurisdiction() {
        return this.jurisdiction;
    }

    public NowTypes setId(UUID id) {
        this.id=id;
        return this;
    }

    public NowTypes setTemplateName(String templateName) {
        this.templateName=templateName;
        return this;
    }

    public NowTypes setDescription(String description) {
        this.description=description;
        return this;
    }

    public NowTypes setRank(Integer rank) {
        this.rank=rank;
        return this;
    }

    public NowTypes setStaticText(String staticText) {
        this.staticText=staticText;
        return this;
    }

    public NowTypes setStaticTextWelsh(String staticTextWelsh) {
        this.staticTextWelsh=staticTextWelsh;
        return this;
    }

    public NowTypes setPriority(String priority) {
        this.priority=priority;
        return this;
    }

    public NowTypes setJurisdiction(String jurisdiction) {
        this.jurisdiction=jurisdiction;
        return this;
    }

    public static NowTypes nowTypes() {
        return new NowTypes();
    }
}
