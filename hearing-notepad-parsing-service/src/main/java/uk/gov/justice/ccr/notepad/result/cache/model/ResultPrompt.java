package uk.gov.justice.ccr.notepad.result.cache.model;


import static java.util.stream.Collectors.toCollection;

import java.util.*;

@SuppressWarnings({"squid:S00107"})
public class ResultPrompt {

    private String id;
    private UUID resultDefinitionId;
    private String resultDefinitionLabel;
    private String label;
    private ResultType type;
    private boolean mandatory;
    private String durationElement;
    private Set<String> keywords;
    private Set<String> fixedList;
    private Integer promptOrder;
    private String reference;

    public ResultPrompt() {

    }

    public ResultPrompt(final String id, final UUID resultDefinitionId, final String resultDefinitionLabel,
                        final String label, final ResultType type, final boolean mandatory,
                        final String durationElement, final Set<String> keywords,
                        final Set<String> fixedList, final Integer promptOrder, final String reference) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.resultDefinitionLabel = resultDefinitionLabel;
        this.label = label;
        this.type = type;
        this.mandatory = mandatory;
        this.durationElement = durationElement;
        this.keywords = keywords;
        this.fixedList = fixedList;
        this.promptOrder = promptOrder;
        this.reference = reference;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UUID getResultDefinitionId() {
        return resultDefinitionId;
    }

    public void setResultDefinitionId(final UUID resultDefinitionId) {
        this.resultDefinitionId = resultDefinitionId;
    }

    public String getResultDefinitionLabel() {
        return resultDefinitionLabel;
    }

    public final void setResultDefinitionLabel(String value) {
        resultDefinitionLabel = value;
    }

    public String getLabel() {
        return label;
    }

    public final void setLabel(String value) {
        label = value;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(ResultType value) {
        type = value;
    }

    public boolean isMandatory() {
        return mandatory;
    }

    public final void setMandatory(boolean value) {
        mandatory = value;
    }

    public String getDurationElement() {
        return durationElement;
    }

    public final void setDurationElement(String value) {
        durationElement = value;
    }

    public final Set<String> getKeywords() {
        return Optional.ofNullable(keywords).orElse(new HashSet<>());
    }

    public final void setKeywords(List<String> keywords) {
        if (!keywords.isEmpty()) {
            this.keywords = keywords.stream().filter(v -> !v.isEmpty()).distinct().collect(toCollection(TreeSet::new));
        }
    }

    public final void setFixedList(Set<String> fixedList) {
        this.fixedList = fixedList;
    }

    public Set<String> getFixedList() {
        return fixedList;
    }

    public Integer getPromptOrder() {
        return promptOrder;
    }

    public void setPromptOrder(Integer promptOrder) {
        this.promptOrder = promptOrder;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return "ResultPrompt{" +
                "id='" + id + '\'' +
                ", resultDefinitionLabel='" + resultDefinitionLabel + '\'' +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", mandatory='" + mandatory + '\'' +
                ", durationElement='" + durationElement + '\'' +
                ", keywords=" + keywords +
                ", fixedList=" + fixedList +
                ", promptOrder=" + promptOrder +
                ", reference='" + reference + '\'' +
                '}';
    }
}
