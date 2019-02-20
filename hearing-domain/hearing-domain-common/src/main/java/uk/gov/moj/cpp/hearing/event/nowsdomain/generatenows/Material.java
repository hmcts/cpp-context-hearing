package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//TODO GPE-6313 remove
@SuppressWarnings({"squid:S1135"})
public class Material implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;

    private List<NowResult> nowResult = new ArrayList<>();

    private boolean amended;

    private List<UserGroups> userGroups = new ArrayList<>();

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

    public boolean isAmended() {
        return amended;
    }

    public Material setAmended(boolean amended) {
        this.amended = amended;
        return this;
    }
}
