package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;


public class ResultsUserGroup implements Serializable {

    private final static long serialVersionUID = 2468816681916524334L;
    private String group;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

}
