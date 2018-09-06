package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;


public class NowType implements Serializable {

    private static final long serialVersionUID = -8533556522837735944L;
    
    private String id;
    private String description;
    private String templateName;
    private Integer rank;
    private String staticText;
    private String welshStaticText;
    private String priority;
    private String jurisdiction;
    private String welshDescription;
    private String bilingualTemplateName;
    private Boolean remotePrintingRequired;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public String getStaticText() {
        return staticText;
    }

    public void setStaticText(String staticText) {
        this.staticText = staticText;
    }

    public String getWelshStaticText() {
        return welshStaticText;
    }

    public void setWelshStaticText(String welshStaticText) {
        this.welshStaticText = welshStaticText;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getWelshDescription() {
        return welshDescription;
    }

    public void setWelshDescription(final String welshDescription) {
        this.welshDescription = welshDescription;
    }

    public String getBilingualTemplateName() {
        return bilingualTemplateName;
    }

    public void setBilingualTemplateName(final String bilingualTemplateName) {
        this.bilingualTemplateName = bilingualTemplateName;
    }

    public Boolean getRemotePrintingRequired() {
        return remotePrintingRequired;
    }

    public void setRemotePrintingRequired(final Boolean remotePrintingRequired) {
        this.remotePrintingRequired = remotePrintingRequired;
    }
}
