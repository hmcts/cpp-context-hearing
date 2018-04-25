package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class PromptsUserGroup implements Serializable {

    private final static long serialVersionUID = -6369345874459123632L;
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
