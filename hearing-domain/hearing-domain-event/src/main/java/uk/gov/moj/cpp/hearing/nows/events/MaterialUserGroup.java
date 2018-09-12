package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;

public class MaterialUserGroup implements Serializable {

    private final static long serialVersionUID = 7735855598855790581L;
    private String group;

    public String getGroup() {
        return group;
    }

    public MaterialUserGroup setGroup(String group) {
        this.group = group;
        return this;
    }

    public static MaterialUserGroup materialUserGroup(){
        return new MaterialUserGroup();
    }

}
