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
    private Set<ResultPromptDynamicListNameAddress> nameAddressList;
    private Integer promptOrder;
    private String reference;
    private Integer durationSequence;
    private Boolean hidden;
    private String componentLabel;
    private String listLabel;
    private String addressType;
    private String partName;
    private Boolean nameEmail;
    private String welshDurationElement;

    public ResultPrompt() {

    }

    public ResultPrompt(final String id, final UUID resultDefinitionId, final String resultDefinitionLabel,
                        final String label, final ResultType type, final String resultPromptRule,
                        final String durationElement, final Set<String> keywords,
                        final Set<String> fixedList, Set<ResultPromptDynamicListNameAddress> nameAddressList, final Integer promptOrder,
                        final String reference, final Integer durationSequence, final Boolean hidden,
                        final String componentLabel, final String listLabel, final String addressType, final String partName, final Boolean nameEmail, final String welshDurationElement) {
        this.id = id;
        this.resultDefinitionId = resultDefinitionId;
        this.resultDefinitionLabel = resultDefinitionLabel;
        this.label = label;
        this.type = type;
        this.resultPromptRule = resultPromptRule;
        this.durationElement = durationElement;
        this.keywords = keywords;
        this.fixedList = fixedList;
        this.nameAddressList = nameAddressList;
        this.promptOrder = promptOrder;
        this.reference = reference;
        this.durationSequence = durationSequence;
        this.hidden = hidden;
        this.componentLabel = componentLabel;
        this.listLabel = listLabel;
        this.addressType = addressType;
        this.partName = partName;
        this.nameEmail = nameEmail;
        this.welshDurationElement = welshDurationElement;
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

    public String getWelshDurationElement() {
        return welshDurationElement;
    }

    public final void setWelshDurationElement(final String value) {
        welshDurationElement = value;
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

    public Set<ResultPromptDynamicListNameAddress> getNameAddressList() {
        if (nameAddressList != null) {
            return new HashSet<>(nameAddressList);
        } else {
            return new HashSet<>();
        }
    }

    public void setNameAddressList(final Set<ResultPromptDynamicListNameAddress> nameAddressList) {
        if (nameAddressList != null) {
            this.nameAddressList = new HashSet<>(nameAddressList);
        }
    }

    public String getComponentLabel() {
        return componentLabel;
    }

    public void setComponentLabel(String componentLabel) {
        this.componentLabel = componentLabel;
    }

    public String getListLabel() {
        return listLabel;
    }

    public void setListLabel(String listLabel) {
        this.listLabel = listLabel;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public Boolean getNameEmail() {
        return nameEmail;
    }

    public void setNameEmail(Boolean nameEmail) {
        this.nameEmail = nameEmail;
    }

    @Override
    public String toString() {
        if(nameAddressList != null) {
            return "ResultPrompt{" +
                    "id='" + id + '\'' +
                    ", resultDefinitionLabel='" + resultDefinitionLabel + '\'' +
                    ", label='" + label + '\'' +
                    ", type=" + type +
                    ", resultPromptRule='" + resultPromptRule + '\'' +
                    ", durationElement='" + durationElement + '\'' +
                    ", welshDurationElement='" + welshDurationElement + '\'' +
                    ", keywords=" + keywords +
                    ", fixedList=" + fixedList +
                    ", promptOrder=" + promptOrder +
                    ", reference='" + reference + '\'' +
                    ", hidden='" + hidden + '\'' +
                    ", durationSequence='" + durationSequence + '\'' +
                    ", nameAddressList='" + nameAddressList.toString() + '\'' +
                    ", componentLabel='" + componentLabel + '\'' +
                    ", listLabel='" + listLabel + '\'' +
                    ", addressType='" + addressType + '\'' +
                    ", partName='" + partName + '\'' +
                    ", nameEmail='" + nameEmail + '\'' +
                    '}';
        } else {
            return "ResultPrompt{" +
                    "id='" + id + '\'' +
                    ", resultDefinitionLabel='" + resultDefinitionLabel + '\'' +
                    ", label='" + label + '\'' +
                    ", type=" + type +
                    ", resultPromptRule='" + resultPromptRule + '\'' +
                    ", durationElement='" + durationElement + '\'' +
                    ", welshDurationElement='" + welshDurationElement + '\'' +
                    ", keywords=" + keywords +
                    ", fixedList=" + fixedList +
                    ", promptOrder=" + promptOrder +
                    ", reference='" + reference + '\'' +
                    ", hidden='" + hidden + '\'' +
                    ", durationSequence='" + durationSequence + '\'' +
                    ", componentLabel='" + componentLabel + '\'' +
                    ", listLabel='" + listLabel + '\'' +
                    ", addressType='" + addressType + '\'' +
                    ", partName='" + partName + '\'' +
                    ", nameEmail='" + nameEmail + '\'' +
                    '}';
        }
    }
}
