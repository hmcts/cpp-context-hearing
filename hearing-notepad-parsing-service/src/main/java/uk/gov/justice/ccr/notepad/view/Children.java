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
        "type",
        "minLength",
        "maxLength"
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
    private Boolean required;
    private String minLength;
    private String maxLength;

    @JsonProperty(value = "children")
    private List<Children> childrenList;

    public Children(){}

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

    public Children(final String label, final String promptRef, final ResultType type,  final String partName, final Boolean required, final String minLength, final String maxLength ) {
        this.label = label;
        this.type = type;
        this.promptRef = promptRef;
        this.partName = partName;
        this.required = required;
        this.minLength = minLength;
        this.maxLength = maxLength;

    }

    public Children(final String label, final String promptRef, final ResultType type,  final String partName, final String addressType, final Boolean required, final String minLength, final String maxLength ) {
        this.label = label;
        this.type = type;
        this.promptRef = promptRef;
        this.partName = partName;
        this.addressType = addressType;
        this.required = required;
        this.minLength = minLength;
        this.maxLength = maxLength;

    }

    public Children(final String code, final String label, final String promptRef, final ResultType type,  final String partName, final Boolean required, final String minLength, final String maxLength ) {
        this.code = code;
        this.label = label;
        this.type = type;
        this.promptRef = promptRef;
        this.partName = partName;
        this.required = required;
        this.minLength = minLength;
        this.maxLength = maxLength;

    }

    public Children withCode(String code){
        this.code = code;
        return this;
    }

    public Children withLabel(String label){
        this.label = label;
        return this;
    }
    public Children withType(ResultType type){
        this.type = type;
        return this;
    }
    public Children withPromptRef(String promptRef){
        this.promptRef = promptRef;
        return this;
    }
    public Children withPartName(String partName){
        this.partName = partName;
        return this;
    }
    public Children withRequired(boolean required){
        this.required = required;
        return this;
    }
    public Children withMinLength(String minLength){
        this.minLength = minLength;
        return this;
    }

    public Children withMaxLength(String maxLength){
        this.maxLength = maxLength;
        return this;
    }

    public Children withFixedList(Set<String> fixedList){
        this.fixedList = fixedList;
        return this;
    }
    public Children withAddressType(String addressType){
        this.addressType = addressType;
        return this;
    }
    public Children withChildrenList(List<Children> childrenList){
        this.childrenList = childrenList;
        return this;
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

    public void setType(ResultType type) {
        this.type = type;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String getMinLength() {
        return minLength;
    }

    public void setMinLength(String minLength) {
        this.minLength = minLength;
    }

    public String getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(String maxLength) {
        this.maxLength = maxLength;
    }
}
