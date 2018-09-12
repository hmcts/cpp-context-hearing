package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.UUID;


public class NowType implements Serializable {

    private static final long serialVersionUID = 2L;

    private UUID id;

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


    public UUID getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTemplateName() {
        return templateName;
    }

    public Integer getRank() {
        return rank;
    }

    public String getStaticText() {
        return staticText;
    }

    public String getWelshStaticText() {
        return welshStaticText;
    }

    public String getPriority() {
        return priority;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public String getWelshDescription() {
        return welshDescription;
    }

    public String getBilingualTemplateName() {
        return bilingualTemplateName;
    }

    public Boolean getRemotePrintingRequired() {
        return remotePrintingRequired;
    }

    public NowType setId(UUID id) {
        this.id = id;
        return this;
    }

    public NowType setDescription(String description) {
        this.description = description;
        return this;
    }

    public NowType setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public NowType setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public NowType setStaticText(String staticText) {
        this.staticText = staticText;
        return this;
    }

    public NowType setWelshStaticText(String welshStaticText) {
        this.welshStaticText = welshStaticText;
        return this;
    }

    public NowType setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public NowType setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public NowType setWelshDescription(String welshDescription) {
        this.welshDescription = welshDescription;
        return this;
    }

    public NowType setBilingualTemplateName(String bilingualTemplateName) {
        this.bilingualTemplateName = bilingualTemplateName;
        return this;
    }

    public NowType setRemotePrintingRequired(Boolean remotePrintingRequired) {
        this.remotePrintingRequired = remotePrintingRequired;
        return this;
    }

    public static NowType nowType() {
        return new NowType();
    }
}
