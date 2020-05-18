package uk.gov.justice.ccr.notepad.result.cache.model;


import static java.util.stream.Collectors.toCollection;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@SuppressWarnings({"squid:S00107"})
public class ResultPrompt {

    private String id;
    private UUID resultDefinitionId;
    private String resultDefinitionLabel;
    private String label;
    private ResultType type;
    private String resultPromptRule;
    private String durationElement;
    private Set<String> keywords;
    private Set<String> fixedList;
    private Integer promptOrder;
    private String reference;
    private Integer durationSequence;
    private Boolean hidden;

    public ResultPrompt() {

    }

    public ResultPrompt(final String id, final UUID resultDefinitionId, final String resultDefinitionLabel,
                        final String label, final ResultType type, final String resultPromptRule,
                        final String durationElement, final Set<String> keywords,
                        final Set<String> fixedList, final Integer promptOrder,
                        final String reference, final Integer durationSequence, final Boolean hidden) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.resultDefinitionLabel = resultDefinitionLabel;
        this.label = label;
        this.type = type;
        this.resultPromptRule = resultPromptRule;
        this.durationElement = durationElement;
        this.keywords = keywords;
        this.fixedList = fixedList;
        this.promptOrder = promptOrder;
        this.reference = reference;
        this.durationSequence = durationSequence;
        this.hidden = hidden;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
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

    public final void setResultDefinitionLabel(final String value) {
        resultDefinitionLabel = value;
    }

    public String getLabel() {
        return label;
    }

    public final void setLabel(final String value) {
        label = value;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(final ResultType value) {
        type = value;
    }

    public String getResultPromptRule() {
        return resultPromptRule;
    }

    public final void setResultPromptRule(final String value) {
        resultPromptRule = value;
    }

    public String getDurationElement() {
        return durationElement;
    }

    public final void setDurationElement(final String value) {
        durationElement = value;
    }

    public final Set<String> getKeywords() {
        return Optional.ofNullable(keywords).orElse(new HashSet<>());
    }

    public final void setKeywords(final List<String> keywords) {
        if (!keywords.isEmpty()) {
            this.keywords = keywords.stream().filter(v -> !v.isEmpty()).distinct().collect(toCollection(TreeSet::new));
        }
    }

    public Set<String> getFixedList() {
        return fixedList;
    }

    public final void setFixedList(final Set<String> fixedList) {
        this.fixedList = fixedList;
    }

    public Integer getPromptOrder() {
        return promptOrder;
    }

    public void setPromptOrder(final Integer promptOrder) {
        this.promptOrder = promptOrder;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public Integer getDurationSequence() {
        return durationSequence;
    }

    public void setDurationSequence(final Integer durationSequence) {
        this.durationSequence = durationSequence;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(final Boolean hidden) {
        this.hidden = hidden;
    }

    @Override
    public String toString() {
        return "ResultPrompt{" +
                "id='" + id + '\'' +
                ", resultDefinitionLabel='" + resultDefinitionLabel + '\'' +
                ", label='" + label + '\'' +
                ", type=" + type +
                ", resultPromptRule='" + resultPromptRule + '\'' +
                ", durationElement='" + durationElement + '\'' +
                ", keywords=" + keywords +
                ", fixedList=" + fixedList +
                ", promptOrder=" + promptOrder +
                ", reference='" + reference + '\'' +
                ", hidden='" + hidden + '\'' +
                ", durationSequence='" + durationSequence + '\'' +
                '}';
    }
}
