package uk.gov.moj.cpp.hearing.nows.events;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Material implements Serializable {

    private final static long serialVersionUID = 6679626275705377367L;
    private UUID id;
    private String language;
    private List<NowResult> nowResult;
    private List<MaterialUserGroup> userGroups = new ArrayList<MaterialUserGroup>();
    private boolean amended;

    public static Material material() {
        return new Material();
    }

    public UUID getId() {
        return id;
    }

    public Material setId(UUID id) {
        this.id = id;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public Material setLanguage(String language) {
        this.language = language;
        return this;
    }

    public List<NowResult> getNowResult() {
        return nowResult;
    }

    public Material setNowResult(List<NowResult> nowResult) {
        this.nowResult = new ArrayList<>(nowResult);
        return this;
    }

    public List<MaterialUserGroup> getUserGroups() {
        return userGroups;
    }

    public Material setUserGroups(List<MaterialUserGroup> userGroups) {
        this.userGroups = new ArrayList<>(userGroups);
        return this;
    }

    public boolean isAmended() {
        return amended;
    }

    public Material setAmended(boolean amended) {
        this.amended = amended;
        return this;
    }
}
