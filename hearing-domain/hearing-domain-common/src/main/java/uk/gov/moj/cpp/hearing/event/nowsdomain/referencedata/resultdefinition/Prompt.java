package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import java.util.List;
import java.util.UUID;

public class Prompt {

    private UUID id;

    private String label;

    private Boolean mandatory;

    private String type;

    private Integer sequence;

    private String duration;

    private List<String> wordGroup;

    private List<String> userGroups;

    private UUID fixedListId;

    private String reference;

    public static Prompt prompt() {
        return new Prompt();
    }

    public UUID getId() {
        return this.id;
    }

    public Prompt setId(UUID id) {
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

    public List<String> getWordGroup() {
        return this.wordGroup;
    }

    public Prompt setWordGroup(List<String> wordGroup) {
        this.wordGroup = wordGroup;
        return this;
    }

    public List<String> getUserGroups() {
        return this.userGroups;
    }

    public Prompt setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
        return this;
    }

    public UUID getFixedListId() {
        return this.fixedListId;
    }

    public Prompt setFixedListId(UUID fixedListId) {
        this.fixedListId = fixedListId;
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
