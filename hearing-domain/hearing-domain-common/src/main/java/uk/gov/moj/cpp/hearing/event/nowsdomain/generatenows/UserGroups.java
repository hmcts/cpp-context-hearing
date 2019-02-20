package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class UserGroups implements Serializable {

    private static final long serialVersionUID = 1L;

    private String group;

    public static UserGroups userGroups() {
        return new UserGroups();
    }

    public String getGroup() {
        return this.group;
    }

    public UserGroups setGroup(String group) {
        this.group = group;
        return this;
    }
}
