package uk.gov.justice.ccr.notepad.result.cache.model;


import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ResultPrompt {

    private String id ;
    private String resultDefinitionLabel;
    private String label;
    private ResultType type;
    private String mandatory;
    private String durationElement;
    private Set<String> keywords = new TreeSet<>();
    private Set<String> fixedList;
    private Integer promptOrder;

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

    public String getMandatory() {
        return mandatory;
    }

    public final void setMandatory(String value) {
        mandatory = value;
    }

    public String getId() {
        return id;
    }

    public String getDurationElement() {
        return durationElement;
    }

    public final void setDurationElement(String value) {
        durationElement = value;
    }

    public final Set<String> getKeywords() {
        return keywords;
    }

    public final void setKeywords(List<String> keywords) {
        if (!keywords.isEmpty()) {
            this.keywords = new TreeSet<>(keywords.stream().filter(v -> !v.isEmpty()).collect(Collectors.toSet()));
        }
    }

    public final void setFixedList(Set<String> fixedList) {
        this.fixedList = fixedList;
    }

    public Set<String> getFixedList() {
        return fixedList;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getPromptOrder() {
        return promptOrder;
    }

    public void setPromptOrder(Integer promptOrder) {
        this.promptOrder = promptOrder;
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
                '}';
    }
}
