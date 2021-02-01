package uk.gov.justice.ccr.notepad.view;

import static com.google.common.collect.Lists.newArrayList;

import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "welshLabel",
        "type"
})
@SuppressWarnings("squid:S2384")
public class Children {
    private String label;
    private String welshLabel;
    private ResultType type;
    private String promptRef;
    private Set<String> fixedList;
    private Set<NameAddress> nameAddressList;
    private String listLabel;
    private String addressType;
    private Boolean nameEmail;
    private String partName;
    private int sequence;
    private String code;

    @JsonProperty(value = "children")
    private List<Children> childrenList;


    public Children(final String label, final String code, final String promptRef,final ResultType type,final Set<String> fixedList,final List<Children> childrenList) {
        this.label = label;
        this.code = code;
        this.promptRef = promptRef;
        this.type = type;
        this.fixedList = fixedList;
        this.childrenList = childrenList;
    }

    public Children(final String label,final ResultType type,final Set<String> fixedList,final List<Children> childrenList, final String code) {
        this.label = label;
        this.type = type;
        this.fixedList = fixedList;
        this.childrenList = childrenList;
        this.code = code;
    }

    public Children(final String label,  final String promptRef, final ResultType type, final String partName) {
        this.label = label;
        this.promptRef = promptRef;
        this.type = type;
        this.partName = partName;
    }

    public Children(final String code, final String label,  final String promptRef, final ResultType type, final String partName) {
        this.code = code;
        this.label = label;
        this.promptRef = promptRef;
        this.type = type;
        this.partName = partName;
    }

    public Children(final String code, final String label, final String promptRef, final String welshLabel, final ResultType type) {
        this.code = code;
        this.label = label;
        this.promptRef = promptRef;
        this.type = type;
        this.welshLabel = welshLabel;
    }

    public Children(final String label, final String promptRef, final ResultType type, final int sequenceNumber, final String partName) {
        this.label = label;
        this.type = type;
        this.promptRef = promptRef;
        this.sequence = sequenceNumber;
        this.partName = partName;
    }

    public String getLabel() { return label; }

    public String getWelshLabel() { return welshLabel; }

    public ResultType getType() { return type; }

    public Set<String> getFixedList() { return fixedList; }

    public void setFixedList(final Set<String> fixedList) { this.fixedList = fixedList; }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    @JsonProperty(value = "children")
    public List<Children> getChildrenList() { return childrenList; }

    public final void addChildrenList(final Children value) {
        if (childrenList == null) {
            childrenList = newArrayList();
        }
        childrenList.add(value);
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getPromptRef() {
        return promptRef;
    }

    public void setPromptRef(String promptRef) {
        this.promptRef = promptRef;
    }

    public Set<NameAddress> getNameAddressList() {
        return nameAddressList;
    }

    public void setNameAddressList(final Set<NameAddress> nameAddressList) {
        this.nameAddressList = nameAddressList;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setWelshLabel(String welshLabel) {
        this.welshLabel = welshLabel;
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

    public Boolean getNameEmail() {
        return nameEmail;
    }

    public void setNameEmail(Boolean nameEmail) {
        this.nameEmail = nameEmail;
    }

    public String getPartName() {
        return partName;
    }

    public void setPartName(final String partName) {
        this.partName = partName;
    }
}
