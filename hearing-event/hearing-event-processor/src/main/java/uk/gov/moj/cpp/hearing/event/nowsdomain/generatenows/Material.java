package uk.gov.moj.cpp.hearing.event.nowsdomain.generatenows;

public class Material {

    private java.util.UUID id;

    private java.util.List<NowResult> nowResult;

    private String language;

    private java.util.List<UserGroups> userGroups;

    public static Material material() {
        return new Material();
    }

    public java.util.UUID getId() {
        return this.id;
    }

    public Material setId(java.util.UUID id) {
        this.id = id;
        return this;
    }

    public java.util.List<NowResult> getNowResult() {
        return this.nowResult;
    }

    public Material setNowResult(java.util.List<NowResult> nowResult) {
        this.nowResult = nowResult;
        return this;
    }

    public String getLanguage() {
        return this.language;
    }

    public Material setLanguage(String language) {
        this.language = language;
        return this;
    }

    public java.util.List<UserGroups> getUserGroups() {
        return this.userGroups;
    }

    public Material setUserGroups(java.util.List<UserGroups> userGroups) {
        this.userGroups = userGroups;
        return this;
    }
}
