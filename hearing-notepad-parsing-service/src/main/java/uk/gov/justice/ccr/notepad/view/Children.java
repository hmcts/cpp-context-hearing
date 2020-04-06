package uk.gov.justice.ccr.notepad.view;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.justice.ccr.notepad.result.cache.model.ResultType;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "label",
        "type"
})
@SuppressWarnings("squid:S2384")
public class Children {
    private String label;
    private ResultType type;
    private Set<String> fixedList;

    @JsonProperty(value = "children")
    private List<Children> childrenList;


    public Children(final String label,final ResultType type,final Set<String> fixedList,final List<Children> childrenList) {
        this.label = label;
        this.type = type;
        this.fixedList = fixedList;
        this.childrenList = childrenList;
    }

    public Children(String label, ResultType type) {
        this.label = label;
        this.type = type;
    }

    public String getLabel() { return label; }

    public ResultType getType() { return type; }

    public Set<String> getFixedList() { return fixedList; }

    public void setFixedList(final Set<String> fixedList) { this.fixedList = fixedList; }

    @JsonProperty(value = "children")
    public List<Children> getChildrenList() { return childrenList; }

    public final void addChildrenList(final Children value) {
        if (childrenList == null) {
            childrenList = newArrayList();
        }
        childrenList.add(value);
    }

}
