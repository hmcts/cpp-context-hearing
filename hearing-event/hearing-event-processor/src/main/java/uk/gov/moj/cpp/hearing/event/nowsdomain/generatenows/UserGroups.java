package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class UserGroups {

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
