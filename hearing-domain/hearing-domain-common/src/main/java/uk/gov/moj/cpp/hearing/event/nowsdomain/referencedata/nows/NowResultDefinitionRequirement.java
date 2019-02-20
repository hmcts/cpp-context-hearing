package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.nows;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NowResultDefinitionRequirement {

    private UUID id;

    private Boolean mandatory;

    private Boolean primary;

    private Integer sequence;

    private String text;

    private String welshText;

    private String nowReference;

    public static NowResultDefinitionRequirement resultDefinitions() {
        return new NowResultDefinitionRequirement();
    }

    public UUID getId() {
        return this.id;
    }

    public String getNowReference() {
        return nowReference;
    }

    public NowResultDefinitionRequirement setId(UUID id) {
        this.id = id;
        return this;
    }

    public Boolean getMandatory() {
        return this.mandatory;
    }

    public NowResultDefinitionRequirement setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public Boolean getPrimary() {
        return this.primary;
    }

    public NowResultDefinitionRequirement setPrimary(Boolean primary) {
        this.primary = primary;
        return this;
    }

    public Integer getSequence() {
        return this.sequence;
    }

    public NowResultDefinitionRequirement setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    public String getText() {
        return text;
    }

    public NowResultDefinitionRequirement setText(String text) {
        this.text = text;
        return this;
    }

    public String getWelshText() {
        return welshText;
    }

    public NowResultDefinitionRequirement setWelshText(String welshText) {
        this.welshText = welshText;
        return this;
    }

    public NowResultDefinitionRequirement setNowReference(String nowReference) {
        this.nowReference = nowReference;
        return this;
    }
}
