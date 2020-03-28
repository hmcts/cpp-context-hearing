package uk.gov.moj.cpp.hearing.event.nowsdomain.referencedata.resultdefinition;

import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prompt {

    private UUID id;

    private String label;

    private String welshLabel;

    private Boolean mandatory;

    private String resultPromptRule;

    private String type;

    private Integer sequence;

    private String duration;

    private List<String> wordGroup;

    private List<String> userGroups;

    private UUID fixedListId;

    private String qual;

    private String reference;

    private String courtExtract;

    private Integer durationSequence;

    private boolean isAvailableForCourtExtract;

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

    public String getResultPromptRule() {
        return this.resultPromptRule;
    }

    public Prompt setResultPromptRule(final String resultPromptRule) {
        this.resultPromptRule = resultPromptRule;
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
        return Collections.unmodifiableList(ofNullable(this.wordGroup).orElseGet(ArrayList::new));
    }

    public Prompt setWordGroup(List<String> wordGroup) {
        this.wordGroup = Collections.unmodifiableList(ofNullable(wordGroup).orElseGet(ArrayList::new));
        return this;
    }

    public List<String> getUserGroups() {
        return Collections.unmodifiableList(ofNullable(this.userGroups).orElseGet(ArrayList::new));
    }

    public Prompt setUserGroups(List<String> userGroups) {
        this.userGroups = Collections.unmodifiableList(ofNullable(userGroups).orElseGet(ArrayList::new));
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

    public boolean isAvailableForCourtExtract() {
        return isAvailableForCourtExtract;
    }

    public Prompt setAvailableForCourtExtract(boolean availableForCourtExtract) {
        isAvailableForCourtExtract = availableForCourtExtract;
        return this;
    }

    public String getCourtExtract() {
        return courtExtract;
    }

    public Prompt setCourtExtract(final String courtExtract) {
        this.courtExtract = courtExtract;
        return this;
    }

    public String getWelshLabel() {
        return welshLabel;
    }

    public Prompt setWelshLabel(String welshLabel) {
        this.welshLabel = welshLabel;
        return this;
    }

    public String getQual() {
        return qual;
    }

    public Prompt setQual(final String qual) {
        this.qual = qual;
        return this;
    }

    public Integer getDurationSequence() {
        return durationSequence;
    }

    public Prompt setDurationSequence(final Integer durationSequence) {
        this.durationSequence = durationSequence;
        return this;
    }
}
