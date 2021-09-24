package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Objects.nonNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "code",
        "promptOrder",
        "promptRef",
        "label",
        "type",
        "required",
        "hidden",
        "componentType",
        "componentLabel",
        "partName",
        "addressType",
        "listLabel",
        "minLength",
        "maxLength"
})
public class PromptChoice {

    private String code;

    private Integer promptOrder;

    private String promptRef;

    private String label;

    private ResultType type;

    private boolean hidden;


    private boolean required;

    private List<Children> children;

    private Set<String> fixedList;

    @JsonIgnore
    private boolean isVisible = true;

    @JsonIgnore
    private String durationElement;

    @JsonIgnore
    private String welshDurationElement;

    private String componentType;

    private String addressType;

    private String componentLabel;

    private String partName;

    private String listLabel;

    private Set<NameAddress> nameAddressList;

    @JsonIgnore
    private Boolean nameEmail;

    private String minLength;

    private String maxLength;

    @JsonIgnore
    private String referenceDataKey;

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public ResultType getType() {
        return type;
    }

    public final void setType(final ResultType value) {
        type = value;
    }

    public final void setHidden(final boolean value) {
        hidden = value;
    }

    public boolean getHidden() {
        return hidden;
    }

    public boolean getRequired() {
        return required;
    }

    public void setRequired(final boolean required) {
        this.required = required;
    }

    public List<Children> getChildren() {
        return children;
    }

    public final void addChildren(final Children value) {
        if (children == null) {
            children = newArrayList();
        }
        children.add(value);
    }

    public Integer getPromptOrder() {
        return promptOrder;
    }

    public void setPromptOrder(final Integer promptOrder) {
        this.promptOrder = promptOrder;
    }

    @JsonIgnore
    public boolean getVisible() {
        return isVisible;
    }

    public final void setVisible(final boolean value) {
        isVisible = value;
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


    public Set<String> getFixedList() {
        return fixedList;
    }

    public void setFixedList(final Set<String> fixedList) {
        this.fixedList = fixedList;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(final String componentType) {
        this.componentType = componentType;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public void setPromptRef(String promptRef) {
        this.promptRef = promptRef;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(final String addressType) {
        this.addressType = addressType;
    }

    public String getComponentLabel() {
        return componentLabel;
    }

    public void setComponentLabel(String componentLabel) {
        this.componentLabel = componentLabel;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getListLabel() {
        return listLabel;
    }

    public void setListLabel(String listLabel) {
        this.listLabel = listLabel;
    }

    public Set<NameAddress> getNameAddressList() {
        if (nameAddressList != null) {
            return new HashSet<>(nameAddressList);
        } else {
            return new HashSet<>();
        }
    }

    public void setNameAddressList(final Set<NameAddress> nameAddressList) {
        if (nameAddressList != null) {
            this.nameAddressList = new HashSet<>(nameAddressList);
        } else {
            this.nameAddressList = new HashSet<>();
        }
    }

    public Boolean getNameEmail() {
        return nameEmail;
    }

    public void setNameEmail(final Boolean nameEmail) {
        this.nameEmail = nameEmail;
    }

    public String getMinLength() {
        return minLength;
    }

    public void setMinLength(final String minLength) {
        this.minLength = minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(final String maxLength) {
        this.maxLength = maxLength;
    }

    public String getReferenceDataKey() {
        return referenceDataKey;
    }

    public void setReferenceDataKey(final String referenceDataKey) {
        this.referenceDataKey = referenceDataKey;
    }
}
