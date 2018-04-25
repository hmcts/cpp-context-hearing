package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Prompt implements Serializable {

    private final static long serialVersionUID = -7063185622725100131L;
    private String label;
    private String value;
    private List<PromptsUserGroup> userGroups = new ArrayList<PromptsUserGroup>();

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<PromptsUserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<PromptsUserGroup> userGroups) {
        this.userGroups = userGroups;
    }
}
