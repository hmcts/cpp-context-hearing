package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.UUID;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class NowTypes implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private String templateName;

    private String description;

    private Integer rank;

    private String staticText;

    private String welshStaticText;

    private String priority;

    private String jurisdiction;

    private String welshDescription;

    private String bilingualTemplateName;

    private Boolean remotePrintingRequired;

    public static NowTypes nowTypes() {
        return new NowTypes();
    }

    public UUID getId() {
        return this.id;
    }

    public NowTypes setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getTemplateName() {
        return this.templateName;
    }

    public NowTypes setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public NowTypes setDescription(String description) {
        this.description = description;
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

    public String getWelshStaticText() {
        return this.welshStaticText;
    }

    public NowTypes setWelshStaticText(String welshStaticText) {
        this.welshStaticText = welshStaticText;
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

    public String getWelshDescription() {
        return welshDescription;
    }

    public NowTypes setWelshDescription(final String welshDescription) {
        this.welshDescription = welshDescription;
        return this;
    }

    public String getBilingualTemplateName() {
        return bilingualTemplateName;
    }

    public NowTypes setBilingualTemplateName(final String bilingualTemplateName) {
        this.bilingualTemplateName = bilingualTemplateName;
        return this;
    }

    public Boolean getRemotePrintingRequired() {
        return remotePrintingRequired;
    }

    public NowTypes setRemotePrintingRequired(final Boolean remotePrintingRequired) {
        this.remotePrintingRequired = remotePrintingRequired;
        return this;
    }


}
