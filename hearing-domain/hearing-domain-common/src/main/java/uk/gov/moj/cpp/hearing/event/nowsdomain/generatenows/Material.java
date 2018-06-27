package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public class Material implements Serializable {

    private UUID id;

    private List<NowResult> nowResult;

    private List<UserGroups> userGroups;

    public static Material material() {
        return new Material();
    }

    public UUID getId() {
        return this.id;
    }

    public Material setId(UUID id) {
        this.id = id;
        return this;
    }

    public List<NowResult> getNowResult() {
        return this.nowResult;
    }

    public Material setNowResult(List<NowResult> nowResult) {
        this.nowResult = nowResult;
        return this;
    }

    public List<UserGroups> getUserGroups() {
        return this.userGroups;
    }

    public Material setUserGroups(List<UserGroups> userGroups) {
        this.userGroups = userGroups;
        return this;
    }
}
