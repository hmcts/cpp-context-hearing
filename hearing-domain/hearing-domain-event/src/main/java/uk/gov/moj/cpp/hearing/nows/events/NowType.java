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

    public static NowType nowType() {
        return new NowType();
    }

    public UUID getId() {
        return id;
    }

    public NowType setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public NowType setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public NowType setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public Integer getRank() {
        return rank;
    }

    public NowType setRank(Integer rank) {
        this.rank = rank;
        return this;
    }

    public String getStaticText() {
        return staticText;
    }

    public NowType setStaticText(String staticText) {
        this.staticText = staticText;
        return this;
    }

    public String getWelshStaticText() {
        return welshStaticText;
    }

    public NowType setWelshStaticText(String welshStaticText) {
        this.welshStaticText = welshStaticText;
        return this;
    }

    public String getPriority() {
        return priority;
    }

    public NowType setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public String getJurisdiction() {
        return jurisdiction;
    }

    public NowType setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
        return this;
    }

    public String getWelshDescription() {
        return welshDescription;
    }

    public NowType setWelshDescription(String welshDescription) {
        this.welshDescription = welshDescription;
        return this;
    }

    public String getBilingualTemplateName() {
        return bilingualTemplateName;
    }

    public NowType setBilingualTemplateName(String bilingualTemplateName) {
        this.bilingualTemplateName = bilingualTemplateName;
        return this;
    }

    public Boolean getRemotePrintingRequired() {
        return remotePrintingRequired;
    }

    public NowType setRemotePrintingRequired(Boolean remotePrintingRequired) {
        this.remotePrintingRequired = remotePrintingRequired;
        return this;
    }
}
