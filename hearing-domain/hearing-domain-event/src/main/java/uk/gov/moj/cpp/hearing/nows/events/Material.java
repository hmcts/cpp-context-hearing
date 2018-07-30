package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class Material implements Serializable {

    private final static long serialVersionUID = 6679626275705377367L;
    private String id;
    private String language;
    private List<NowResult> nowResult;
    private List<MaterialUserGroup> userGroups = new ArrayList<MaterialUserGroup>();
    private boolean amended;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<NowResult> getNowResult() {
        return nowResult;
    }

    public void setNowResult(List<NowResult> nowResult) {
        this.nowResult = nowResult;
    }

    public List<MaterialUserGroup> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<MaterialUserGroup> userGroups) {
        this.userGroups = userGroups;
    }

    public boolean isAmended() {
        return amended;
    }

    public void setAmended(boolean amended) {
        this.amended = amended;
    }
}
