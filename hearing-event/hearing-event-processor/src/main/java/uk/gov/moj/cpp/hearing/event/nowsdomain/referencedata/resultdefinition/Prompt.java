package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

public class Prompt {

    private java.util.UUID id;

    private String label;

    private java.util.List<String> userGroups;

    private Boolean mandatory;

    private String type;

    private Integer sequence;

    private String duration;

    private String reference;

    public static Prompt prompt() {
        return new Prompt();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Prompt setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public Prompt setLabel(String label) {
        this.label = label;
        return this;
    }

    public java.util.List<String> getUserGroups() {
        return this.userGroups;
    }

    public Prompt setUserGroups(java.util.List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }

    public Boolean getMandatory() {
        return this.mandatory;
    }

    public Prompt setMandatory(Boolean mandatory) {
        this.mandatory = mandatory;
        return this;
    }

    public String getType() {
        return this.type;
    }

    public Prompt setType(String type) {
        this.type = type;
        return this;
    }

    public Integer getSequence() {
        return this.sequence;
    }

    public Prompt setSequence(Integer sequence) {
        this.sequence = sequence;
        return this;
    }

    public String getDuration() {
        return this.duration;
    }

    public Prompt setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    public String getReference() {
        return this.reference;
    }

    public Prompt setReference(String reference) {
        this.reference = reference;
        return this;
    }
}
